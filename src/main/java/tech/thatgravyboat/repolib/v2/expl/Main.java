package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.Bool;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Num;

public class Main {

    public static void main(String[] args) {
        var math = new Constants(builder -> {
            builder.function("add", function -> {
                function.arity(2);
                function.execute((eval, values) -> {
                    var first = eval.getNumberOrThrow(values.get(0));
                    var second = eval.getNumberOrThrow(values.get(1));
                    return new Num(first + second);
                });
            });
            builder.function("lessThan", function -> {
                function.arity(2);
                function.execute((eval, values) -> {
                    var first = eval.getNumberOrThrow(values.get(0));
                    var second = eval.getNumberOrThrow(values.get(1));
                    return new Bool(first < second);
                });
            });
        });
        var root = new Constants(builder -> {
            builder.field("Math", math);
            builder.field("temp", new MutableStruct());
            builder.function("print", function -> {
                function.arity(1);
                function.executeSimpleVoid(values -> System.out.println(values.getFirst()));
            });
        });

        var evaluator = new Evaluator(root);
        var expression = new Parser("""
        # This is a comment
        for (temp.i = 0; Math.lessThan(temp.i, 10); temp.i = Math.add(temp.i, 1)) {
            print(temp.i);
        };
        """).parseExpression();

        evaluator.evaluate(expression);
    }

}
