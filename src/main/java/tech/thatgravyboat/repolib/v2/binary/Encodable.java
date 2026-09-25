package tech.thatgravyboat.repolib.v2.binary;

public interface Encodable {

    void precode(NameTable table);
    void encode(EncoderContext context);

}
