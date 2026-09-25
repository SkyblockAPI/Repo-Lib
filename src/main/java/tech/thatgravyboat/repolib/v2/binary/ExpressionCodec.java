package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

import java.io.IOException;

public class ExpressionCodec {

    public static Expression<?> read(DecoderContext buffer) throws IOException {
        var typeByte = buffer.readByte();
        var type = ExpressionTypeRegistry.registry.get(typeByte);
        if (type == null) {
            throw new IOException("Unknown expression type " + typeByte);
        }

        return type.decode(buffer);
    }

    public static <FileType extends Expression<FileType>> void write(FileType data, EncoderContext buffer) {
        var expression = data.expressionId();
        buffer.writeByte(expression.id());
        expression.encode(buffer, data);
    }
}
