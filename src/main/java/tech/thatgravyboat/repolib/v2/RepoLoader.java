package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoLoader implements FileVisitor<Path> {

    public final Path path;
    private final Map<String, ModuleFile> files = new HashMap<>();
    private Expression rootList = null;
    private Expression rootFile = null;
    private final Map<String, StackFile> stackFiles = new HashMap<>();
    private final List<LoadingErrors> errors = new ArrayList<>();

    public RepoLoader(Path path) {
        this.path = path;
    }

    public List<LoadingErrors> load() throws IOException {
        files.clear();
        stackFiles.clear();
        rootList = null;
        rootFile = null;

        errors.clear();
        Files.walkFileTree(path, this);

        var constants = new RepoConstants(this);

        for (var entry : this.stackFiles.values()) {
            entry.init(constants);
        }

        if (rootList == null) {
            errors.add(new LoadingErrors(path, "No root list file found."));
        }
        if (rootFile == null) {
            errors.add(new LoadingErrors(path, "No root module file found."));
        }

        return errors;
    }

    public ModuleFile getModule(String name) {
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
                .replace("&do;", ".")
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

    @Override
    public @NotNull FileVisitResult preVisitDirectory(
            Path dir,
            @NotNull BasicFileAttributes attrs
    ) throws IOException {
        if (Files.isHidden(dir) || dir.getFileName().toString().startsWith(".")) return FileVisitResult.SKIP_SUBTREE;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
        if (file.getFileName().toString().startsWith(".")) return FileVisitResult.CONTINUE;

        try {
            var relativeFileName = RepoLoader.this.path.relativize(file).toString().replace("\\", "/");
            if (relativeFileName.lastIndexOf('.') == -1) return FileVisitResult.CONTINUE;
            var relativeName = unescape(relativeFileName.substring(0, relativeFileName.lastIndexOf('.')));

            var content = Files.readString(file, StandardCharsets.UTF_8);
            if (relativeFileName.endsWith(".srls")) {
                var expression = Expression.parseFileOrThrow(this, content);
                stackFiles.put(relativeName, expression);
            } else if (relativeFileName.equals("root.srlm")) {
                rootFile = Expression.parse(content);
            } else if (relativeFileName.endsWith(".srlm")) {
                var expression = Expression.parseModuleOrThrow(this, content);
                files.put(relativeName, expression);
            } else if (relativeFileName.equals("root.srll")) {
                rootList = Expression.parse(content);
            } else {
                errors.add(new LoadingErrors(file, "Not a valid script file"));
            }
        } catch (Exception exception) {
            errors.add(new LoadingErrors(file, exception));
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public @NotNull FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public @NotNull FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}
