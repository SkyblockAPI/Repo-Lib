package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Locale;

public sealed interface Value permits ArrayValue, BoolValue, FunctionValue, KeyValue, NilValue, NumValue, StrValue {

    static String prettyPrint(Value value, String prefix) {
        return switch (value) {
            case ArrayValue arr -> ArrayValue.prettyPrint(arr, prefix + " ");
            case FunctionValue ignored -> "[function]";
            case StructValue s -> StructValue.prettyPrint(s, prefix + " ");
            case KeyValue kv -> "[Object " + kv.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "]";
            default -> value.toString();
        };
    }

    Value NIL = new NilValue();

}
