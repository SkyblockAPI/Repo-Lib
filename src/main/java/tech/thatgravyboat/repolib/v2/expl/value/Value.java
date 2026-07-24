package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;

public sealed interface Value extends Comparable<Value>, SelfEvaluatingExpression
    permits ArrayValue, BoolValue, FunctionValue, KeyValue, NilValue, NumValue, StrValue {

    static String prettyPrint(Value value) {
        return prettyPrint(value, "", 0);
    }

    static String prettyPrint(Value value, String prefix, int depth) {
        return switch (value) {
            case ArrayValue arr when depth < 10 -> ArrayValue.prettyPrint(arr, prefix + " ", depth);
            case ArrayValue ignored -> "[Array]";
            case LambdaFunctionValue lambda -> "[function arityMin=%s arityMax=%s vararg=%s]".formatted(lambda.arityMin(), lambda.arityMax(), lambda.vararg());
            case FunctionValue ignored -> "[function]";
            case StructValue s when depth < 10 -> StructValue.prettyPrint(s, prefix + " ", depth);
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
