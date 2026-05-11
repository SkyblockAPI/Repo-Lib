package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Locale;

public sealed interface Value permits Array, Bool, Function, KeyValue, Nil, Num, Str {

    static String prettyPrint(Value value, String prefix) {
        return switch (value) {
            case Array arr -> Array.prettyPrint(arr, prefix + " ");
            case Function ignored -> "[function]";
            case Struct s -> Struct.prettyPrint(s, prefix + " ");
            case KeyValue kv -> "[Object " + kv.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "]";
            default -> value.toString();
        };
    }

    Value NIL = new Nil();

}
