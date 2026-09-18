import java.util.List;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.FolderLoader;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;

import java.io.IOException;
import java.nio.file.Path;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class Main {

    public static void main(String[] args) throws IOException {
        var repoLoader = new FolderLoader(Path.of("src/test/repo"));
        var errors = repoLoader.load();
        for (var error : errors) {
            System.out.println("Failed to load file " + error.file() + " due to " + error.reason());
            error.reason().printStackTrace();
        }
        var file = repoLoader.module("silly");

        var constants = new RepoConstants(repoLoader);
        var evaluator = new Evaluator(constants, repoLoader::module);

        var res = file.apply(evaluator, List.of());

        System.out.println(Value.prettyPrint(res));
    }

}
