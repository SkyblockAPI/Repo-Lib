package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record StrExpression(String value) implements Expression {

    @Override
    public @NotNull String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STRING;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.value);
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeLiteral(this.value);
    }

    public static StrExpression decode(DecoderContext buffer) throws IOException {
        return new StrExpression(buffer.readLiteral());
    }

}
