package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import static java.lang.constant.ConstantDescs.CD_String;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

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
    public void precode(NameTable table) {
        for (var expression : path) {
            expression.precode(table);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.path, ExpressionCodec::write);
    }

    public static FileAccessExpression decode(DecoderContext buffer) throws IOException {
        return new FileAccessExpression(buffer.readCollection(ExpressionCodec::read));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        cb.aload(1);

        StringBuilder constantSections = new StringBuilder();
        int size = path.size();

        Expression[] pathArray = path.toArray(new Expression[0]);
        for (int i = 0; i < pathArray.length; i++) {
            Expression pathSegment = pathArray[i];
            if (pathSegment instanceof StrExpression(String pathSegmentString)) {
                constantSections.append(pathSegmentString);
                size -= 1;
            } else {
                pathSegment.compile(cb, lc);
                Snippets.getStringOrThrow(cb);
                constantSections.append("\u0001");
            }
            if (i != pathArray.length - 1) {
                constantSections.append("/");
            }
        }
        if (size != 0) {
            Snippets.stringStringConcat(cb, constantSections.toString(), size);
        } else {
            cb.loadConstant(constantSections.toString());
        }
        cb.invokevirtual(CD_Evaluator, "getFileAccess", MethodTypeDesc.of(CD_FunctionValue, CD_String));
        return false;
    }
}
