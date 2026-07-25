package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.List;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;

public interface StructuredFunctionValue extends FunctionValue {

    Value apply(Evaluator evaluator, StructValue structValue);
    Value apply(Evaluator evaluator, List<Value> args);
}
