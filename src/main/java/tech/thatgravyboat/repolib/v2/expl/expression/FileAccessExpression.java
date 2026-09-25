package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record FileAccessExpression(Collection<Expression<?>> path) implements Expression<FileAccessExpression> {

    public static final BinaryCodec<FileAccessExpression> CODEC =
        BinaryCodec.EXPRESSION.collection().mapped(FileAccessExpression::new, FileAccessExpression::path);

    @Override
    public @NotNull String toString() {
        return path.toString();
    }

    @Override
    public ExpressionTypeRegistry.Type<FileAccessExpression> expressionId() {
        return ExpressionTypes.FILE_ACCESS;
    }


}
