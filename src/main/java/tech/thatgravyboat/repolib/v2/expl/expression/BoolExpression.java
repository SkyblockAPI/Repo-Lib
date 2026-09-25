package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record BoolExpression(boolean value) implements Expression {

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.BOOLEAN;
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeBoolean(this.value);
    }

    @Override
    public void precode(NameTable table) {}

    public static BoolExpression decode(DecoderContext buffer) throws IOException {
        return new BoolExpression(buffer.readBoolean());
    }
}
