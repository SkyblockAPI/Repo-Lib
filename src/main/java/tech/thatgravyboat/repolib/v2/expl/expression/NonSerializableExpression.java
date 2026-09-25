package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;

public non-sealed interface NonSerializableExpression extends Expression<NonSerializableExpression> {
    @Override
    default ExpressionTypeRegistry.Type<NonSerializableExpression> expressionId() {
        throw new UnsupportedOperationException("Unable to serialize " + this.getClass().getSimpleName());
    }
}
