package tech.thatgravyboat.repolib.v2.binary;

public interface TypedFile<Self extends TypedFile<Self>> {
    BinaryFileTypeRegistry.Type<Self> fileId();
}