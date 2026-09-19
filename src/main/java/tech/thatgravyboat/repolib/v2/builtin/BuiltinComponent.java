package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinComponent {

    private static final String[] thousandsPlace = new String[]{"", "M", "MM", "MMM"};
    private static final String[] hundredsPlace = new String[]{"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] tensPlace = new String[]{"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] onesPlace = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    public static final Constants COMPONENT = new Constants(builder -> {
        builder.function(
                "join", function -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {
                        var components = MutableArrayValue.create();
                        values.forEach(components::add);
                        return components;
                    }));
                });

        builder.function(
                "asString", function -> {
                    function.arity(1);
                    function.execute((evaluator, values) -> {
                                var stringBuilder = new StringBuilder();
                                asString(evaluator, values.getFirst(), stringBuilder);

                                return new StrValue(stringBuilder.toString());
                            }
                    );
                });

        builder.function(
                "asRoman", function -> {
                    function.arity(1);
                    function.execute((evaluator, values) -> {
                        var first = evaluator.getNumberOrThrow(values.getFirst());
                        return new StrValue(toRomanNumeral((int) first));
                    });
                });

        builder.function(
                "splitToLength", function -> {
                    function.arity(2);
                    function.execute((evaluator, values) -> splitToLength(evaluator, values.getFirst(), (int) evaluator.getNumberOrThrow(values.get(1))));
                });

        builder.function(
                "asComponent", function -> {
                    function.arity(1);
                    function.execute((evaluator, values) -> parseComponent(evaluator, values.getFirst()));
                });
    });

    private static StructValue parseComponent(Evaluator evaluator, Value value) {
        return switch (value) {
            case ArrayValue array -> new MutableStructValue(new HashMap<>(Map.of("extra", array)));
            case StructValue kv -> kv;
            case StrValue literal -> new MutableStructValue(new HashMap<>(Map.of("text", literal)));
            default -> evaluator.panic("Invalid component type " + value.type());
        };
    }

    private static ArrayValue splitToLength(Evaluator evaluator, Value value, int maxLength) {
        if (value instanceof ArrayValue array) {
            var result = MutableArrayValue.create();

            for (var line : array) {
                for (var lines : splitToLength(evaluator, line, maxLength)) {
                    result.add(lines);
                }
            }

            return result;
        }
        var text = parseComponent(evaluator, value);

        var accumulator = new ArrayList<StructValue>();
        extractSections(evaluator, text, accumulator, new MutableStructValue());

        var result = MutableArrayValue.create();
        var current = MutableArrayValue.create();
        var currentLength = 0;

        for (var entry : accumulator) {
            var literal = evaluator.getStringOrThrow(entry.get("text"));

            if (currentLength + literal.length() >= maxLength && !literal.isBlank()) {
                result.add(new MutableStructValue(new HashMap<>(Map.of("extra", current))));
                current = MutableArrayValue.create();
                currentLength = 0;
            }
            currentLength += literal.length();
            current.add(entry);
        }

        result.add(new MutableStructValue(new HashMap<>(Map.of("extra", current))));
        return result;
    }

    private static <T extends StructValue & KeyValue.Mutable> void extractSections(
            Evaluator evaluator,
            StructValue text,
            List<StructValue> accumulator,
            T parent
    ) {
        text.forEach(entry -> {
            if (entry.getKey().equals("text") || entry.getKey().equals("extra")) {
                return;
            }
            parent.set(entry.getKey(), entry.getValue());
        });

        if (text.contains("text")) {
            var literalText = evaluator.getStringOrThrow(text.get("text"));
            if (text.get("do_not_split") == BoolValue.TRUE) {
                var entry = new MutableStructValue(parent);
                entry.set("text", new StrValue(literalText));
                accumulator.add(entry);
            } else {
                var textLength = literalText.length();
                var consumed = 0;
                do {
                    var next = literalText.indexOf(' ', consumed);
                    final String span;
                    if (next == -1) {
                        span = literalText.substring(consumed);
                        consumed = textLength;
                    } else {
                        span = literalText.substring(consumed, next + 1);
                        consumed = next + 1;
                    }

                    var entry = new MutableStructValue(parent);
                    entry.set("text", new StrValue(span));
                    accumulator.add(entry);
                } while (consumed < textLength);
            }
        }

        if (text.contains("extra")) {
            var extra = evaluator.getArrayOrThrow(text.get("extra"));
            for (var value : extra) {
                var extraParent = new MutableStructValue(parent);
                extractSections(evaluator, parseComponent(evaluator, value), accumulator, extraParent);
            }
        }
    }

    private static void asString(Evaluator evaluator, Value values, StringBuilder stringBuilder) {
        if (values instanceof KeyValue kv) {
            if (kv.contains("text")) {
                stringBuilder.append(evaluator.getStringOrThrow(kv.get("text")));
            }

            if (kv.contains("extra")) {
                var extra = evaluator.getArrayOrThrow(kv.get("extra"));
                for (var value : extra) {
                    asString(evaluator, value, stringBuilder);
                }
            }
        } else if (values instanceof StrValue(String value)) {
            stringBuilder.append(value);
        } else {
            evaluator.panic("Failed to read " + values + " as a component.");
        }
    }

    private static String toRomanNumeral(int number) {
        if (number >= 4000) return "TOO_HIGH_NUMBER";
        if (number == 0) return "O";
        if (number < 0) return "TOO_LOW_NUMBER";
        return thousandsPlace[number / 1000] + hundredsPlace[number % 1000 / 100] + tensPlace[number % 100 / 10] + onesPlace[number % 10];
    }
}
