package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

public record BoolExpression(boolean value) implements Expression {

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.BOOLEAN;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeBoolean(this.value);
    }

    public static BoolExpression decode(ByteBuffer buffer) throws IOException {
        return new BoolExpression(buffer.readBoolean());
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        Snippets.loadBoolValue(cb, value);
        return false;
    }
}
