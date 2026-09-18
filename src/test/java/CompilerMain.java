import tech.thatgravyboat.repolib.v2.FolderLoader;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.jvm.compiler.ExpressionCompiler;

void main() {
    try {
        var repoLoader = new FolderLoader(Path.of("src/test/repo"));
        repoLoader.registerTransform((e, n) -> {
            try {
                return ExpressionCompiler.createSelfEvaluatingExpression(e, n);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        var errors = repoLoader.load();
        for (var error : errors) {
            IO.println("Failed to load file " + error.file() + " due to " + error.reason());
            error.reason().printStackTrace();
        }
        var constants = new RepoConstants(repoLoader);
        var evaluator = new Evaluator(constants, repoLoader::module);
        var meow = new Parser("""
                print("meow");
                """).parseModuleFile("test", repoLoader);
//            meow.init(constants);
//            System.out.println(meow.meta());
        IO.println(meow.evaluate(evaluator));

        IO.println(evaluator.stack);

//            var file = repoLoader.getModule("silly");
//            var res = file.apply(evaluator, List.of());
//            System.out.println(res);

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
