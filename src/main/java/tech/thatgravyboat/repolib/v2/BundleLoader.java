package tech.thatgravyboat.repolib.v2;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
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
    private Expression<?> rootList = null;
    private final RepoConstants constants = new RepoConstants(this);

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

            var context = new DecoderContext(buffer);
            context.updateTable(NameTable.CODEC.decode(context).freezeForDecode());

            var bundle = RepoBundle.CODEC.decode(context);

            this.modules = bundle.modules();
            this.stackFiles = bundle.stackFiles();
            this.rootList = bundle.rootList();
            this.rootFile = bundle.rootFile();
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
    public Expression<?> rootList() {
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
        StackFile stackFile = stackFiles.get(name);
        if (stackFile != null && stackFile.needsInitialization()) {
            stackFile.init(this, constants);
        }
        return stackFile;
    }

    @Override
    public FunctionValue module(String name) {
        FunctionValue module = modules.get(name);
        if (module != null && module.needsInitialization()) {
            module.initialize(createEvaluator());
        }
        return module;
    }

    @Override
    public Collection<String> modules() {
        return this.modules.keySet();
    }

    @Override
    public RepoBundle bundle() {
        return new RepoBundle(
            this.rootFile,
            this.rootList,
            this.stackFiles,
            this.modules
        );
    }

    public Evaluator createEvaluator() {
        return new Evaluator(new LayeredStructValue(
                new MutableStructValue(),
                constants
        ), this::module);
    }
}
