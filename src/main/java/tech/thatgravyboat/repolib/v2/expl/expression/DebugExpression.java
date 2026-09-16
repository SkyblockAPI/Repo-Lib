package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public final class DebugExpression implements Expression {

    public static DebugExpression INSTANCE = new DebugExpression();

    private DebugExpression() {}

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.DEBUG;
    }

    @Override
    public void encode(ByteBuffer buffer) {}

    public static DebugExpression decode(ByteBuffer buffer) {
        return new DebugExpression();
    }
}
