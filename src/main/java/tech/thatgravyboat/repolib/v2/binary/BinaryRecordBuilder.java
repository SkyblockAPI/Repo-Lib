package tech.thatgravyboat.repolib.v2.binary;

import java.io.IOException;
import java.util.function.Function;

public class BinaryRecordBuilder {

    public static <Instance, First> BinaryCodec<Instance> of(
        BinaryCodec.Entry<Instance, First> first,
        Function<First, Instance> constructor
    ) {
        return new BinaryCodec<Instance>() {
            @Override
            public Instance decode(DecoderContext context) throws IOException {
                return constructor.apply(first.codec().decode(context));
            }

            @Override
            public void collectLiterals(Instance data, NameTable table) {
                first.codec().collectLiterals(first.getter().apply(data), table);
            }

            @Override
            public void encode(EncoderContext context, Instance data) {
                first.codec().encode(context, first.getter().apply(data));
            }
        };
    }

    public interface Function2<A, B, R> {
        R apply(A a, B b);
    }

    public static <Instance, First, Second> BinaryCodec<Instance> of(
        BinaryCodec.Entry<Instance, First> first,
        BinaryCodec.Entry<Instance, Second> second,
        Function2<First, Second, Instance> constructor
    ) {
        return new BinaryCodec<>() {
            @Override
            public Instance decode(DecoderContext context) throws IOException {
                var a = first.codec().decode(context);
                var b = second.codec().decode(context);
                return constructor.apply(a, b);
            }

            @Override
            public void collectLiterals(Instance data, NameTable table) {
                first.codec().collectLiterals(first.getter().apply(data), table);
                second.codec().collectLiterals(second.getter().apply(data), table);
            }

            @Override
            public void encode(EncoderContext context, Instance data) {
                first.codec().encode(context, first.getter().apply(data));
                second.codec().encode(context, second.getter().apply(data));
            }
        };
    }


    public interface Function3<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    public static <Instance, First, Third, Second> BinaryCodec<Instance> of(
        BinaryCodec.Entry<Instance, First> first,
        BinaryCodec.Entry<Instance, Second> second,
        BinaryCodec.Entry<Instance, Third> third,
        Function3<First, Second, Third, Instance> constructor
    ) {
        return new BinaryCodec<>() {
            @Override
            public Instance decode(DecoderContext context) throws IOException {
                var a = first.codec().decode(context);
                var b = second.codec().decode(context);
                var c = third.codec().decode(context);
                return constructor.apply(a, b, c);
            }

            @Override
            public void collectLiterals(Instance data, NameTable table) {
                first.codec().collectLiterals(first.getter().apply(data), table);
                second.codec().collectLiterals(second.getter().apply(data), table);
                third.codec().collectLiterals(third.getter().apply(data), table);
            }

            @Override
            public void encode(EncoderContext context, Instance data) {
                first.codec().encode(context, first.getter().apply(data));
                second.codec().encode(context, second.getter().apply(data));
                third.codec().encode(context, third.getter().apply(data));
            }
        };
    }
    public interface Function4<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    public static <Instance, First, Third, Fourth, Second> BinaryCodec<Instance> of(
        BinaryCodec.Entry<Instance, First> first,
        BinaryCodec.Entry<Instance, Second> second,
        BinaryCodec.Entry<Instance, Third> third,
        BinaryCodec.Entry<Instance, Fourth> fourth,
        Function4<First, Second, Third, Fourth, Instance> constructor
    ) {
        return new BinaryCodec<>() {
            @Override
            public Instance decode(DecoderContext context) throws IOException {
                var a = first.codec().decode(context);
                var b = second.codec().decode(context);
                var c = third.codec().decode(context);
                var d = fourth.codec().decode(context);
                return constructor.apply(a, b, c, d);
            }

            @Override
            public void collectLiterals(Instance data, NameTable table) {
                first.codec().collectLiterals(first.getter().apply(data), table);
                second.codec().collectLiterals(second.getter().apply(data), table);
                third.codec().collectLiterals(third.getter().apply(data), table);
                fourth.codec().collectLiterals(fourth.getter().apply(data), table);
            }

            @Override
            public void encode(EncoderContext context, Instance data) {
                first.codec().encode(context, first.getter().apply(data));
                second.codec().encode(context, second.getter().apply(data));
                third.codec().encode(context, third.getter().apply(data));
                fourth.codec().encode(context, fourth.getter().apply(data));
            }
        };
    }

}
