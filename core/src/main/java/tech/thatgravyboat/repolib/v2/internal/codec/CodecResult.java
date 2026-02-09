package tech.thatgravyboat.repolib.v2.internal.codec;

import com.sun.jdi.Value;
import tech.thatgravyboat.repolib.internal.Utils;
import tech.thatgravyboat.repolib.v2.internal.ThrowingFunction;

import java.util.Objects;

public sealed interface CodecResult<Result> {

    default <MappedResult> CodecResult<MappedResult> flatMap(ThrowingFunction<Result, CodecResult<MappedResult>> mapper) {
        return switch (this) {
            case Success<Result> success -> {
                try {
                    yield mapper.apply(success.value());
                } catch (Exception e) {
                    yield CodecResult.failure(e.getMessage());
                }
            }
            case Failure<Result> failure -> CodecResult.failure(failure.error());
        };
    }

    default <MappedResult> CodecResult<MappedResult> map(ThrowingFunction<Result, MappedResult> mapper) {
        return switch (this) {
            case Success<Result> success -> {
                try {
                    yield CodecResult.success(mapper.apply(success.value()));
                } catch (Exception e) {
                    yield CodecResult.failure(e.getMessage());
                }
            }
            case Failure<Result> failure -> CodecResult.failure(failure.error());
        };
    }

    default Result orElseThrow() {
        return switch (this) {
            case Success<Result> success -> success.value();
            case Failure<Result> failure -> throw new ResultException(failure);
        };
    }

    default <Target> CodecResult<Target> into() {
        try {
            return switch (this) {
                case Success<Result> s -> success(Utils.unsafe(s.value));
                case Failure<Result> r -> failure(r.error);
            };
        } catch (ClassCastException e) {
            return failure(e.getMessage());
        }
    }

    default boolean isSuccess() {
        return this instanceof CodecResult.Success<Result>;
    }

    default boolean isFailure() {
        return this instanceof CodecResult.Failure<Result>;
    }

    record Success<Result>(Result value) implements CodecResult<Result> { }
    record Failure<Result>(String error) implements CodecResult<Result> { }

    static <Result> CodecResult<Result> success(Result value) {
        return new Success<>(value);
    }

    static <Result> CodecResult<Result> failure(String error) {
        return new Failure<>(error);
    }

    class ResultException extends RuntimeException {

        private final CodecResult.Failure<?> result;

        public ResultException(CodecResult.Failure<?> result) {
            super(result.error());
            this.result = result;
        }

        public CodecResult.Failure<?> result() {
            return result;
        }
    }
}
