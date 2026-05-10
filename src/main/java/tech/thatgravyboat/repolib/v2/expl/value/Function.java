package tech.thatgravyboat.repolib.v2.expl.value;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;

import java.util.List;

@FunctionalInterface
non-sealed public interface Function extends Value {

    Value apply(Evaluator evaluator, List<Value> args);
}
