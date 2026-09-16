package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record BoolExpression(boolean value) implements Expression {

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.BOOLEAN;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeBoolean(this.value);
    }

    public static BoolExpression decode(ByteBuffer buffer) throws IOException {
        return new BoolExpression(buffer.readBoolean());
    }
}
