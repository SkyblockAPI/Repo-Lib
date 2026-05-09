package tech.thatgravyboat.repolib.v2;


import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface Expression {

    static Expression parseOrThrow(String source) {
        return parseOrThrow(source, false);
    }

    static Expression parseOrThrow(String source, boolean multiline) {
        return new Parser(source).parse();
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

    record Ident(String value) implements Expression {

        @Override
        public String toString() {
            return this.value;
        }
    }

    record Unary(Op op, Expression rhs) implements Expression {
        public enum Op {
            NEGATE, NOT
        }

        @Override
        public String toString() {
            return switch (op) {
                case NEGATE -> "-" + rhs;
                case NOT -> "!" + rhs;
            };
        }
    }

    record Ternary(Expression cond,
                   Expression thenExpr,
                   Expression elseExpr
    ) implements Expression {

        @Override
        public String toString() {
            return String.format("%s ? %s : %s", cond, thenExpr, elseExpr);
        }
    }

    record Access(@Nullable Expression lhs, Expression field) implements Expression {

        @Override
        public String toString() {
            return lhs + "." + field;
        }
    }

    record Call(Expression lhs, List<Expression> args) implements Expression {

        @Override
        public String toString() {
            return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    record Assign(Expression lhs, Expression value) implements Expression {

        @Override
        public String toString() {
            return lhs + " = " + value;
        }
    }

    record Block(List<Expression> exprs) implements Expression {

        @Override
        public String toString() {
            if (exprs.isEmpty()) {
                return "{}";
            }
            return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
        }
    }

}
