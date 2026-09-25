package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public final class DebugExpression implements Expression<DebugExpression> {

    public static DebugExpression INSTANCE = new DebugExpression();

    public static final BinaryCodec<DebugExpression> CODEC = BinaryCodec.unit(INSTANCE);

    private DebugExpression() {}

    @Override
    public ExpressionTypeRegistry.Type<DebugExpression> expressionId() {
        return ExpressionTypes.DEBUG;
    }


}
