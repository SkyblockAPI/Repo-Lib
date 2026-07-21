package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StructExpression;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class ModuleFile implements SelfEvaluatingExpression {
    private final RepoLoader loader;
    private final Expression script;
    private final KeyValue staticData;

    public ModuleFile(RepoLoader loader, StructExpression staticData, Expression script) {
        this.loader = loader;
        this.script = script;
        if (staticData == null) {
            this.staticData = ImmutableStructValue.EMPTY;
        } else {
            var data =  ((StructValue) Evaluator.CONSTANT.eval0(staticData));
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
    public Value evaluate(Evaluator evaluator) {
        return evaluator.eval0(script);
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return this.script.canReturnValueBeReturned();
    }
}

