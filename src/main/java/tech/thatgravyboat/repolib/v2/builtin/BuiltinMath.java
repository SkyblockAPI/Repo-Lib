package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.expl.value.Array;
import tech.thatgravyboat.repolib.v2.expl.value.Bool;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.Num;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinMath {

    public static final Constants MATH = new Constants(builder -> {

        builder.function("gt", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new Bool(first > second);
            });
        });

        builder.function("gte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new Bool(first >= second);
            });
        });

        builder.function("lt", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new Bool(first < second);
            });
        });

        builder.function("lte", function -> {
            function.arity(2);
            function.execute((evaluator, args) -> {
                var first = evaluator.getNumberOrThrow(args.getFirst());
                var second = evaluator.getNumberOrThrow(args.get(1));

                return new Bool(first <= second);
            });
        });

        builder.function("add", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double sum = 0;

                for (var value : values) {
                    if (value instanceof Array array) {
                        for (var arrayValue : array) {
                            sum += evaluator.getNumberOrThrow(arrayValue);
                        }
                    } else {
                        sum += evaluator.getNumberOrThrow(value);
                    }
                }

                return new Num(sum);
            });
        });

        builder.function("min", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double min = 0;

                for (var value : values) {
                    if (value instanceof Array array) {
                        for (var arrayValue : array) {
                            min = Math.min(evaluator.getNumberOrThrow(arrayValue), min);
                        }
                    } else {
                        min = Math.min(evaluator.getNumberOrThrow(value), min);
                    }
                }

                return new Num(min);
            });
        });

        builder.function("max", function -> {
            function.vararg(true);
            function.arity(1);
            function.execute((evaluator, values) -> {
                double min = 0;

                for (var value : Array.flatten(values)) {
                    min = Math.max(evaluator.getNumberOrThrow(value), min);
                }

                return new Num(min);
            });
        });

        builder.function("abs", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                return new Num(Math.abs(evaluator.getNumberOrThrow(values.getFirst())));
            });
        });

        builder.function("mul", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));
                return new Num(first * second);
            });
        });

        builder.function("div", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = evaluator.getNumberOrThrow(values.getFirst());
                var second = evaluator.getNumberOrThrow(values.get(1));
                if (second == 0) {
                    evaluator.panic("Can't divide by 0!");
                    return Value.NIL;
                }
                return new Num(first * second);
            });
        });

        builder.constant("PI", Math.PI);
        builder.constant("E", Math.E);


    });
}
