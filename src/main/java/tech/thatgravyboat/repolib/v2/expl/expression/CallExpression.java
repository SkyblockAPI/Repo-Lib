package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_Object;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record CallExpression(Expression lhs, Collection<Expression> args) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.CALL;
    }

    @Override
    public void precode(NameTable table) {
        this.lhs.precode(table);
        for (var arg : args) {
            arg.precode(table);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.write(this.lhs, buffer);
        buffer.writeCollection(this.args, ExpressionCodec::write);
    }

    public static CallExpression decode(DecoderContext buffer) throws IOException {
        return new CallExpression(
                ExpressionCodec.read(buffer),
                buffer.readCollection(ExpressionCodec::read)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        lhs.compile(cb, lc);
        cb.checkcast(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.FunctionValue"));
        cb.aload(1); // should always be EVALUATOR

        switch (args.size()) {
            case 0: {
                cb.invokestatic(CD_List, "of", MethodTypeDesc.of(CD_List), true);
                break;
            }
            case 1: {
                for (Expression arg : args) {
                    arg.compile(cb, lc);
                }
                cb.invokestatic(CD_List, "of", MethodTypeDesc.of(CD_List, CD_Object), true);
                break;
            }
            case 2: {
                for (Expression arg : args) {
                    arg.compile(cb, lc);
                }
                cb.invokestatic(CD_List, "of", MethodTypeDesc.of(CD_List, CD_Object, CD_Object), true);
                break;
            }
            default: {
                cb.new_(ClassDesc.of("java.util.ArrayList"));
                cb.dup();
                cb.loadConstant(args.size());
                cb.invokespecial(ClassDesc.of("java.util.ArrayList"), "<init>", MethodTypeDesc.of(CD_void, CD_int));
                for (Expression arg : args) {
                    cb.dup();
                    arg.compile(cb, lc);
                    cb.invokeinterface(CD_List, "add", MethodTypeDesc.of(CD_boolean, CD_Object));
                    cb.pop();
                }
            }
        }

        lc.pushStack(cb, "");
        cb.invokeinterface(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.FunctionValue"), "apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_List));
        lc.popStack(cb);
        return false;
    }
}
