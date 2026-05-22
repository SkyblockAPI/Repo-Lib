package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinMath {

    public static final Constants MATH = new Constants(builder -> {

        builder.function("gt", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new BoolValue(first > second);
            });
        });

        builder.function("gte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new BoolValue(first >= second);
            });
        });

        builder.function("lt", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new BoolValue(first < second);
            });
        });

        builder.function("lte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new BoolValue(first <= second);
            });
        });

        builder.function("add", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double sum = 0;

                for (var value : values) {
                    if (value instanceof ArrayValue array) {
                        for (var arrayValue : array) {
                            sum += evaluator.getNumberOrThrow(arrayValue);
                        }
                    } else {
                        sum += evaluator.getNumberOrThrow(value);
                    }
                }

                return new NumValue(sum);
            });
        });

        builder.function("sub", function -> {
            function.vararg(true);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));

                return new NumValue(first - second);
            });
        });

        builder.function("pow", function -> {
            function.vararg(true);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));

                return new NumValue(Math.pow(first, second));
            });
        });

        builder.function("min", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double min = 0;

                for (var value : values) {
                    if (value instanceof ArrayValue array) {
                        for (var arrayValue : array) {
                            min = Math.min(evaluator.getNumberOrThrow(arrayValue), min);
                        }
                    } else {
                        min = Math.min(evaluator.getNumberOrThrow(value), min);
                    }
                }

                return new NumValue(min);
            });
        });

        builder.function("max", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double min = 0;

                for (var value : ArrayValue.flatten(values)) {
                    min = Math.max(evaluator.getNumberOrThrow(value), min);
                }

                return new NumValue(min);
            });
        });

        builder.function("abs", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                return new NumValue(Math.abs(evaluator.getNumberOrThrow(values.getFirst())));
            });
        });

        builder.function("mul", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));
                return new NumValue(first * second);
            });
        });

        builder.function("div", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));
                if (second == 0) {
                    return evaluator.panic("Can't divide by 0!");
                }
                return new NumValue(first / second);
            });
        });

        builder.function("clamp", function -> {
            function.arity(3);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));
                var third = evaluator.getNumberOrThrow(values.get(2));

                return new NumValue(Math.clamp(first, second, third));
            });
        });

        builder.constant("PI", Math.PI);
        builder.constant("E", Math.E);


    });
}
