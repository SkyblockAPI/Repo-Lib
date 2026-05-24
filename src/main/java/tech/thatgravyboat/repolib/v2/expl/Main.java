package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;

public class Main {

    public static void main(String[] args) {
        var math = new Constants(builder -> {
            builder.function("add", function -> {
                function.arity(2);
                function.execute((eval, values) -> {
                    var first = eval.getNumberOrThrow(values.get(0));
                    var second = eval.getNumberOrThrow(values.get(1));
                    return new NumValue(first + second);
                });
            });
            builder.function("lessThan", function -> {
                function.arity(2);
                function.execute((eval, values) -> {
                    var first = eval.getNumberOrThrow(values.get(0));
                    var second = eval.getNumberOrThrow(values.get(1));
                    return new BoolValue(first < second);
                });
            });
            builder.function("equals", function -> {
                function.arity(2);
                function.execute((eval, values) -> {
                    var first = eval.getNumberOrThrow(values.get(0));
                    var second = eval.getNumberOrThrow(values.get(1));
                    return new BoolValue(first == second);
                });
            });
        });
        var root = new Constants(builder -> {
            builder.field("Math", math);
            builder.field("temp", new MutableStructValue());
            builder.function("print", function -> {
                function.arity(1);
                function.executeSimpleVoid(values -> System.out.println(values.getFirst()));
            });
        });

        var evaluator = new Evaluator(root);
        var expression = new Parser("""
        print("meow" + " mrow" + " mrrp");
        print(2>..<(2+2));
        """).parseExpression();

        evaluator.evaluate(expression);
    }

}
