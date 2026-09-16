package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;
import java.util.Locale;

public record StatementExpression(Op op) implements Expression {
    @Override
    public @NotNull String toString() {
        return op.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STATEMENT;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        EnumCodec.encode(this.op, buffer);
    }

    public static StatementExpression decode(ByteBuffer buffer) throws IOException {
        return new StatementExpression(Op.CODEC.decode(buffer));
    }

    public enum Op {
        RETURN, BREAK, CONTINUE,
        ;

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());
    }
}
