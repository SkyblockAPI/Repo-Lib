package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

public record StrExpression(String value) implements Expression {

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STRING;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeString(this.value);
    }

    public static StrExpression decode(ByteBuffer buffer) throws IOException {
        return new StrExpression(buffer.readString());
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        Snippets.loadStrValue(cb, value);
        return false;
    }
}
