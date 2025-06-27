import tech.thatgravyboat.repolib.api.RepoAPI;
import tech.thatgravyboat.repolib.api.RepoVersion;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(RepoImplementation.PROPERTY, Impl.class.getName());
        RepoAPI.setup(RepoVersion.V1_21_5, System.out::println);

        Thread.sleep(5000);

        var data = RepoAPI.runes().getRunes("BITE");
        System.out.println(data);
    }

    public static class Impl implements RepoImplementation {

        @Override
        public Path getRepoPath() {
            return Path.of("./run");
        }
    }
}
