import tech.thatgravyboat.repolib.api.RepoAPI;
import tech.thatgravyboat.repolib.api.RepoVersion;
import tech.thatgravyboat.repolib.api.recipes.Recipe;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(RepoImplementation.PROPERTY, Impl.class.getName());
        RepoAPI.setup(RepoVersion.V1_21_5, it -> {
            var data = RepoAPI.attributes().getAttribute("ACCESSORY_SIZE");
            System.out.println(data);

            var kat = RepoAPI.recipes().getRecipes(Recipe.Type.KAT);
            System.out.println(kat);

            var gdrag = RepoAPI.pets().getPet("GOLDEN_DRAGON").tiers().values().stream().findFirst().get();
            System.out.println("lvl 0");
            gdrag.getFormattedLore(0).forEach(System.out::println);
            System.out.println("lvl 100");
            gdrag.getFormattedLore(100).forEach(System.out::println);
            System.out.println("lvl 150");
            gdrag.getFormattedLore(150).forEach(System.out::println);
        });

        new Scanner(System.in).nextLine();
    }

    public static class Impl implements RepoImplementation {

        @Override
        public Path getRepoPath() {
            return Path.of("./run");
        }
    }
}
