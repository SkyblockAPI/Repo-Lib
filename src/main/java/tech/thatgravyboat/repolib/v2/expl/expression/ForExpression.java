package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;

public record ForExpression(@Nullable Expression init, @Nullable Expression cond, @Nullable Expression incr, Expression body) implements Expression {

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
                init == null ? "" : init,
                cond == null ? "" : cond,
                incr == null ? "" : incr,
                body
        );
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
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeNullable(this.init, buffer);
        ExpressionCodec.writeNullable(this.cond, buffer);
        ExpressionCodec.writeNullable(this.incr, buffer);
        ExpressionCodec.write(this.body, buffer);
    }

    public static ForExpression decode(ByteBuffer buffer) throws IOException {
        return new ForExpression(
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }
}
