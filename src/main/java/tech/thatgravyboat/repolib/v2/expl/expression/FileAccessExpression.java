package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import static java.lang.constant.ConstantDescs.CD_String;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

public record FileAccessExpression(Collection<Expression> path) implements Expression {

    @Override
    public @NotNull String toString() {
        return path.toString();
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FILE_ACCESS;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.path, ExpressionCodec::write);
    }

    public static FileAccessExpression decode(ByteBuffer buffer) throws IOException {
        return new FileAccessExpression(buffer.readCollection(ExpressionCodec::read));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        cb.aload(1);
        for (Expression pathSegment : path) {
            if (pathSegment instanceof StrExpression(String pathSegmentString)) {
                cb.loadConstant(pathSegmentString);
            } else {
                cb.aload(1);
                pathSegment.compile(cb, lc);
                cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
            }
        }
        Snippets.pathStringConcat(cb, path.size());
        cb.invokevirtual(CD_Evaluator, "getFileAccess", MethodTypeDesc.of(CD_FunctionValue, CD_String));
        return false;
    }
}
