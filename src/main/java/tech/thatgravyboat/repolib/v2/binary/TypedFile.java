package tech.thatgravyboat.repolib.v2.binary;

public interface TypedFile<Self extends TypedFile<Self> & Encodable> extends Encodable {
    BinaryFileTypeRegistry.Type<Self> fileId();
}