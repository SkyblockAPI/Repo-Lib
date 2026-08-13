import java.util.List;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;

import java.io.IOException;
import java.nio.file.Path;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class Main {

    public static void main(String[] args) throws IOException {
        var repoLoader = new RepoLoader(Path.of("src/test/repo"));
        var errors = repoLoader.load();
        for (var error : errors) {
            System.out.println("Failed to load file " + error.file() + " due to " + error.reason());
            error.reason().printStackTrace();
        }
        var file = repoLoader.getModule("silly");

        var constants = new RepoConstants(repoLoader);
        var evaluator = new Evaluator(constants, repoLoader::getModule);

        var res = file.apply(evaluator, List.of());

        System.out.println(Value.prettyPrint(res));
    }

}
