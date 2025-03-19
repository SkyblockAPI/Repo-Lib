import tech.thatgravyboat.repolib.api.RepoAPI;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(RepoImplementation.PROPERTY, Impl.class.getName());
        RepoAPI.setup();

        Thread.sleep(5000);

        var pet = RepoAPI.pets().getPet("SHEEP");
        var tier = pet.tiers().get("LEGENDARY");
        int level = 66;

        System.out.println(pet.name());
        for (String line : tier.getFormattedLore(level, "PET_ITEM_TEXTBOOK")) {
            System.out.println(line);
        }

    }

    public static class Impl implements RepoImplementation {

        @Override
        public Path getRepoPath() {
            return Path.of("./run");
        }
    }
}
