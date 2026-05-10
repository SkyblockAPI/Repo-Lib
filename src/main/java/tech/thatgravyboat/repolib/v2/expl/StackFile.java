package tech.thatgravyboat.repolib.v2.expl;

public final class StackFile implements Expression.SelfEvaluatingExpression {

    private final Expression script;
    private final Value.KeyValue meta;

    public StackFile(Expression meta, Expression script) {
        this.script = script;

        var struct = new Value.MutableStruct();
        struct.set("this", struct); // Should allow for access of the top level by also using "this" in the script.
        var evaluator = new Evaluator(struct);
        evaluator.evaluate(meta);
        this.meta = struct.toFullyImmutable();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        evaluateScript(evaluator);
        return Value.NIL;
    }

    public Evaluator createEvaluator(Value.KeyValue overrides) {
        var inputs = new Value.MutableStruct();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }
        inputs.set("meta", this.meta);

        return new Evaluator(inputs);
    }

    public Value.KeyValue evaluateScript(Evaluator evaluator) {
        evaluator.evaluate(script);

        return evaluator.defaults.get("stack") instanceof Value.KeyValue value ? value : Value.ImmutableStruct.EMPTY;
    }

    public Value.KeyValue evaluate(Value.KeyValue overrides) {
        return evaluateScript(createEvaluator(overrides));
    }
}
