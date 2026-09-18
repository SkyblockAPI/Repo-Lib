package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryFileTypeRegistry {

    static final AtomicInteger counter = new AtomicInteger();
    static final Byte2ObjectMap<Type<?>> registry = new Byte2ObjectArrayMap<>();

    public static <FileType extends TypedFile<FileType> & Encodable> FileType readUntyped(
            Type<FileType> type,
            DecoderContext context
    ) throws IOException {
        return type.decode(context);
    }

    public static TypedFile<?> read(DecoderContext context) throws IOException {
        var typeByte = context.readByte();
        var type = registry.get(typeByte);
        if (type == null) {
            throw new IOException("Unknown file type " + typeByte);
        }

        return type.decode(context);
    }

    public static void write(EncoderContext context, TypedFile<?> data) {
        context.writeByte(data.fileId().id);
        data.encode(context);
    }

    public static void writeUntyped(EncoderContext context, TypedFile<?> data) {
        data.encode(context);
    }

    public record Type<FileType extends TypedFile<FileType> & Encodable>(
            Decoder<FileType> decoder,
            byte id) implements DataType<FileType> {
        @Override
        public FileType decode(DecoderContext buffer) throws IOException {
            return decoder.decode(buffer);
        }
    }
}
