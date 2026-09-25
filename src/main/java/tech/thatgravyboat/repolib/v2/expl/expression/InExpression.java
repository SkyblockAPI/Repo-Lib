package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record InExpression(AccessExpression holder, Expression field) implements Expression {
    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.IN;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.holder);
        table.insert(this.field);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.holder, buffer);
        ExpressionCodec.write(this.field, buffer);
    }

    public static InExpression decode(DecoderContext buffer) throws IOException {
        return new InExpression(
            ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
            ExpressionCodec.read(buffer));
    }
}
