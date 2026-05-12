package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinObjects {

    public static String toString(Value value) {
        return switch (value) {
            case StrValue str -> str.value();
            case NumValue(double num) -> num == (int) num ? Integer.toString((int) num) : Double.toString(num);
            default -> value.toString();
        };
    }

    public static final KeyValue OBJECTS = new Constants(builder -> {

        builder.function("toString", function -> {
            function.arity(1);
            function.executeSimple(args -> {
                var firstArg = args.getFirst();

                return switch (firstArg) {
                    case StrValue str -> str;
                    case NumValue num -> new StrValue(String.valueOf(num));
                    default -> new StrValue(firstArg.toString());
                };
            });
        });

        builder.function("entries", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst();
                if (first instanceof StructValue struct) {
                    var array = MutableArrayValue.create();

                    for (var entry : struct) {
                        var entryStruct = new MutableStructValue();

                        entryStruct.set("key", new StrValue(entry.getKey()));
                        entryStruct.set("value", entry.getValue());

                        array.add(entryStruct);
                    }
                    return array;
                }

                evaluator.panic("Can't call Objects.entries on non struct type.");
                return Value.NIL;
            });
        });

    });

}
