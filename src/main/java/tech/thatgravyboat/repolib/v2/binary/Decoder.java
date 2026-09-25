package tech.thatgravyboat.repolib.v2.binary;

import java.io.IOException;

public interface Decoder<Type> {
    Type decode(DecoderContext context) throws IOException;
}
