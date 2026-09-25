package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;

public class BinaryFileTypeRegistry {

    static final Byte2ObjectMap<Type<? extends TypedFile<?>>> registry = new Byte2ObjectArrayMap<>();
    static final AtomicInteger counter = new AtomicInteger();

    public static final BinaryCodec<TypedFile<?>> CODEC = BinaryCodec.dispatch(BinaryCodec.BYTE, BinaryCodec.cast(registry), (type) -> type.fileId().id());

    public static final BinaryCodec<FunctionValueFile<?>> FUNCTION_FILE = BinaryCodec.dispatch(BinaryCodec.BYTE, new HashMap<>() {{
        put(FileTypes.FUNCTION.id(), FileTypes.FUNCTION);
        put(FileTypes.MODULE.id(), FileTypes.MODULE);
    }}, (type) -> type.fileId().id());

    public static <FileType extends TypedFile<FileType>> FileType readUntyped(
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

    public static <Type extends TypedFile<Type>> void write(EncoderContext context, Type data) {
        context.writeByte(data.fileId().id);
        data.fileId().codec.encode(context, data);
    }

    public record Type<FileType extends TypedFile<FileType>>(
            BinaryCodec<FileType> codec,
            byte id) implements BinaryCodec<FileType> {

        @Override
        public FileType decode(DecoderContext buffer) throws IOException {
            return codec.decode(buffer);
        }

        @Override
        public void encode(EncoderContext context, FileType data) {
            this.codec.encode(context, data);
        }

        @Override
        public void collectLiterals(FileType data, NameTable table) {
            this.codec.collectLiterals(data, table);
        }
    }
}
