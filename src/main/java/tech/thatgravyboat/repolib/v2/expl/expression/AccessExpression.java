package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

import java.io.IOException;

public record AccessExpression(@Nullable Expression lhs, Expression field) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }

    public static AccessExpression decode(DecoderContext buffer) throws IOException {
        return new AccessExpression(
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ACCESS;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.lhs);
        this.field.precode(table);
    }

    @Override
    public void encode(EncoderContext context) {
        ExpressionCodec.writeNullable(this.lhs, context);
        ExpressionCodec.write(this.field, context);
    }
}
