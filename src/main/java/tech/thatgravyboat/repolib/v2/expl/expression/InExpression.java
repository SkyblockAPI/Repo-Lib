package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record InExpression(AccessExpression holder, Expression field) implements Expression {
    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.IN;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeUntyped(this.holder, buffer);
        ExpressionCodec.write(this.field, buffer);
    }

    public static InExpression decode(ByteBuffer buffer) throws IOException {
        return new InExpression(
                ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
                ExpressionCodec.read(buffer)
        );
    }
}
