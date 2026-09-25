package tech.thatgravyboat.repolib.v2.binary;

public interface Encoder<Type> {

    public void encode(EncoderContext context, Type data);

}
