package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record AssignExpression(AccessExpression lhs, Expression<?> value) implements Expression<AssignExpression> {

    public static final BinaryCodec<AssignExpression> CODEC = BinaryRecordBuilder.of(
        AccessExpression.CODEC.forGetter(AssignExpression::lhs),
        BinaryCodec.EXPRESSION.forGetter(AssignExpression::value),
        AssignExpression::new);

    @Override
    public @NotNull String toString() {
        return lhs + " = " + value;
    }

    @Override
    public boolean requiresSemicolon() {
        return value.requiresSemicolon();
    }

    @Override
    public ExpressionTypeRegistry.Type<AssignExpression> expressionId() {
        return ExpressionTypes.ASSIGN;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var access = lhs();
        final Value field;
        if (access.lhs() == null) {
            field = evaluator.scope();
        } else {
            field = evaluator.eval0(access.lhs());
        }

        return evaluator.set(field, evaluator.eval0(access.field()).asString(), evaluator.eval0(value()));
    }
}
