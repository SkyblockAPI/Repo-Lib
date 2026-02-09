package tech.thatgravyboat.repolib.v2.internal.codec.struct.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

public interface StructFieldRepoCodec<Type> extends RepoCodec<Type> {

    CodecResult<Type> decode(JsonObject object) throws CodecResult.ResultException;

    CodecResult<Void> encode(JsonObject object, Type value) throws CodecResult.ResultException;

    @Override
    default CodecResult<Type> decode(JsonElement element) {
        if (element instanceof JsonObject object) {
            try {
                return this.decode(object);
            } catch (CodecResult.ResultException e) {
                return CodecResult.failure(e.result().error());
            }
        }
        return CodecResult.failure("Expected an object");
    }

    @Override
    default CodecResult<JsonElement> encode(Type value) {
        try {
            JsonObject object = new JsonObject();
            return switch (this.encode(object, value)) {
                case CodecResult.Success<Void> ignored -> CodecResult.success(object);
                case CodecResult.Failure<Void> failure -> CodecResult.failure(failure.error());
            };
        } catch (CodecResult.ResultException e) {
            return CodecResult.failure(e.result().error());
        }
    }
}
