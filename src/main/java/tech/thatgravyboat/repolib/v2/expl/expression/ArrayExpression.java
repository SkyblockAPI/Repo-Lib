package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_MutableArrayValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record ArrayExpression(Collection<Expression> list) implements SelfEvaluatingExpression {
    @Override
    public @NotNull String toString() {
        return list.toString();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var array = MutableArrayValue.create(new ArrayList<>());

        for (var expression : list) {
            array.add(evaluator.eval0(expression));
        }

        return array;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ARRAY;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.list, ExpressionCodec::write);
    }

    public static ArrayExpression decode(ByteBuffer buffer) throws IOException {
        return new ArrayExpression(buffer.readCollection(ExpressionCodec::read));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        ClassDesc arrayList = ClassDesc.of("java.util.ArrayList");
        cb.new_(arrayList);
        cb.dup();
        cb.invokespecial(arrayList, "<init>", MTD_void);
        cb.invokestatic(CD_MutableArrayValue, "create", MethodTypeDesc.of(CD_MutableArrayValue, CD_List));
        for (Expression entry : list) {
            cb.dup();
            entry.compile(cb, lc);
            cb.invokevirtual(CD_MutableArrayValue, "add", MethodTypeDesc.of(CD_void, CD_Value));
        }
        return false;
    }
}
