package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class BuiltinArray {



    public static final KeyValue ARRAY = new Constants(builder -> {

        builder.function("sorted", function -> {
            function.arity(1);
            function.execute((evaluator, args) -> {
                var array = evaluator.getArrayOrThrow(args.getFirst());
                var sorted = MutableArrayValue.create();
                StreamSupport.stream(array.spliterator(), false).sorted(Comparator.comparing(Function.identity())).forEach(sorted::add);

                return sorted;
            });
        });

    });

}
