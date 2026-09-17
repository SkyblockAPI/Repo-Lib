import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.compiler.ModuleCompiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CompilerMain {

    public static void main(String[] args) throws IOException {
        try {
            var repoLoader = new RepoLoader(Path.of("src/test/repo"));
            repoLoader.registerTransform((e, n) -> {
                try {
                    return ModuleCompiler.createSelfEvaluatingExpression(e, n);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            var errors = repoLoader.load();
            for (var error : errors) {
                System.out.println("Failed to load file " + error.file() + " due to " + error.reason());
                error.reason().printStackTrace();
            }
            var constants = new RepoConstants(repoLoader);
            var evaluator = new Evaluator(constants, repoLoader::getModule);
            var meow = new Parser("""
                    print("meow");
                    """).parseModuleFile("test", repoLoader, evaluator);
//            meow.init(constants);
//            System.out.println(meow.meta());
            System.out.println(meow.evaluate(evaluator));

            System.out.println(evaluator.stack);

//            var file = repoLoader.getModule("silly");
//            var res = file.apply(evaluator, List.of());
//            System.out.println(res);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
