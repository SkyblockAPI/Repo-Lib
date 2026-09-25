package tech.thatgravyboat.repolib.v2.binary;

import java.io.IOException;

public record EnumCodec<Type extends Enum<Type>>(Type[] types) implements Decoder<Type> {
    @Override
    public Type decode(DecoderContext buffer) throws IOException {
        return types[buffer.readByte()];
    }

    public static <Type extends Enum<Type>> void encode(Type type, EncoderContext buffer) {
        buffer.writeByte((byte) type.ordinal());
    }

}
