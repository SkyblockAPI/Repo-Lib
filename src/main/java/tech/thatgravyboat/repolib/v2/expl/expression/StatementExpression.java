package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

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
        RETURN(ExecutionExceptions.RETURN),
        BREAK(ExecutionExceptions.BREAK),
        CONTINUE(ExecutionExceptions.CONTINUE),
        ;

        private final RuntimeException exception;

        Op(RuntimeException exception) {
            this.exception = exception;
        }

        public Value raise() {
            throw exception;
        }

        public static final BinaryCodec<Op> CODEC = BinaryCodec.enumCodec(values());
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        return this.op.raise();
    }
}
