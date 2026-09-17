package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.util.Collection;
import java.util.stream.Collectors;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

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
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.exprs, ExpressionCodec::write);
    }

    public static BlockExpression decode(ByteBuffer buffer) throws IOException {
        return new BlockExpression(buffer.readCollection(ExpressionCodec::read));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        if (exprs.isEmpty()) {
            Snippets.pushNil(cb);
        }
        Expression[] exprs = this.exprs.toArray(new Expression[0]);
        for (int i = 0; i < exprs.length; i++) {
            Expression expr = exprs[i];
            if (expr.compile(cb, lc)) {
                return true;
            }
            if (i != exprs.length - 1) {
                cb.pop();
            }
        }
        return false;
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
        public void encode(ByteBuffer buffer) {
            ExpressionCodec.write(this.expression, buffer);
        }

        public static LastElement decode(ByteBuffer buffer) throws IOException {
            return new LastElement(ExpressionCodec.read(buffer));
        }

        @Override
        public boolean compile(CodeBuilder cb, CompilationTracker lc) {
            return expression.compile(cb, lc);
        }
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }
}
