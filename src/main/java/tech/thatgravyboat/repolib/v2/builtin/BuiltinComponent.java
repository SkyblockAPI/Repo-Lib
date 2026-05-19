package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinComponent {

    public static final Constants COMPONENT = new Constants(builder -> {
        builder.function(
                "join", function -> {
                    function.vararg(true);
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
    });
    private static final String[] thousandsPlace = new String[]{"", "M", "MM", "MMM"};
    private static final String[] hundredsPlace = new String[]{"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] tensPlace = new String[]{"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] onesPlace = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

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

    /**
     * @param subtractive This refers to if it should preform subtractions for numbers such as 4 if true it will be IV if false it will be IIII
     */
    private static String toRomanNumeral(int number) {
        return thousandsPlace[number / 1000] + hundredsPlace[number % 1000 / 100] + tensPlace[number % 100 / 10] + onesPlace[number % 10];
    }

}
