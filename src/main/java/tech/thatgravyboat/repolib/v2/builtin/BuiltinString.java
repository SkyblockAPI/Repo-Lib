package tech.thatgravyboat.repolib.v2.builtin;

import java.text.CompactNumberFormat;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

                return new StrValue(string.toString());
            });
        });

        builder.function("eqIc", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = evaluator.getStringOrThrow(values.getFirst());
                var second = evaluator.getStringOrThrow(values.get(1));

                return BoolValue.wrap(first.equalsIgnoreCase(second));
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

        builder.function("snake_case", function -> {
            function.arity(1);
            function.execute(BuiltinString::snake_case);
        });

        builder.function("formatted", function -> {
            function.arity(1);
            var format = new DecimalFormat("+#.####;-#.####", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
        builder.function("percentage", function -> {
            function.arity(1);
            var format = new DecimalFormat("+#.####%;-#.####%", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
        builder.function("uFormatted", function -> {
            function.arity(1);
            var format = new DecimalFormat("#.####;#.####", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });

        builder.function("formattedI", function -> {
            function.arity(1);
            var format = new DecimalFormat("+#;-#", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
        builder.function("percentageI", function -> {
            function.arity(1);
            var format = new DecimalFormat("+#%;-#%", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
        builder.function("uFormattedI", function -> {
            function.arity(1);
            var format = new DecimalFormat("#;#", DecimalFormatSymbols.getInstance(Locale.ROOT));
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
        builder.function("compact", function -> {
            function.arity(1, 2);
            var format = new CompactNumberFormat(
                "#;#",
                DecimalFormatSymbols.getInstance(Locale.ROOT),
                new String[]{
                    "", "", "",
                    "0K", "00K", "000K",
                    "0M", "00M", "000M",
                    "0B", "00B", "000B",
                    "0T", "00T", "000T"
                });
            function.execute((evaluator, values) -> {
                var arg = values.getFirst();
                int digits;
                if (values.size() == 2) {
                    digits = (int) evaluator.getNumberOrThrow(values.get(1));
                } else {
                    digits = 1;
                }

                format.setMinimumFractionDigits(digits);
                format.setMaximumFractionDigits(digits);
                return new StrValue(format.format(evaluator.getNumberOrThrow(arg)));
            });
        });
    });

    private static Value uppercase(Evaluator evaluator, List<Value> args) {
        return new StrValue(evaluator.getStringOrThrow(args.getFirst()).toUpperCase(Locale.ROOT));
    }
    private static Value lowercase(Evaluator evaluator, List<Value> args) {
        return new StrValue(evaluator.getStringOrThrow(args.getFirst()).toLowerCase(Locale.ROOT));
    }
    private static Value snake_case(Evaluator evaluator, List<Value> args) {
        return new StrValue(evaluator.getStringOrThrow(args.getFirst()).toLowerCase(Locale.ROOT).replace(" ", "_"));
    }
}
