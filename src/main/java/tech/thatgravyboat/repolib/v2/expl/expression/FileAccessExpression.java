package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

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
}
