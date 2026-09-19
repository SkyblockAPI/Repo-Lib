package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;

public class LambdaFunctionValue implements LambdaValue {
    private final BiFunction<Evaluator, List<Value>, Value> executor;
    private final boolean vararg;
    private final int arityMin;
    private final int arityMax;

    public LambdaFunctionValue(
            BiFunction<Evaluator, List<Value>, Value> executor,
            boolean vararg,
            int arityMin,
            int arityMax
    ) {
        this.executor = executor;
        this.vararg = vararg;
        this.arityMin = arityMin;
        this.arityMax = arityMax;
    }

    @Override
    public Value apply(Evaluator evaluator, List<Value> args) {
        return executor.apply(evaluator, args);
    }

    public BiFunction<Evaluator, List<Value>, Value> executor() {
        return executor;
    }

    @Override
    public boolean vararg() {
        return vararg;
    }

    @Override
    public int arityMin() {
        return arityMin;
    }

    @Override
    public int arityMax() {
        return arityMax;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LambdaFunctionValue) obj;
        return Objects.equals(this.executor, that.executor) &&
                this.vararg == that.vararg &&
                this.arityMin == that.arityMin &&
                this.arityMax == that.arityMax;
    }

    @Override
    public int hashCode() {
        return Objects.hash(executor, vararg, arityMin, arityMax);
    }

    @Override
    public String toString() {
        return "LambdaFunctionValue[" +
                "executor=" + executor + ", " +
                "vararg=" + vararg + ", " +
                "arityMin=" + arityMin + ", " +
                "arityMax=" + arityMax + ']';
    }

}
