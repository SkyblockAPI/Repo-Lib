package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;

public sealed interface Value extends Comparable<Value>, SelfEvaluatingExpression
    permits ArrayValue, BoolValue, FunctionValue, KeyValue, NilValue, NumValue, StrValue {

    static String prettyPrint(Value value, String prefix) {
        return switch (value) {
            case ArrayValue arr -> ArrayValue.prettyPrint(arr, prefix + " ");
            case FunctionValue ignored -> "[function]";
            case StructValue s -> StructValue.prettyPrint(s, prefix + " ");
            case KeyValue kv -> "[Object " + kv.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "]";
            default -> value.toString();
        };
    }

    String type();

    Value NIL = new NilValue();

    @Override
    default int compareTo(@NotNull Value o) {
        return 0;
    }

    @Override
    default Value evaluate(Evaluator evaluator) {
        return this;
    }
}
