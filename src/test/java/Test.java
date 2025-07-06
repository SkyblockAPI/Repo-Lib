import tech.thatgravyboat.repolib.api.RepoAPI;
import tech.thatgravyboat.repolib.api.RepoVersion;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(RepoImplementation.PROPERTY, Impl.class.getName());
        RepoAPI.setup(RepoVersion.V1_21_5, it -> {
            var data = RepoAPI.attributes().getAttribute("ACCESSORY_SIZE");
            System.out.println(data);
        });

        while (true) {
            Thread.sleep(100);
        }
    }

    public static class Impl implements RepoImplementation {

        @Override
        public Path getRepoPath() {
            return Path.of("./run");
        }
    }
}
