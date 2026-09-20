package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_Object;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record ForEachExpression(AccessExpression field, Expression array, Expression body) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var values = evaluator.getArrayOrThrow(evaluator.eval0(array));

        try {
            values.forEach(value -> {
                try {
                    evaluator.pushPop(this.toString(), () -> {
                        evaluator.eval0(new AssignExpression(field, value));
                        return evaluator.eval0(body);
                    });
                } catch (ExecutionExceptions.Continue ignored) {}
            });
        } catch (ExecutionExceptions.Break ignored) {}
        return NilValue.NIL;
    }

    @Override
    public @NotNull String toString() {
        return "for (%s : %s) %s".formatted(
            field,
            array,
            body
        );
    }
    @Override
    public boolean requiresSemicolon() {
        return false;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FOR_EACH;
    }

    @Override
    public void precode(NameTable table) {
        this.field.precode(table);
        this.array.precode(table);
        this.body.precode(table);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.field, buffer);
        ExpressionCodec.write(this.array, buffer);
        ExpressionCodec.write(this.body, buffer);
    }

    public static ForEachExpression decode(DecoderContext buffer) throws IOException {
        return new ForEachExpression(
                ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        lc.pushStack(cb, "for");
        if (array instanceof RangeExpression(
                boolean inclusiveStart, boolean inclusiveEnd, Expression from, Expression v
        )) {
            int localSlot = lc.getLowestUnused();
            int maxSlot = lc.getLowestUnused();
            Label checkLabel = cb.newLabel();
            Label loopLabel = cb.newLabel();
            Label endLoopLabel = cb.newLabel();
            if (from instanceof NumExpression(double fromValue)) {
                if (inclusiveStart) {
                    cb.loadConstant((int) fromValue);
                } else {
                    cb.loadConstant((int) fromValue + 1);
                }
            } else {
                from.compile(cb, lc);
                Snippets.getNumberOrThrow(cb);
                cb.d2i();
                if (!inclusiveStart) {
                    cb.loadConstant(1);
                    cb.iadd();
                }
            }
            cb.istore(localSlot);
            v.compile(cb, lc);
            Snippets.getNumberOrThrow(cb);
            cb.d2i();
            cb.istore(maxSlot);

            cb.iload(localSlot);
            cb.iload(maxSlot);
            cb.if_icmplt(checkLabel);
            Snippets.throwPanic(cb, "Num range start smaller than end!");
            Label oldBreakLabel = lc.getBreakLabel();
            Label oldContinueLabel = lc.getContinueLabel();
            lc.setBreakLabel(endLoopLabel);
            lc.setContinueLabel(loopLabel);
            cb.labelBinding(loopLabel);

            cb.iload(localSlot);
            cb.loadConstant(1);
            cb.iadd();
            cb.istore(localSlot);

            cb.labelBinding(checkLabel);
            cb.iload(maxSlot);
            cb.iload(localSlot);
            if (inclusiveEnd) {
                cb.if_icmplt(endLoopLabel);
            } else {
                cb.if_icmple(endLoopLabel);
            }

            cb.new_(CD_NumValue);
            cb.dup();
            cb.iload(localSlot);
            cb.i2d();
            cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
            Snippets.assign(cb, field, lc);
            cb.pop();

            if (!body.compile(cb, lc)) {
                cb.pop();
            }
            // do stuff idk
            cb.goto_(loopLabel);
            cb.labelBinding(endLoopLabel);
            lc.setBreakLabel(oldBreakLabel);
            lc.setContinueLabel(oldContinueLabel);
        } else {
            int localSlot = lc.getLowestUnused();
            ClassDesc iterator = ClassDesc.of("java.util.Iterator");
            cb.aload(1);
            array.compile(cb, lc);
            cb.invokevirtual(CD_Evaluator, "getArrayOrThrow", MethodTypeDesc.of(CD_ArrayValue, CD_Value));
            cb.invokeinterface(ClassDesc.of("java.lang.Iterable"), "iterator", MethodTypeDesc.of(iterator));
            cb.astore(localSlot);
            Label loopLabel = cb.newLabel();
            Label endLoopLabel = cb.newLabel();

            Label oldBreakLabel = lc.getBreakLabel();
            Label oldContinueLabel = lc.getContinueLabel();
            lc.setBreakLabel(endLoopLabel);
            lc.setContinueLabel(loopLabel);

            cb.labelBinding(loopLabel);
            cb.aload(localSlot);
            cb.invokeinterface(iterator, "hasNext", MethodTypeDesc.of(CD_boolean));
            cb.ifeq(endLoopLabel);
            cb.aload(localSlot);
            cb.invokeinterface(iterator, "next", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_Value);
            Snippets.assign(cb, field, lc);
            cb.pop();
            if (!body.compile(cb, lc)) {
                cb.pop();
            }

            // meow
            cb.goto_(loopLabel);
            cb.labelBinding(endLoopLabel);
            lc.setBreakLabel(oldBreakLabel);
            lc.setContinueLabel(oldContinueLabel);
            lc.free(localSlot);
        }
        Snippets.pushNil(cb);
        lc.popStack(cb);
        return false;
    }
}
