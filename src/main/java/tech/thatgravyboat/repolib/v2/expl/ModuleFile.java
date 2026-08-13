package tech.thatgravyboat.repolib.v2.expl;

import java.util.List;
import java.util.Objects;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StructExpression;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class ModuleFile implements FunctionValue {
    private final String name;
    private final RepoLoader loader;
    private final Expression script;
    private final KeyValue staticData;

    public ModuleFile(String name, RepoLoader loader, Expression staticData, Expression script, Evaluator evaluator) {
        this.name = name;
        this.loader = loader;
        this.script = script;
        if (staticData == null) {
            this.staticData = ImmutableStructValue.EMPTY;
        } else {
            var data =  ((StructValue) Objects.requireNonNullElse(evaluator, Evaluator.CONSTANT).eval0(staticData));
            if (data instanceof KeyValue.Mutable mutable) {
                this.staticData = mutable.toFullyImmutable();
            } else {
                this.staticData = data;
            }
        }
    }

    public KeyValue getStaticData() {
        return staticData;
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return this.script.canReturnValueBeReturned();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        evaluator.setInOverlay("static_data", this.staticData);
        return evaluator.evaluate(script);
    }

    private Value evaluate0(Evaluator evaluator) {
        evaluator.setInOverlay("static_data", this.staticData);
        return evaluator.evaluate(script);
    }

    @Override
    public Value apply(Evaluator evaluator, List<Value> args) {
        if (args.size() == 1) {
            var scope = evaluator.getMutableStructOrThrow(args.getFirst());
            return evaluator.pushPop(name, scope, () -> this.evaluate0(evaluator));
        } else {
            return evaluator.pushPop(name, () -> this.evaluate0(evaluator));
        }
    }
}

