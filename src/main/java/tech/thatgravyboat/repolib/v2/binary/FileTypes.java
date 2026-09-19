package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.expl.FunctionFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public class FileTypes {
    public static final BinaryFileTypeRegistry.Type<ModuleFile> MODULE = register(ModuleFile::decode);
    public static final BinaryFileTypeRegistry.Type<StackFile> STACK = register(StackFile::decode);
    public static final BinaryFileTypeRegistry.Type<FunctionFile> FUNCTION = register(FunctionFile::decode);


    private static <FileType extends TypedFile<FileType> & Encodable> BinaryFileTypeRegistry.Type<FileType> register(
            Decoder<FileType> decoder
    ) {
        var type = new BinaryFileTypeRegistry.Type<>(decoder, (byte) BinaryFileTypeRegistry.counter.getAndIncrement());
        BinaryFileTypeRegistry.registry.put(type.id(), type);
        return type;
    }

}
