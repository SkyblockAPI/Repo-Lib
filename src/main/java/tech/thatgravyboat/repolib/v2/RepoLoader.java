package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RepoLoader {

    List<LoadingErrors> load() throws IOException;
    Map<String, FunctionValueFile<?>> files();
    ModuleFile rootFile();
    Expression<?> rootList();
    Map<String, StackFile> stackFiles();
    RepoInstance create();
    StackFile stackFile(String name);
    FunctionValue module(String name);
    Collection<String> modules();
    Evaluator createEvaluator();

    RepoBundle bundle();
}
