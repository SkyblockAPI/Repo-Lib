package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record CallExpression(Expression lhs, Collection<Expression> args) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.CALL;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.write(this.lhs, buffer);
        buffer.writeCollection(this.args, ExpressionCodec::write);
    }

    public static CallExpression decode(ByteBuffer buffer) throws IOException {
        return new CallExpression(
                ExpressionCodec.read(buffer),
                buffer.readCollection(ExpressionCodec::read)
        );
    }
}
