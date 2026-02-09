package tech.thatgravyboat.repolib.v2.internal.codec.struct;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.Functions.*;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructFieldRepoCodec;

import java.util.function.Supplier;

public class StructRepoCodec {

    public static <Struct> RepoCodec<Struct> create(
            Function1<JsonObject, CodecResult<Struct>> decoder,
            Function1<Struct, CodecResult<JsonObject>> encoder
    ) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<Struct> decode(JsonElement element) {
                if (element instanceof JsonObject object) {
                    try {
                        return decoder.apply(object);
                    } catch (CodecResult.ResultException e) {
                        return CodecResult.failure(e.result().error());
                    }
                }
                return CodecResult.failure("Expected an object");
            }

            @Override
            public CodecResult<JsonElement> encode(Struct value) {
                try {
                    return encoder.apply(value).map(json -> json);
                } catch (CodecResult.ResultException e) {
                    return CodecResult.failure(e.result().error());
                }
            }
        };
    }

    public static <Struct> RepoCodec<Struct> of(Supplier<Struct> factory) {
        return create(
                object -> CodecResult.success(factory.get()),
                struct -> CodecResult.success(new JsonObject())
        );
    }

    public static <Struct, P1> RepoCodec<Struct> of(
            Function1<P1, Struct> factory,
            StructFieldRepoCodec<P1> codec1, Function1<Struct, P1> getter1
    ) {
        return create(
                object -> {
                    var p1 = codec1.decode(object).orElseThrow();

                    return CodecResult.success(factory.apply(p1));
                },
                struct -> {
                    JsonObject object = new JsonObject();

                    codec1.encode(object, getter1.apply(struct)).orElseThrow();

                    return CodecResult.success(object);
                }
        );
    }

    public static <Struct, P1, P2> RepoCodec<Struct> of(
            Function2<P1, P2, Struct> factory,
            StructFieldRepoCodec<P1> codec1, Function1<Struct, P1> getter1,
            StructFieldRepoCodec<P2> codec2, Function1<Struct, P2> getter2
    ) {
        return create(
                object -> {
                    var p1 = codec1.decode(object).orElseThrow();
                    var p2 = codec2.decode(object).orElseThrow();

                    return CodecResult.success(factory.apply(p1, p2));
                },
                struct -> {
                    JsonObject object = new JsonObject();

                    codec1.encode(object, getter1.apply(struct)).orElseThrow();
                    codec2.encode(object, getter2.apply(struct)).orElseThrow();

                    return CodecResult.success(object);
                }
        );
    }

    public static <Struct, P1, P2, P3> RepoCodec<Struct> of(
            Function3<P1, P2, P3, Struct> factory,
            StructFieldRepoCodec<P1> codec1, Function1<Struct, P1> getter1,
            StructFieldRepoCodec<P2> codec2, Function1<Struct, P2> getter2,
            StructFieldRepoCodec<P3> codec3, Function1<Struct, P3> getter3
    ) {
        return create(
                object -> {
                    var p1 = codec1.decode(object).orElseThrow();
                    var p2 = codec2.decode(object).orElseThrow();
                    var p3 = codec3.decode(object).orElseThrow();

                    return CodecResult.success(factory.apply(p1, p2, p3));
                },
                struct -> {
                    JsonObject object = new JsonObject();

                    codec1.encode(object, getter1.apply(struct)).orElseThrow();
                    codec2.encode(object, getter2.apply(struct)).orElseThrow();
                    codec3.encode(object, getter3.apply(struct)).orElseThrow();

                    return CodecResult.success(object);
                }
        );
    }

    public static <Struct, P1, P2, P3, P4> RepoCodec<Struct> of(
            Function4<P1, P2, P3, P4, Struct> factory,
            StructFieldRepoCodec<P1> codec1, Function1<Struct, P1> getter1,
            StructFieldRepoCodec<P2> codec2, Function1<Struct, P2> getter2,
            StructFieldRepoCodec<P3> codec3, Function1<Struct, P3> getter3,
            StructFieldRepoCodec<P4> codec4, Function1<Struct, P4> getter4
    ) {
        return create(
                object -> {
                    var p1 = codec1.decode(object).orElseThrow();
                    var p2 = codec2.decode(object).orElseThrow();
                    var p3 = codec3.decode(object).orElseThrow();
                    var p4 = codec4.decode(object).orElseThrow();

                    return CodecResult.success(factory.apply(p1, p2, p3, p4));
                },
                struct -> {
                    JsonObject object = new JsonObject();

                    codec1.encode(object, getter1.apply(struct)).orElseThrow();
                    codec2.encode(object, getter2.apply(struct)).orElseThrow();
                    codec3.encode(object, getter3.apply(struct)).orElseThrow();
                    codec4.encode(object, getter4.apply(struct)).orElseThrow();

                    return CodecResult.success(object);
                }
        );
    }
}
