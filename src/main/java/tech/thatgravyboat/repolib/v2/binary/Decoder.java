package tech.thatgravyboat.repolib.v2.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface Decoder<Type> {

    Type decode(ByteBuffer buffer) throws IOException;
}
