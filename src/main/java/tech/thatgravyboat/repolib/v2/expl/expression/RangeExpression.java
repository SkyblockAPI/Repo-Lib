package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedList;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_MutableArrayValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_NumValue;

public record RangeExpression(boolean inclusiveStart, boolean inclusiveEnd, Expression from, Expression to)
        implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var number = evaluator.getNumberOrThrow(evaluator.eval0(from));
        var second = evaluator.getNumberOrThrow(evaluator.eval0(to));

        var array = new LinkedList<Value>();
        var range = (second - number - (inclusiveEnd ? 0 : 1));
        if (range < 0) {
            return evaluator.panic("Num range start smaller then end!");
        }
        for (int i = inclusiveStart ? 0 : 1; i <= range; i++) {
            array.add(new NumValue(number + i));
        }
        return MutableArrayValue.create(array);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.RANGE;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        byte set = (byte) (inclusiveStart ? 1 : 0);
        if (inclusiveEnd) {
            set |= 2;
        }
        buffer.writeByte(set);
        ExpressionCodec.write(this.from, buffer);
        ExpressionCodec.write(this.to, buffer);
    }

    public static RangeExpression decode(ByteBuffer buffer) throws IOException {
        byte set = buffer.readByte();

        return new RangeExpression(
                (set & 1) == 1,
                (set & 2) == 2,
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {

        int localSlot = lc.getLowestUnused();
        int maxSlot = lc.getLowestUnused();
        int arraySlot = lc.getLowestUnused();

        Label checkLabel = cb.newLabel();
        Label loopLabel = cb.newLabel();
        Label endLoopLabel = cb.newLabel();

        ClassDesc CD_ArrayList = ClassDesc.of("java.util.ArrayList");

        cb.new_(CD_ArrayList);
        cb.dup();

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
        if (to instanceof NumExpression(double toValue)) {
            cb.loadConstant((int) toValue);
        } else {
            to.compile(cb, lc);
            Snippets.getNumberOrThrow(cb);
            cb.d2i();
        }
        cb.istore(maxSlot);

        cb.iload(maxSlot);
        cb.iload(localSlot);
        cb.isub();

        cb.invokespecial(CD_ArrayList, "<init>", MethodTypeDesc.of(CD_void, CD_int));

        cb.astore(arraySlot);
        cb.goto_(checkLabel);

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

        cb.aload(arraySlot);
        cb.new_(CD_NumValue);
        cb.dup();
        cb.iload(localSlot);
        cb.i2d();
        cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
        cb.invokeinterface(CD_List, "add", MethodTypeDesc.of(CD_boolean, CD_Object));
        cb.pop();
        cb.goto_(loopLabel);
        cb.labelBinding(endLoopLabel);

        cb.aload(arraySlot);
        cb.invokestatic(CD_MutableArrayValue, "create", MethodTypeDesc.of(CD_MutableArrayValue, CD_List));

        lc.free(localSlot);
        lc.free(maxSlot);
        lc.free(arraySlot);
        return true;
    }
}
