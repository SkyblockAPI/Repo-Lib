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
        
        test = |a, b| {
           (a + b)
        };
        
        testArray = [1,2,3,4,5,6,7,8,9,0];
        
        testArray.mapIndexed(test).forEach(print);
        
        print(test(2, 3));
        
        """).parseExpression();


        evaluator.evaluate(expression);
        for (var error : evaluator.errors) {
            System.out.println(error);
        }
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
                function.vararg(true);
                function.arity(1);
                function.executeSimpleVoid(System.out::println);
            });
        });


        return new Evaluator(new MutableStructValue(root));
    }

}
