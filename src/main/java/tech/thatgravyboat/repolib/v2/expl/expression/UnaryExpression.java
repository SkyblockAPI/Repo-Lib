package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

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
    public void precode(NameTable table) {
        table.insert(this.rhs);
    }

    @Override
    public void encode(EncoderContext buffer) {
        EnumCodec.encode(this.op, buffer);
        ExpressionCodec.write(this.rhs, buffer);
    }

    public static UnaryExpression decode(DecoderContext buffer) throws IOException {
        return new UnaryExpression(Op.CODEC.decode(buffer), ExpressionCodec.read(buffer));
    }

    public enum Op {
        NEGATE,
        NOT,
        ;
        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());
    }
}

