package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class StackFile implements Expression.SelfEvaluatingExpression {

    private final Expression script;
    private final KeyValue meta;

    public StackFile(Expression meta, Expression script) {
        this.script = script;

        var struct = new MutableStruct();
        //struct.set("this", struct); // Should allow for access of the top level by also using "this" in the script.
        var evaluator = new Evaluator(struct);
        evaluator.evaluate(meta);
        this.meta = struct.toFullyImmutable();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        evaluateScript(evaluator);
        return Value.NIL;
    }

    public Evaluator createEvaluator(KeyValue overrides) {
        return createEvaluator(overrides, ImmutableStruct.EMPTY);
    }

    public Evaluator createEvaluator(KeyValue overrides, KeyValue data) {
        var inputs = new MutableStruct();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }
        inputs.set("data", data);
        inputs.set("meta", this.meta);

        return new Evaluator(inputs);
    }

    public KeyValue evaluateScript(Evaluator evaluator) {
        evaluator.evaluate(script);

        return evaluator.defaults.get("stack") instanceof KeyValue value ? value : ImmutableStruct.EMPTY;
    }

    public KeyValue evaluate(KeyValue overrides) {
        return evaluateScript(createEvaluator(overrides));
    }
}
