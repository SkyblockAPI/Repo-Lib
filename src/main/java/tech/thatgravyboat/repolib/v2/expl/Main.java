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

        var evaluator = new Evaluator(new MutableStructValue(root));
        var expression = new Parser("""
        if (true) {
            print("meow");
        }
        
        meow = 2;
        
        match (meow) {
            2 -> print("rawr");
            else -> print("purr");
        }
        
        for (test : [1,2,3,4,5,6,7,8,9,0]) {
            print(test);
        }
        
        print("meow2");
        """).parseExpression();

        evaluator.evaluate(expression);
    }

}
