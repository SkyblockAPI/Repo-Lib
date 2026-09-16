package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record UnaryExpression(Op op, Expression rhs) implements Expression {
    @Override
    public @NotNull String toString() {
        return switch (op) {
            case NEGATE -> "-" + rhs;
            case NOT -> "!" + rhs;
        };
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.UNARY;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        EnumCodec.encode(this.op, buffer);
        ExpressionCodec.write(this.rhs, buffer);
    }

    public static UnaryExpression decode(ByteBuffer buffer) throws IOException {
        return new UnaryExpression(
                Op.CODEC.decode(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    public enum Op {
        NEGATE, NOT,
        ;

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());
    }
}
