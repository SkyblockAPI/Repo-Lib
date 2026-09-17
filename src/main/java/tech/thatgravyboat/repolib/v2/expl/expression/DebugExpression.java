package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public final class DebugExpression implements Expression {

    public static DebugExpression INSTANCE = new DebugExpression();

    private DebugExpression() {}

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.DEBUG;
    }

    @Override
    public void precode(NameTable table) {}

    @Override
    public void encode(EncoderContext buffer) {}

    public static DebugExpression decode(DecoderContext buffer) {
        return new DebugExpression();
    }
}
