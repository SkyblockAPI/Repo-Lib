package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record NumExpression(double value) implements Expression {

    @Override
    public @NotNull String toString() {
        return String.valueOf(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.NUMBER;
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeDouble(this.value);
    }

    public static NumExpression decode(DecoderContext buffer) throws IOException {
        return new NumExpression(buffer.readDouble());
    }

    @Override
    public void precode(NameTable table) {}

}
