package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record BlockExpression(Collection<Expression> exprs) implements Expression {

    @Override
    public @NotNull String toString() {
        if (exprs.isEmpty()) {
            return "{}";
        }
        return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.BLOCK;
    }

    @Override
    public void precode(NameTable table) {
        for (var expr : exprs) {
            expr.precode(table);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.exprs, ExpressionCodec::write);
    }

    public static BlockExpression decode(DecoderContext buffer) throws IOException {
        return new BlockExpression(buffer.readCollection(ExpressionCodec::read));
    }

    public record LastElement(Expression expression) implements SelfEvaluatingExpression {
        @Override
        public Value evaluate(Evaluator evaluator) {
            return evaluator.eval0(this.expression);
        }

        @Override
        public boolean canReturnValueBeReturned() {
            return true;
        }

        @Override
        public ExpressionTypeRegistry.Type<?> expressionId() {
            return ExpressionTypes.BLOCK_LAST_ELEMENT;
        }

        @Override
        public void encode(EncoderContext buffer) {
            ExpressionCodec.write(this.expression, buffer);
        }

        @Override
        public void precode(NameTable table) {
            table.insert(this.expression);
        }

        public static LastElement decode(DecoderContext buffer) throws IOException {
            return new LastElement(ExpressionCodec.read(buffer));
        }
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }
}
