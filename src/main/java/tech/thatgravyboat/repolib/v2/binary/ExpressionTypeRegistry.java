package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpressionTypeRegistry {

    static final AtomicInteger counter = new AtomicInteger();
    static final Byte2ObjectMap<ExpressionTypeRegistry.Type<?>> registry = new Byte2ObjectArrayMap<>();

    public record Type<ExpressionType extends Expression & Encodable>(
            Decoder<ExpressionType> decoder,
            byte id
    ) implements DataType<ExpressionType> {
        @Override
        public ExpressionType decode(DecoderContext stream) throws IOException {
            return decoder.decode(stream);
        }
    }
}
