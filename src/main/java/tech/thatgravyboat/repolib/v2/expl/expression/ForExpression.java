package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record ForExpression(
    @Nullable Expression init, @Nullable Expression cond, @Nullable Expression incr, Expression body
) implements Expression {

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
            init == null ? "" : init,
            cond == null ? "" : cond,
            incr == null ? "" : incr,
            body);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }


    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FOR;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.init);
        table.insert(this.cond);
        table.insert(this.incr);
        table.insert(this.body);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeNullable(this.init, buffer);
        ExpressionCodec.writeNullable(this.cond, buffer);
        ExpressionCodec.writeNullable(this.incr, buffer);
        ExpressionCodec.write(this.body, buffer);
    }

    public static ForExpression decode(DecoderContext buffer) throws IOException {
        return new ForExpression(
            ExpressionCodec.readNullable(buffer),
            ExpressionCodec.readNullable(buffer),
            ExpressionCodec.readNullable(buffer),
            ExpressionCodec.read(buffer));
    }

}
