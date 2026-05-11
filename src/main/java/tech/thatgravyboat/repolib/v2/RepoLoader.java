package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
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
    private Expression rootList = null;
    private Expression rootFile = null;
    private final Map<String, StackFile> stackFiles = new HashMap<>();

    public RepoLoader(Path path) {
        this.path = path;
    }

    public List<LoadingErrors> load() throws IOException {
        files.clear();
        stackFiles.clear();
        rootList = null;
        rootFile = null;

        var errors = new ArrayList<LoadingErrors>();
        try (var stream = Files.walk(path)) {
            stream.forEach(path -> {
                var relativeFileName = this.path.relativize(path).toString();
                if (Files.isDirectory(path)) {
                    return;
                }
                var relativeName = unescape(relativeFileName.substring(0, relativeFileName.lastIndexOf('.')));

                try {
                    var content = Files.readString(path, StandardCharsets.UTF_8);
                    if (relativeFileName.endsWith(".srls")) {
                        var expression = Expression.parseFileOrThrow(content);
                        stackFiles.put(relativeName, expression);
                    } else if (relativeFileName.equals("root.srlm")) {
                        rootFile = Expression.parse(content);
                    } else if (relativeFileName.endsWith(".srlm")) {
                        var expression = Expression.parse(content);
                        files.put(relativeName, expression);
                    } else if (relativeFileName.equals("root.srll")) {
                        rootList = Expression.parse(content);
                    } else {
                        errors.add(new LoadingErrors(path, "Not a valid script file"));
                    }
                } catch (Exception exception) {
                    errors.add(new LoadingErrors(path, exception));
                }
            });
        }

        if (rootList == null) {
            errors.add(new LoadingErrors(path, "No root list file found."));
        }
        if (rootFile == null) {
            errors.add(new LoadingErrors(path, "No root module file found."));
        }

        return errors;
    }

    public Expression getExpression(String name) {
        return files.get(name);
    }

    public StackFile getStackFile(String fileName) {
        return this.stackFiles.get(fileName);
    }

    public Map<String, StackFile> stackFiles() {
        return stackFiles;
    }

    public Expression rootList() {
        return rootList;
    }

    public Expression rootFile() {
        return rootFile;
    }

    public RepoInstance create() {
        var repoConstants = new RepoConstants(this);
        return new RepoInstance(this, repoConstants, new RepoListConstants(repoConstants, this));
    }

    private static String unescape(String encoded) {
        return encoded
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&cl;", ":")
                .replace("&dq;", "\"")
                .replace("&fs;", "/")
                .replace("&bs;", "\\")
                .replace("&pi;", "|")
                .replace("&qu;", "?")
                .replace("&as;", "*")
                .replace("&an;", "&");
    }
}
