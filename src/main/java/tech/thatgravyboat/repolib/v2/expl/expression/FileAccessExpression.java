package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

public record FileAccessExpression(Collection<Expression> path) implements Expression {

    @Override
    public @NotNull String toString() {
        return path.toString();
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FILE_ACCESS;
    }

    @Override
    public void precode(NameTable table) {
        for (var expression : path) {
            expression.precode(table);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.path, ExpressionCodec::write);
    }

    public static FileAccessExpression decode(DecoderContext buffer) throws IOException {
        return new FileAccessExpression(buffer.readCollection(ExpressionCodec::read));
    }
}
