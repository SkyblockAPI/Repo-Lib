package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.util.Locale;

public record StatementExpression(Op op) implements Expression {
    @Override
    public @NotNull String toString() {
        return op.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STATEMENT;
    }

    @Override
    public void encode(EncoderContext buffer) {
        EnumCodec.encode(this.op, buffer);
    }

    public static StatementExpression decode(DecoderContext buffer) throws IOException {
        return new StatementExpression(Op.CODEC.decode(buffer));
    }

    @Override
    public void precode(NameTable table) {}

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        switch (op) {
            case RETURN -> {
                Snippets.pushNil(cb);
                lc.popAll(cb);
                cb.areturn();
                return true;
            }
            case BREAK -> cb.goto_(lc.getBreakLabel());
            case CONTINUE -> cb.goto_(lc.getContinueLabel());
            default -> Snippets.throwPanic(cb, "didnt know how to compile " + op);
        }
        return false;
    }

    public enum Op {
        RETURN, BREAK, CONTINUE,
        ;

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());
    }
}
