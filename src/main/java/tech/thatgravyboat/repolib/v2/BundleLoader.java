package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.FileTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static tech.thatgravyboat.repolib.v2.binary.RepoBinaryUtils.BINARY_VERSION;
import static tech.thatgravyboat.repolib.v2.binary.RepoBinaryUtils.MAGIC_NUMBER;

public class BundleLoader implements RepoLoader {

    private final Path path;
    private Map<String, FunctionValueFile<?>> modules = new HashMap<>();
    private Map<String, StackFile> stackFiles = new HashMap<>();
    private ModuleFile rootFile = null;
    private Expression rootList = null;
    private final RepoConstants constants = new RepoConstants(this);
    private Transformer expressionTransformer = (a, _) -> a;

    public BundleLoader(Path bundle) {
        this.path = bundle;
    }

    @Override
    public List<LoadingErrors> load() throws IOException {

        var inputStream = Files.newInputStream(this.path);
        try (inputStream; var gzipInputStream = new GZIPInputStream(new BufferedInputStream(inputStream))) {
            var buffer = new ByteBufferImpl(gzipInputStream);
            if (buffer.readByte() != MAGIC_NUMBER[0] || buffer.readByte() != MAGIC_NUMBER[1] || buffer.readByte() != MAGIC_NUMBER[2]) {
                throw new UnsupportedOperationException("Can't decode non repo bundle!");
            }
            var fileVersion = buffer.readShort();
            if (fileVersion != BINARY_VERSION) {
                if (fileVersion < BINARY_VERSION) {
                    throw new UnsupportedOperationException("File was compiled by an older version!");
                }
                throw new UnsupportedOperationException("File was compiled by a newer version!");
            }

            var table = NameTable.decode(buffer).freezeForDecode();
            var context = new DecoderContext(table, buffer);

            ModuleFile root;
            if (context.readBoolean()) {
                root = BinaryFileTypeRegistry.readUntyped(FileTypes.MODULE, context);
            } else {
                root = null;
            }

            Expression list = ExpressionCodec.readNullable(context);

            Map<String, StackFile> stacks = new HashMap<>();

            context.readCollection(_ -> {
                stacks.put(context.readLiteral(), BinaryFileTypeRegistry.readUntyped(FileTypes.STACK, context));
                return null;
            });

            Map<String, FunctionValueFile<?>> modules = new HashMap<>();

            context.readCollection(_ -> {
                modules.put(context.readLiteral(), (FunctionValueFile<?>) BinaryFileTypeRegistry.read(context));
                return null;
            });

            this.modules = modules;
            this.stackFiles = stacks;
            this.rootList = list;
            this.rootFile = root;
        }
        return List.of();
    }

    @Override
    public Map<String, FunctionValueFile<?>> files() {
        return this.modules;
    }

    @Override
    public ModuleFile rootFile() {
        return this.rootFile;
    }

    @Override
    public Expression rootList() {
        return this.rootList;
    }

    @Override
    public Map<String, StackFile> stackFiles() {
        return this.stackFiles;
    }

    @Override
    public RepoInstance create() {
        return new RepoInstance(this, constants, new RepoListConstants(constants, this));
    }

    @Override
    public StackFile stackFile(String name) {
        return this.stackFiles.get(name);
    }

    @Override
    public FunctionValue module(String name) {
        return this.modules.get(name);
    }

    @Override
    public Collection<String> modules() {
        return this.modules.keySet();
    }

    public Evaluator createEvaluator() {
        return new Evaluator(new LayeredStructValue(
                new MutableStructValue(),
                constants
        ), this::module);
    }

    @Override
    public void registerTransform(Transformer transformer) {
        this.expressionTransformer = transformer;
    }

    @Override
    public Expression transform(Expression original, String name) {
        return this.expressionTransformer.accept(original, name);
    }
}
