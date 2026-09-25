package tech.thatgravyboat.repolib.v2.binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;
import tech.thatgravyboat.repolib.v2.RepoBundle;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.expression.DebugExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

@ApiStatus.Internal
public interface BinaryCodec<Type> extends Decoder<Type>, Encoder<Type> {

    BinaryCodec<Integer> INT = simple(ByteBuffer::writeInt, ByteBuffer::readInt);
    BinaryCodec<Boolean> BOOLEAN = simple(ByteBuffer::writeBoolean, ByteBuffer::readBoolean);
    BinaryCodec<Long> LONG = simple(ByteBuffer::writeLong, ByteBuffer::readLong);
    BinaryCodec<Short> SHORT = simple(ByteBuffer::writeShort, ByteBuffer::readShort);
    BinaryCodec<Double> DOUBLE = simple(ByteBuffer::writeDouble, ByteBuffer::readDouble);
    BinaryCodec<Byte> BYTE = simple(ByteBuffer::writeByte, ByteBuffer::readByte);
    BinaryCodec<byte[]> BYTE_ARRAY = simple(ByteBuffer::writeByteArray, ByteBuffer::readByteArray);
    BinaryCodec<String> LITERAL_STRING = simple(ByteBuffer::writeString, ByteBuffer::readString);
    BinaryCodec<String> STRING = new BinaryCodec<>() {
        @Override
        public String decode(DecoderContext context) throws IOException {
            return context.readLiteral();
        }

        @Override
        public void collectLiterals(String data, NameTable table) {
            table.insert(data);
        }

        @Override
        public void encode(EncoderContext context, String data) {
            context.writeLiteral(data);
        }
    };

    BinaryCodec<Expression<?>> EXPRESSION = new BinaryCodec<>() {
        @Override
        public Expression<?> decode(DecoderContext context) throws IOException {
            return ExpressionCodec.read(context);
        }

        @Override
        public void collectLiterals(Expression<?> data, NameTable table) {
            data.expressionId().codec().collectLiterals(cast(data), table);
        }

        @Override
        public void encode(EncoderContext context, Expression<?> data) {
            ExpressionCodec.write(cast(data), context);
        }
    };
    BinaryCodec<Expression<?>> NULLABLE_EXPRESSION = nullable(EXPRESSION);

    static <Type extends Enum<Type>> BinaryCodec<Type> enumCodec(Type[] types) {
        return new BinaryCodec<>() {
            @Override
            public Type decode(DecoderContext context) throws IOException {
                return types[context.readInt()];
            }

            @Override
            public void collectLiterals(Type data, NameTable table) {}

            @Override
            public void encode(EncoderContext context, Type data) {
                context.writeInt(data.ordinal());
            }
        };
    }

    static <Type> BinaryCodec<Type> unit(Type instance) {
        return new BinaryCodec<>() {
            @Override
            public Type decode(DecoderContext context) {
                return instance;
            }

            @Override
            public void collectLiterals(Type data, NameTable table) {}

            @Override
            public void encode(EncoderContext context, Type data) {}
        };
    }

    default void encodeStart(Type data, ByteBuffer buffer) {
        var nameTable = NameTable.builder();

        this.collectLiterals(data, nameTable);
        var context = new EncoderContext(nameTable.freezeForEncode(), buffer);
        NameTable.CODEC.encode(context, nameTable);
        this.encode(context, data);
    }

    void collectLiterals(Type data, NameTable table);

    static <T, B> T cast(B b) {
        //noinspection unchecked
        return (T) b;
    }

    default BinaryCodec<Collection<Type>> collection() {
        return collection(this);
    }

    default BinaryCodec<Type> nullable() {
        return nullable(this);
    }

    static <Type> BinaryCodec<Collection<Type>> collection(BinaryCodec<Type> codec) {
        return new BinaryCodec<>() {
            @Override
            public Collection<Type> decode(DecoderContext context) throws IOException {
                var amount = context.readInt();
                var list = new ArrayList<Type>();
                for (var i = 0; i < amount; i++) {
                    list.add(codec.decode(context));
                }
                return list;
            }

            @Override
            public void collectLiterals(Collection<Type> data, NameTable table) {
                for (var datum : data) {
                    codec.collectLiterals(datum, table);
                }
            }

            @Override
            public void encode(EncoderContext context, Collection<Type> data) {
                context.writeInt(data.size());
                for (var datum : data) {
                    codec.encode(context, datum);
                }
            }
        };
    }

    static <Key, Value> BinaryCodec<Map<Key, Value>> map(BinaryCodec<Key> keyCodec, BinaryCodec<Value> valueCodec) {
        return new BinaryCodec<>() {
            @Override
            public Map<Key, Value> decode(DecoderContext context) throws IOException {
                var size = context.readInt();
                var map = new HashMap<Key, Value>();

                for (var i = 0; i < size; i++) {
                    map.put(keyCodec.decode(context), valueCodec.decode(context));
                }

                return map;
            }

            @Override
            public void collectLiterals(Map<Key, Value> data, NameTable table) {
                for (var keyValueEntry : data.entrySet()) {
                    keyCodec.collectLiterals(keyValueEntry.getKey(), table);
                    valueCodec.collectLiterals(keyValueEntry.getValue(), table);
                }
            }

            @Override
            public void encode(EncoderContext context, Map<Key, Value> data) {
                context.writeInt(data.size());

                for (var keyValueEntry : data.entrySet()) {
                    keyCodec.encode(context, keyValueEntry.getKey());
                    valueCodec.encode(context, keyValueEntry.getValue());
                }
            }
        };
    }

    static <Type> BinaryCodec<Type> nullable(BinaryCodec<Type> codec) {
        return new BinaryCodec<>() {
            @Override
            public void encode(EncoderContext context, Type data) {
                if (data == null) {
                    context.writeBoolean(false);
                } else {
                    context.writeBoolean(true);
                    codec.encode(context, data);
                }
            }

            @Override
            public void collectLiterals(Type data, NameTable table) {
                if (data == null) {
                    return;
                }
                codec.collectLiterals(data, table);
            }

            @Override
            public Type decode(DecoderContext context) throws IOException {
                if (context.readBoolean()) {
                    return codec.decode(context);
                }

                return null;
            }
        };
    }


    private static <Type> BinaryCodec<Type> simple(Encoder<Type> encoder, Decoder<Type> decoder) {
        return new BinaryCodec<>() {
            @Override
            public void encode(EncoderContext context, Type data) {
                encoder.encode(context, data);
            }

            @Override
            public void collectLiterals(Type data, NameTable table) {}

            @Override
            public Type decode(DecoderContext context) throws IOException {
                return decoder.decode(context);
            }
        };
    }

    default <Mapped> BinaryCodec<Mapped> mapped(Function<Type, Mapped> to, Function<Mapped, Type> from) {
        final var self = this;
        return new BinaryCodec<>() {
            @Override
            public Mapped decode(DecoderContext context) throws IOException {
                return to.apply(self.decode(context));
            }

            @Override
            public void collectLiterals(Mapped data, NameTable table) {
                self.collectLiterals(from.apply(data), table);
            }

            @Override
            public void encode(EncoderContext context, Mapped data) {
                self.encode(context, from.apply(data));
            }
        };
    }

    default <Owner> Entry<Owner, Type> forGetter(Function<Owner, Type> getter) {
        return new Entry<>(this, getter);
    }

    record Entry<Owner, Type>(BinaryCodec<Type> codec, Function<Owner, Type> getter) {
    }

    static <KeyType, Type> BinaryCodec<Type> dispatch(BinaryCodec<KeyType> keyCodec, Map<KeyType, BinaryCodec<? extends Type>> map, Function<Type, KeyType> keySupplier) {
        return new BinaryCodec<>() {
            @Override
            public Type decode(DecoderContext context) throws IOException {
                var key = keyCodec.decode(context);
                var codec = map.get(key);
                return codec.decode(context);
            }

            @Override
            public void collectLiterals(Type data, NameTable table) {
                map.get(keySupplier.apply(data)).collectLiterals(cast(data), table);
            }

            @Override
            public void encode(EncoderContext context, Type data) {
                var key = keySupplier.apply(data);
                keyCodec.encode(context, key);
                var codec = map.get(keySupplier.apply(data));
                codec.encode(context, cast(data));
            }
        };
    }
}
