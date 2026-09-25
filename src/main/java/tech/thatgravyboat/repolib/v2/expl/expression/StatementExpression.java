package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record StatementExpression(Op op) implements Expression<StatementExpression> {

    public static final BinaryCodec<StatementExpression> CODEC =
        Op.CODEC.mapped(StatementExpression::new, StatementExpression::op);

    @Override
    public @NotNull String toString() {
        return op.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ExpressionTypeRegistry.Type<StatementExpression> expressionId() {
        return ExpressionTypes.STATEMENT;
    }

    public enum Op {
        RETURN,
        BREAK,
        CONTINUE,
        ;

        public static final BinaryCodec<Op> CODEC = BinaryCodec.enumCodec(values());
    }
}
