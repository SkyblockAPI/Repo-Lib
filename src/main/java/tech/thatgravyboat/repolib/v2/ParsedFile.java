package tech.thatgravyboat.repolib.v2;

public final class ParsedFile {

    private final Expression script;
    private final Value.KeyValue meta;

    public ParsedFile(Expression meta, Expression script) {
        this.script = script;

        var struct = new Value.MutableStruct();
        struct.set("this", struct); // Should allow for access of the top level by also using "this" in the script.
        var evaluator = new Evaluator(struct);
        evaluator.evaluate(meta);
        this.meta = struct.toImmutable();
    }

    public Value.KeyValue evaluate(Value.KeyValue overrides) {
        var inputs = new Value.MutableStruct();
        var meta = new Value.MutableStruct();

        for (var entry : this.meta) {
            meta.set(entry.getKey(), entry.getValue());
        }
        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }
        inputs.set("meta", meta.toImmutable());

        var evaluator = new Evaluator(inputs);
        evaluator.evaluate(script);

        return inputs.get("stack") instanceof Value.KeyValue value ? value : Value.ImmutableStruct.EMPTY;
    }
}
