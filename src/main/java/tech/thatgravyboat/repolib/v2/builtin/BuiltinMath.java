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
                var first = args.getFirst().asNumber();
                var second = args.get(1).asNumber();

                return BoolValue.wrap(first > second);
            });
        });

        builder.function("gte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = args.getFirst().asNumber();
                var second = args.get(1).asNumber();

                return BoolValue.wrap(first >= second);
            });
        });

        builder.function("lt", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = args.getFirst().asNumber();
                var second = args.get(1).asNumber();

                return BoolValue.wrap(first < second);
            });
        });

        builder.function("lte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = args.getFirst().asNumber();
                var second = args.get(1).asNumber();

                return BoolValue.wrap(first <= second);
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
                            sum += arrayValue.asNumber();
                        }
                    } else {
                        sum += value.asNumber();
                    }
                }

                return new NumValue(sum);
            });
        });

        builder.function("sub", function -> {
            function.vararg(true);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();
                var second = values.get(1).asNumber();

                return new NumValue(first - second);
            });
        });

        builder.function("pow", function -> {
            function.vararg(true);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();
                var second = values.get(1).asNumber();

                return new NumValue(Math.pow(first, second));
            });
        });

        builder.function("min", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                var flattened = ArrayValue.flatten(values);
                double min = flattened.getFirst().asNumber();

                for (var value : flattened) {
                    min = Math.min(value.asNumber(), min);
                }

                return new NumValue(min);
            });
        });

        builder.function("max", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                var flattened = ArrayValue.flatten(values);
                double min = flattened.getFirst().asNumber();

                for (var value : flattened) {
                    min = Math.max(value.asNumber(), min);
                }

                return new NumValue(min);
            });
        });

        builder.function("abs", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                return new NumValue(Math.abs(values.getFirst().asNumber()));
            });
        });

        builder.function("mul", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();
                var second = values.get(1).asNumber();
                return new NumValue(first * second);
            });
        });

        builder.function("div", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();
                var second = values.get(1).asNumber();
                if (second == 0) {
                    return evaluator.panic("Can't divide by 0!");
                }
                return new NumValue(first / second);
            });
        });

        builder.function("floor", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();

                return new NumValue(Math.floor(first));
            });
        });

        builder.function("ceil", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();

                return new NumValue(Math.ceil(first));
            });
        });

        builder.function("round", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();

                return new NumValue(Math.round(first));
            });
        });

        builder.function("clamp", function -> {
            function.arity(3);
            function.execute((evaluator, values) -> {
                var first = values.getFirst().asNumber();
                var second = values.get(1).asNumber();
                var third = values.get(2).asNumber();

                return new NumValue(Math.clamp(first, second, third));
            });
        });

        builder.constant("PI", Math.PI);
        builder.constant("E", Math.E);


    });
}
