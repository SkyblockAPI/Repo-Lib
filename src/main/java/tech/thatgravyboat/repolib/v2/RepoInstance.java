package tech.thatgravyboat.repolib.v2;

import java.util.Objects;
import tech.thatgravyboat.repolib.v2.expl.ContentInfo;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;

import java.util.LinkedList;
import java.util.List;

import static tech.thatgravyboat.repolib.v2.builtin.Constants.Builder.FunctionBuilder;

public record RepoInstance(
        RepoLoader loader,
        RepoConstants constants,
        RepoListConstants listConstants
) {

    public List<StructValue> listStacks() {
        var constants = this.constants.toMutable();
        var stacks = new LinkedList<StructValue>();
        constants.set(
                "id", FunctionBuilder.create(function -> {
                    function.arity(1);
                    function.executeVoid((evaluator, args) -> {
                        var data = args.getFirst();
                        if (data instanceof StructValue struct) {
                            stacks.add(struct);
                            return;
                        }
                        evaluator.error("'id' expected struct but got " + data);
                    });

                }));
        var evaluator = new Evaluator(listConstants, this.loader::getModule);
        evaluator.evaluate(loader.rootList());
        return stacks;
    }

    public RepoStackResult createStack(StructValue data) {
        return this.createStack(data, null);
    }

    public RepoStackResult createStack(StructValue data, StructValue profile) {
        return this.createStack(data, profile, RepoConfig.DEFAULT);
    }

    public RepoStackResult createStack(StructValue data, StructValue profile, RepoConfig repoConfig) {
        var constants = this.constants.toMutableStruct();
        constants.set("data", data);
        constants.set("profile", Objects.requireNonNullElseGet(profile, this::getEmptyProfile));
        var evaluator = new Evaluator(constants, this.loader::getModule);
        evaluator.evaluate(loader.rootFile());
        var file = evaluator.getStringOrNull(evaluator.getField("file"));
        if (file == null) {
            return null;
        }

        return createStack(file, data, profile, repoConfig);
    }

    private StructValue getEmptyProfile() {
        return ImmutableStructValue.EMPTY;
    }

    public StructValue getMaxedProfile() {
        var profile = this.loader.getModule("profile");
        if (profile == null) {
            return getEmptyProfile();
        }

        var profileStruct = new MutableStructValue();

        var evaluator = new Evaluator(new LayeredStructValue(profileStruct, this.constants), this.loader::getModule);
        evaluator.evaluate(profile);
        return new ImmutableStructValue(profileStruct.fields());
    }

    public RepoStackResult createStack(String id, StructValue data) {
        return createStack(id, data, this.getEmptyProfile(), RepoConfig.DEFAULT);
    }
    public RepoStackResult createStack(String id, StructValue data, StructValue profile) {
        return createStack(id, data, profile, RepoConfig.DEFAULT);
    }

    public RepoStackResult createStack(String id, StructValue data, StructValue profile, RepoConfig repoConfig) {
        var stackFile = loader.getStackFile(id);
        var evaluator = stackFile.createEvaluator(constants, data, Objects.requireNonNullElseGet(profile, this::getEmptyProfile), repoConfig, this.loader::getModule);
        var stack = stackFile.evaluateScript(evaluator);
        return new RepoStackResult(stack, evaluator.debugs, evaluator.errors);
    }

    public record RepoStackResult(
            StructValue stack,
            List<ContentInfo> debug,
            List<ContentInfo> error
    ) {
    }
}
