package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.expl.FunctionFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public class FileTypes {
    public static final BinaryFileTypeRegistry.Type<ModuleFile> MODULE = register(ModuleFile.CODEC);
    public static final BinaryFileTypeRegistry.Type<StackFile> STACK = register(StackFile.CODEC);
    public static final BinaryFileTypeRegistry.Type<FunctionFile> FUNCTION = register(FunctionFile.CODEC);


    private static <FileType extends TypedFile<FileType>> BinaryFileTypeRegistry.Type<FileType> register(
            BinaryCodec<FileType> codec
    ) {
        var type = new BinaryFileTypeRegistry.Type<>(codec, (byte) BinaryFileTypeRegistry.counter.getAndIncrement());
        BinaryFileTypeRegistry.registry.put(type.id(), type);
        return type;
    }

}
