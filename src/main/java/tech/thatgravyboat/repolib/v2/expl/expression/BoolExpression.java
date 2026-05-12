package tech.thatgravyboat.repolib.v2.expl.expression;

public record BoolExpression(boolean value) implements Expression {

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
