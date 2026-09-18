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

public record NumExpression(double value) implements Expression {

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.NUMBER;
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeDouble(this.value);
    }

    public static NumExpression decode(DecoderContext buffer) throws IOException {
        return new NumExpression(buffer.readDouble());
    }

    @Override
    public void precode(NameTable table) {}

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        Snippets.loadNumValue(cb, value);
        return false;
    }
}
