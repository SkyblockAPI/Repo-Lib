package tech.thatgravyboat.repolib.v2.expl;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
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

        Expression include = new Parser("""
        print("hi from inclusion");
        
        (1 + 1)
        """).parseExpression();


        var evaluator = getEvaluator(math, include);
        var expression = new Parser("""
        value = if (true) {
            "meow";
        }
        
        print(value);
        
        meow = include();
        
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

    private static @NotNull Evaluator getEvaluator(Constants math, Expression include) {
        var root = new Constants(builder -> {
            builder.field("Math", math);
            builder.field("temp", new MutableStructValue());
            builder.function("include", function -> {
                function.arity(0);
                function.executeArgless(evaluator -> evaluator.evaluate(include));
            } );
            builder.function("print", function -> {
                function.arity(1);
                function.executeSimpleVoid(values -> System.out.println(values.getFirst()));
            });
        });


        return new Evaluator(new MutableStructValue(root));
    }

}
