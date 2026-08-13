package tech.thatgravyboat.repolib.v2.expl.value;

public interface LambdaValue extends FunctionValue {

    boolean vararg();
    int arityMin();
    int arityMax();
}
