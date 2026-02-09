package tech.thatgravyboat.repolib.v2.api.components;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

import java.util.Optional;
import java.util.Set;

public record SimpleRepoDataComponent<Type>(
        String name,
        Set<SkyBlockIdType> allowedTypes,
        RepoCodec<Type> codec
) implements BaseRepoDataComponent<Type> {

    @Override
    public CodecResult<Type> decode(JsonElement element) {
        return codec.decode(element);
    }

    @Override
    public CodecResult<JsonElement> encode(Type value) {
        return codec.encode(value);
    }
}
