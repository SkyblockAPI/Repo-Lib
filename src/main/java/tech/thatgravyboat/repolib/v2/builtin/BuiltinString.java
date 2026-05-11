package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.List;
import java.util.Locale;

public class BuiltinString {


    public static final KeyValue STRING = new Constants(builder -> {
        builder.function("concat", function -> {
            function.vararg(true);
            function.executeSimple(args -> {

                var string = new StringBuilder();

                for (var arg : args) {
                    string.append(BuiltinObjects.toString(arg));
                }

                return new Str(string.toString());
            });
        });

        builder.function("uppercase", function -> {
            function.arity(1);
            function.execute(BuiltinString::uppercase);
        });

        builder.function("lowercase", function -> {
            function.arity(1);
            function.execute(BuiltinString::lowercase);
        });
    });

    private static Value uppercase(Evaluator evaluator, List<Value> args) {
        return new Str(evaluator.getStringOrThrow(args.getFirst()).toUpperCase(Locale.ROOT));
    }
    private static Value lowercase(Evaluator evaluator, List<Value> args) {
        return new Str(evaluator.getStringOrThrow(args.getFirst()).toUpperCase(Locale.ROOT));
    }
}
