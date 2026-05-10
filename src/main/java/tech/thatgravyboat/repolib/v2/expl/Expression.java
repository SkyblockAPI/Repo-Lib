package tech.thatgravyboat.repolib.v2.expl;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Expression {

    static StackFile parseFileOrThrow(String source) {
        return new Parser(source).parseFile();
    }

    static Expression parse(String source) {
        return new Parser(source).parseExpression();
    }

    record Num(double value) implements Expression {

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record Str(String value) implements Expression {

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }
    }

    record Bool(boolean value) implements Expression {

        @Override
        public String toString() {
            return Boolean.toString(value);
        }
    }

    record Struct(Map<String, Expression> fields) implements Expression {

        @Override
        public String toString() {
            return fields.entrySet()
                    .stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", ", "{", "}"));
        }
    }

    record Unary(Op op, Expression rhs) implements Expression {
        @Override
        public @NotNull String toString() {
            return switch (op) {
                case NEGATE -> "-" + rhs;
                case NOT -> "!" + rhs;
            };
        }

        public enum Op {
            NEGATE, NOT
        }
    }

    record If(Expression cond, Expression thenExpr, @Nullable Expression elseExpr) implements Expression {

        @Override
        public @NotNull String toString() {
            if (elseExpr == null) {
                return String.format("if (%s) %s", cond, thenExpr);
            }
            return String.format("if (%s) %s else %s", cond, thenExpr, elseExpr);
        }
    }

    record Access(@Nullable Expression lhs, String field) implements Expression {

        @Override
        public @NotNull String toString() {
            return lhs + "." + field;
        }
    }

    record Call(Expression lhs, List<Expression> args) implements Expression {

        @Override
        public @NotNull String toString() {
            return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    record Assign(Expression.Access lhs, Expression value) implements Expression {

        @Override
        public @NotNull String toString() {
            return lhs + " = " + value;
        }
    }

    record Block(List<Expression> exprs) implements Expression {

        @Override
        public @NotNull String toString() {
            if (exprs.isEmpty()) {
                return "{}";
            }
            return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
        }
    }

    non-sealed interface SelfEvaluatingExpression extends Expression {
        Value evaluate(Evaluator evaluator);
    }


    record In(Access holder, String field) implements Expression {
    }
}
