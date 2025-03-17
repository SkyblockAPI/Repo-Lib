package tech.thatgravyboat.repolib.internal;

@FunctionalInterface
public interface ThrowingBiFunction<A, B, R> {

    R apply(A a, B b) throws Exception;
}
