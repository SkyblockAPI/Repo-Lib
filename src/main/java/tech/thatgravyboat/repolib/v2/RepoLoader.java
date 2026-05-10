package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.expl.Expression;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoLoader {

    private final Path path;
    private final Map<String, Expression> files = new HashMap<>();
    private final Map<String, StackFile> stackFiles = new HashMap<>();

    public RepoLoader(Path path) {
        this.path = path;
    }

    public List<LoadingErrors> load() throws IOException {
        var errors = new ArrayList<LoadingErrors>();
        try (var stream = Files.walk(path)) {
            stream.forEach(path -> {
                var relativeFileName = this.path.relativize(path).toString();
                if (Files.isDirectory(path)) {
                    return;
                }
                var relativeName = relativeFileName.substring(0, relativeFileName.lastIndexOf('.'));

                try {
                    var content = Files.readString(path, StandardCharsets.UTF_8);
                    if (relativeFileName.endsWith(".ssbp")) {
                        var expression = Expression.parseFileOrThrow(content);
                        stackFiles.put(relativeName, expression);
                    } else if (relativeFileName.endsWith(".ssif")) {
                        var expression = Expression.parse(content);
                        files.put(relativeName, expression);
                    } else {
                        errors.add(new LoadingErrors(path, "Not a valid script file"));
                    }
                } catch (Exception exception) {
                    errors.add(new LoadingErrors(path, exception));
                }
            });
        }
        return errors;
    }

    public Expression getExpression(String name) {
        return files.get(name);
    }

    public StackFile getStackFile(String fileName) {
        return this.stackFiles.get(fileName);
    }
}
