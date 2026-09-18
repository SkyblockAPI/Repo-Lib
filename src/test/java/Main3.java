import com.google.gson.JsonParser;
import tech.thatgravyboat.repolib.v2.FolderLoader;
import tech.thatgravyboat.repolib.v2.RepoConfig;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.jvm.compiler.ExpressionCompiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Main3 {
    public static void main(String[] args) throws IOException {
        RepoLoader loader = new FolderLoader(Path.of("Repo-Data").toRealPath().normalize().toAbsolutePath());
        var instance = loader.create();

        ExpressionCompiler.registerSaver(((s, bytes) -> {
            try {
                Files.write(Path.of("output", s.replaceAll("[:]", "_") + ".class"), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        var errors = loader.load();

        errors.forEach(System.out::println);

        var data =
                JsonParser.parseString(Files.readString(Path.of("data.jsonc"), StandardCharsets.UTF_8)).getAsJsonObject();

        var stackFile = Objects.requireNonNull(loader.stackFile("items/slayer/enderman/aspect_of_the_void"));

        {

            var evaluator = stackFile.createEvaluator(instance.constants(), ImmutableStructValue.EMPTY, RepoConfig.DEFAULT, loader::module);
            var stack = stackFile.evaluateScript(evaluator);
            evaluator.errors.forEach(System.out::println);
            evaluator.debugs.forEach(System.out::println);
            System.out.println(stack);
        }

        long sum = 0;
        for (int i = 0; i < 10000; i++) {

            var evaluator = stackFile.createEvaluator(instance.constants(), ImmutableStructValue.EMPTY, RepoConfig.DEFAULT, loader::module);
            long start = System.nanoTime();
            var stack = stackFile.evaluateScript(evaluator);
            sum += System.nanoTime() - start;
            evaluator.errors.forEach(System.out::println);
            evaluator.debugs.forEach(System.out::println);
        }

        System.out.println("Took " + (sum / 10000_000000.0) + "ms");
    }
}
