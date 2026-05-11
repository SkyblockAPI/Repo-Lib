package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.ContentInfo;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Struct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static tech.thatgravyboat.repolib.v2.builtin.Constants.Builder.*;

public record RepoInstance(
        RepoLoader loader,
        RepoConstants constants,
        RepoListConstants listConstants
) {

    public List<Struct> listStacks() {
        var constants = this.constants.toMutable();
        var stacks = new LinkedList<Struct>();
        constants.set("id", FunctionBuilder.create(function -> {
            function.arity(1);
            function.executeVoid((evaluator, args) -> {
                var data = args.getFirst();
                if (data instanceof Struct struct) {
                    stacks.add(struct);
                    return;
                }
                evaluator.error("'id' expected struct but got " + data);
            });

        }));
        var evaluator = new Evaluator(listConstants);
        evaluator.eval(loader.rootList());
        return stacks;
    }

    public RepoStackResult createStack(Struct data) {
        var constants = this.constants.toMutable();
        constants.set("data", data);
        var evaluator = new Evaluator(constants);
        evaluator.eval(loader.rootFile());
        var file = evaluator.getStringOrNull(evaluator.getField("file"));
        if (file == null) {
            return null;
        }

        return createStack(file, data);
    }

    public RepoStackResult createStack(String id, Struct data) {
        var stackFile = loader.getStackFile(id);
        var evaluator = stackFile.createEvaluator(constants, data);
        var stack = stackFile.evaluateScript(evaluator);
        return new RepoStackResult(stack, evaluator.debugs, evaluator.errors);
    }

    public record RepoStackResult(
            Struct stack,
            List<ContentInfo> debug,
            List<ContentInfo> error
    ) {}

}
