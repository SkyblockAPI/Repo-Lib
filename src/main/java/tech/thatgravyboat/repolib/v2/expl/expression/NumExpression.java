package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record NumExpression(double value) implements Expression {

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.NUMBER;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeDouble(this.value);
    }

    public static NumExpression decode(ByteBuffer buffer) throws IOException {
        return new NumExpression(buffer.readDouble());
    }
}
