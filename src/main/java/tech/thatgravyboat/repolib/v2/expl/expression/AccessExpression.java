package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record AccessExpression(@Nullable Expression lhs, Expression field) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }

    public static AccessExpression decode(ByteBuffer buffer) throws IOException {
        return new AccessExpression(
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ACCESS;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeNullable(this.lhs, buffer);
        ExpressionCodec.write(this.field, buffer);
    }
}
