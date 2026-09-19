package tech.thatgravyboat.repolib.v2.binary;

public interface DataType<FileType extends Encodable> extends Decoder<FileType> {

    byte id();
}
