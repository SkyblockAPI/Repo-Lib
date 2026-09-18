package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.RepoBinaryUtils;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FolderLoader implements FileVisitor<Path>, RepoLoader {

    public final Path path;
    private final Map<String, FunctionValueFile<?>> files = new HashMap<>();
    private Expression rootList = null;
    private ModuleFile rootFile = null;
    private final Map<String, StackFile> stackFiles = new HashMap<>();
    private final List<LoadingErrors> errors = new ArrayList<>();
    private Transformer expressionTransformer = (a, _) -> a;
    private final RepoConstants constants = new RepoConstants(this);

    public FolderLoader(Path path) {
        this.path = path;
    }

    public void registerTransform(Transformer transformer) {
        this.expressionTransformer = transformer;
    }

    public Expression transform(Expression original, String name) {
        return expressionTransformer.accept(original, name);
    }

    public List<LoadingErrors> load() throws IOException {
        files.clear();
        stackFiles.clear();
        rootList = null;
        rootFile = null;

        errors.clear();
        Files.walkFileTree(path, this);

        var constants = new RepoConstants(this);

        System.out.println("meow!");

        for (var entry : this.stackFiles.values()) {
            entry.init(this, constants);
        }

        Path root = path.resolve("root.srlm");
        if (Files.isRegularFile(root) && Files.exists(root)) {
            String file = Files.readString(root, StandardCharsets.UTF_8);

            var rootFile = Expression.parseModuleOrThrow(
                    this,
                    "root",
                    file,
                    new Evaluator(new MutableStructValue(), this::module)
            );
            this.files.put("root", rootFile);
            this.rootFile = rootFile;
        }

        if (rootList == null) {
            errors.add(new LoadingErrors(path, "No root list file found."));
        }
        if (rootFile == null) {
            errors.add(new LoadingErrors(path, "No root module file found."));
        }

        return errors;
    }

    public Evaluator createEvaluator() {
        return new Evaluator(new LayeredStructValue(
                new MutableStructValue(),
                constants
        ), this::module);
    }

    public Collection<String> modules() {
        return files.keySet();
    }

    public FunctionValue module(String name) {
        FunctionValue module = files.get(name);
        if (module != null && module.needsInitialization()) {
            module.initialize(createEvaluator());
        }
        return module;
    }

    public StackFile stackFile(String fileName) {
        StackFile stackFile = stackFiles.get(fileName);
        if (stackFile != null && !stackFile.hasInitialized()) {
            stackFile.init(this, constants);
        }
        return stackFile;
    }

    public Map<String, StackFile> stackFiles() {
        return stackFiles;
    }

    public Expression rootList() {
        return rootList;
    }

    public ModuleFile rootFile() {
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
            var relativeFileName = FolderLoader.this.path.relativize(file).toString().replace("\\", "/");
            if (relativeFileName.lastIndexOf('.') == -1) return FileVisitResult.CONTINUE;
            var relativeName = unescape(relativeFileName.substring(0, relativeFileName.lastIndexOf('.')));

            var content = Files.readString(file, StandardCharsets.UTF_8);
            if (relativeFileName.endsWith(".srls")) {
                var expression = Expression.parseFileOrThrow(this, content, relativeName);
                stackFiles.put(relativeName, expression);
            } else if (relativeFileName.equals("root.srlm")) {
                return FileVisitResult.CONTINUE;
            } else if (relativeFileName.endsWith(".srlm")) {
                var expression = Expression.parseModuleOrThrow(this, relativeName, content, null);
                files.put(relativeName, expression);
            } else if (relativeFileName.endsWith(".srlf")) {
                var expression = Expression.parseFunctionOrThrow(this, relativeName, content);
                files.put(relativeName, expression);
            } else if (relativeFileName.equals("root.srll")) {
                rootList = Expression.parse(content);
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

    public byte[] buildRepoBundle() {
        try (var stream = new ByteArrayOutputStream()) {
            RepoBinaryUtils.bundle(stream, this);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, FunctionValueFile<?>> files() {
        return files;
    }
}
