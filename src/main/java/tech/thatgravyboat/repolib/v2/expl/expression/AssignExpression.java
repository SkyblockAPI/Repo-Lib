package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record AssignExpression(AccessExpression lhs, Expression value) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + " = " + value;
    }

    @Override
    public boolean requiresSemicolon() {
        return value.requiresSemicolon();
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ASSIGN;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeUntyped(this.lhs, buffer);
        ExpressionCodec.write(this.value, buffer);
    }

    public static AssignExpression decode(ByteBuffer buffer) throws IOException {
        return new AssignExpression(
                ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
                ExpressionCodec.read(buffer)
        );
    }
}
