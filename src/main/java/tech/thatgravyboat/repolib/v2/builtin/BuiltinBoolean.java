package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class BuiltinBoolean {

    public static final KeyValue BOOLEAN = new Constants(builder -> {
        builder.function("and", function -> {
            function.arity(2);
            function.execute(BuiltinBoolean::and);
        });
        builder.function("or", function -> {
            function.arity(2);
            function.execute(BuiltinBoolean::or);
        });
    });

    private static Value wrap(boolean bool) {
        return bool ? BoolValue.TRUE : BoolValue.FALSE;
    }

    private static Value and(Evaluator evaluator, List<Value> values) {
        var first = evaluator.getBooleanOrThrow(values.getFirst());
        var second = evaluator.getBooleanOrThrow(values.get(1));

        return wrap(first && second);
    }

    private static Value or(Evaluator evaluator, List<Value> values) {
        var first = evaluator.getBooleanOrThrow(values.getFirst());
        var second = evaluator.getBooleanOrThrow(values.get(1));

        return wrap(first || second);
    }


}
