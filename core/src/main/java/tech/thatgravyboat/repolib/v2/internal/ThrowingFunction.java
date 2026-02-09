package tech.thatgravyboat.repolib.v2.internal;

@FunctionalInterface
public interface ThrowingFunction<A, R> {

    R apply(A a) throws Exception;
}
