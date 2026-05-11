package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArray;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Struct;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinObjects {

    public static final KeyValue OBJECTS = new Constants(builder -> {

        builder.function("entries", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst();
                if (first instanceof Struct struct) {
                    var array = MutableArray.create();

                    for (var entry : struct) {
                        var entryStruct = new MutableStruct();

                        entryStruct.set("key", new Str(entry.getKey()));
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
