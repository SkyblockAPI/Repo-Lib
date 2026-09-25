package tech.thatgravyboat.repolib.v2;

import java.util.Map;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.expl.FunctionValueFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

public record RepoBundle(ModuleFile rootFile, Expression<?> rootList, Map<String, StackFile> stackFiles, Map<String, FunctionValueFile<?>> modules) {
    public static final BinaryCodec<RepoBundle> CODEC = BinaryRecordBuilder.of(
        ModuleFile.CODEC.nullable().forGetter(RepoBundle::rootFile),
        BinaryCodec.EXPRESSION.nullable().forGetter(RepoBundle::rootList),
        BinaryCodec.map(BinaryCodec.STRING, StackFile.CODEC).forGetter(RepoBundle::stackFiles),
        BinaryCodec.map(BinaryCodec.STRING, BinaryFileTypeRegistry.FUNCTION_FILE).forGetter(RepoBundle::modules),
        RepoBundle::new
    );
}
