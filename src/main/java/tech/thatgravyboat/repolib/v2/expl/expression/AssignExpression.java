package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

public record AssignExpression(AccessExpression lhs, Expression value) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + " = " + value;
    }

    @Override
    public boolean requiresSemicolon() {
        return value.requiresSemicolon();
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ASSIGN;
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.lhs, buffer);
        ExpressionCodec.write(this.value, buffer);
    }

    @Override
    public void precode(NameTable table) {
        this.lhs.precode(table);
        this.value.precode(table);
    }

    public static AssignExpression decode(DecoderContext buffer) throws IOException {
        return new AssignExpression(
                ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        value.compile(cb, lc);
        Snippets.assign(cb, lhs, lc);
        return false;
    }
}
