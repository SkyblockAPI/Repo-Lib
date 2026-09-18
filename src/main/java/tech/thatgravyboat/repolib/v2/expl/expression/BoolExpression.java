package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

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
    public void encode(EncoderContext buffer) {
        buffer.writeBoolean(this.value);
    }

    @Override
    public void precode(NameTable table) {}

    public static BoolExpression decode(DecoderContext buffer) throws IOException {
        return new BoolExpression(buffer.readBoolean());
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        Snippets.loadBoolValue(cb, value);
        return false;
    }
}
