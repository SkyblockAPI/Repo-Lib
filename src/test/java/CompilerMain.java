import tech.thatgravyboat.repolib.v2.FolderLoader;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.jvm.compiler.ExpressionCompiler;

void main() {
    try {
        var repoLoader = new FolderLoader(Path.of("src/test/repo"));
        ExpressionCompiler.registerSaver(((s, bytes) -> {
            try {
                Files.write(Path.of("output", s + ".class"), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        var errors = repoLoader.load();
        for (var error : errors) {
            IO.println("Failed to load file " + error.file() + " due to " + error.reason());
            error.reason().printStackTrace();
        }
        var constants = new RepoConstants(repoLoader);
        var evaluator = new Evaluator(constants, repoLoader::module);
        var meow = new Parser("""
testval = [1, 3, 55, 53];
test23 = [5, 1, 7];
for (val2 : test23) {
ignore = false;
for (i = 0; i < 2; i+=1) {
    print(i);
};
if (ignore) continue;
print("val2" + "mrow?");
};
""").parseModuleFile("test", repoLoader);
//            meow.init(constants);
//            System.out.println(meow.meta());
        IO.println(meow.evaluate(evaluator));

        IO.println(meow);

//            var file = repoLoader.getModule("silly");
//            var res = file.apply(evaluator, List.of());
//            System.out.println(res);

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
