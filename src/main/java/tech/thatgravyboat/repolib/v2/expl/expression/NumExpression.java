package tech.thatgravyboat.repolib.v2.expl.expression;

public record NumExpression(double value) implements Expression {

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
