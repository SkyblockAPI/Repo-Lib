package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryFileTypeRegistry {

    static final AtomicInteger counter = new AtomicInteger();
    static final Byte2ObjectMap<Type<?>> registry = new Byte2ObjectArrayMap<>();

    public static <FileType extends TypedFile<FileType> & Encodable> FileType readTyped(
            Type<FileType> type,
            ByteBuffer buffer
    ) throws IOException {
        var typeByte = buffer.readByte();
        if (type != registry.get(typeByte)) {
            throw new IOException("Expected file type " + type.id + " but got " + typeByte);
        }

        return type.decode(buffer);
    }

    public static TypedFile<?> read(ByteBuffer buffer) throws IOException {
        var typeByte = buffer.readByte();
        var type = registry.get(typeByte);
        if (type == null) {
            throw new IOException("Unknown file type " + typeByte);
        }

        return type.decode(buffer);
    }

    public static void write(ByteBuffer buffer, TypedFile<?> data) {
        buffer.writeByte(data.fileId().id);
        data.encode(buffer);
    }

    public record Type<FileType extends TypedFile<FileType> & Encodable>(
            Decoder<FileType> decoder,
            byte id) implements DataType<FileType> {
        @Override
        public FileType decode(ByteBuffer buffer) throws IOException {
            return decoder.decode(buffer);
        }
    }
}
