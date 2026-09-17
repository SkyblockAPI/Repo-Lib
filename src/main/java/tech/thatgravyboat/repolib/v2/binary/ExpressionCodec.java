package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

import java.io.IOException;

public class ExpressionCodec {


    public static <ExpressionType extends Expression & Encodable> ExpressionType readUntyped(
            ExpressionTypeRegistry.Type<ExpressionType> type,
            DecoderContext buffer
    ) throws IOException {
        return type.decode(buffer);
    }

    public static Expression read(DecoderContext buffer) throws IOException {
        var typeByte = buffer.readByte();
        var type = ExpressionTypeRegistry.registry.get(typeByte);
        if (type == null) {
            throw new IOException("Unknown expression type " + typeByte);
        }

        return type.decode(buffer);
    }

    public static <ExpressionType extends Expression & Encodable> ExpressionType readUntypedNullable(
            ExpressionTypeRegistry.Type<ExpressionType> type,
            DecoderContext buffer
    ) throws IOException {
        if (!buffer.readBoolean()) {
            return null;
        }
        return type.decode(buffer);
    }


    public static Expression readNullable(DecoderContext buffer) throws IOException {
        if (!buffer.readBoolean()) {
            return null;
        }
        var typeByte = buffer.readByte();
        var type = ExpressionTypeRegistry.registry.get(typeByte);
        if (type == null) {
            throw new IOException("Unknown expression type " + typeByte);
        }

        return type.decode(buffer);
    }

    public static <FileType extends Expression & Encodable> void write(FileType data, EncoderContext buffer) {
        buffer.writeByte(data.expressionId().id());
        data.encode(buffer);
    }

    public static <FileType extends Expression & Encodable> void writeUntyped(FileType data, EncoderContext buffer) {
        data.encode(buffer);
    }

    public static <FileType extends Expression & Encodable> void writeUntypedNullable(FileType data, EncoderContext buffer) {
        buffer.writeBoolean(data != null);
        if (data == null) {
            return;
        }
        data.encode(buffer);
    }

    public static <FileType extends Expression & Encodable> void writeNullable(FileType data, EncoderContext buffer) {
        buffer.writeBoolean(data != null);
        if (data == null) {
            return;
        }
        buffer.writeByte(data.expressionId().id());
        data.encode(buffer);
    }


}
