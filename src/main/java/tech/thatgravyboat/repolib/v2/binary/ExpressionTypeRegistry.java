package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpressionTypeRegistry {

    static final AtomicInteger counter = new AtomicInteger();
    static final Byte2ObjectMap<ExpressionTypeRegistry.Type<?>> registry = new Byte2ObjectArrayMap<>();

    public record Type<ExpressionType extends Expression<ExpressionType>>(
            BinaryCodec<ExpressionType> codec,
            byte id
    ) implements BinaryCodec<ExpressionType> {
        @Override
        public ExpressionType decode(DecoderContext context) throws IOException {
            return this.codec.decode(context);
        }

        @Override
        public void encode(EncoderContext context, ExpressionType data) {
            this.codec.encode(context, data);
        }

        @Override
        public void collectLiterals(ExpressionType data, NameTable table) {
            this.codec.collectLiterals(data, table);
        }
    }
}
