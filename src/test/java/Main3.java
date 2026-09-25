import com.google.gson.JsonParser;
import tech.thatgravyboat.repolib.v2.FolderLoader;
import tech.thatgravyboat.repolib.v2.RepoConfig;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class Main3 {
    public static void main(String[] args) throws IOException {
        Path repoPath = Path.of("Repo-Data").toRealPath().normalize().toAbsolutePath();
        RepoLoader loader = new FolderLoader(repoPath);
        var instance = loader.create();

        var errors = loader.load();

        errors.forEach(System.out::println);

        var data =
                JsonParser.parseString(Files.readString(Path.of("data.jsonc"), StandardCharsets.UTF_8)).getAsJsonObject();

        var constData = Constants.kvFromJson(data);

        var stackFile = Objects.requireNonNull(loader.stackFile("items/slayer/enderman/aspect_of_the_void"));

        {
            var evaluator = stackFile.createEvaluator(instance.constants(), constData, RepoConfig.DEFAULT, loader::module);
            var stack = stackFile.evaluateScript(evaluator);
            evaluator.errors.forEach(System.out::println);
            evaluator.debugs.forEach(System.out::println);
            System.out.println(stack);;
        }

        long uncompiledSum = 0;
        for (int i = 0; i < 100000; i++) {
            long noCompileStart = System.nanoTime();
            var noCompileEvaluator = stackFile.createEvaluator(instance.constants(), ImmutableStructValue.EMPTY, RepoConfig.DEFAULT, loader::module);
            var noCompileStack = stackFile.evaluateScript(noCompileEvaluator);
            uncompiledSum += System.nanoTime() - noCompileStart;
            System.out.print("Try " + i + " (avg " + uncompiledSum / ((i + 1) * 1000000.0) + "ms)\r");
        }

        System.out.println("Took " + (uncompiledSum / 100_000_000000.0) + "ms (uncompiled)");
    }
}
