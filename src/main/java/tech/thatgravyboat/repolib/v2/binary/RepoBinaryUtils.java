package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.RepoBundle;
import tech.thatgravyboat.repolib.v2.RepoLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RepoBinaryUtils {

    public static final short BINARY_VERSION = 0;
    public static byte[] MAGIC_NUMBER = new byte[]{'S', 'R', 'B'};

    public static void bundle(OutputStream stream, RepoLoader loader) {
        try (var gzipOutputStream = new GZIPOutputStream(stream)) {
            var buffer = new ByteBufferImpl(gzipOutputStream);
            buffer.writeByte(MAGIC_NUMBER[0]);
            buffer.writeByte(MAGIC_NUMBER[1]);
            buffer.writeByte(MAGIC_NUMBER[2]);
            buffer.writeShort(BINARY_VERSION);

            RepoBundle.CODEC.encodeStart(loader.bundle(), buffer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
