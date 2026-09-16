package tech.thatgravyboat.repolib.v2.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RepoBinaryUtils {

    public static final short BINARY_VERSION = 0;

    public static byte[] encode(TypedFile<?> file) {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            var buffer = new ByteBuffer(outputStream);
            buffer.writeShort(BINARY_VERSION);
            BinaryFileTypeRegistry.write(buffer, file);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TypedFile<?> decode(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (inputStream) {
            var buffer = new ByteBuffer(inputStream);
            var fileVersion = buffer.readShort();
            if (fileVersion != BINARY_VERSION) {
                if (fileVersion < BINARY_VERSION) {
                    throw new UnsupportedOperationException("File was compiled by an older version!");
                }
                throw new UnsupportedOperationException("File was compiled by a newer version!");
            }

            return BinaryFileTypeRegistry.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static <FileType extends TypedFile<FileType> & Encodable> FileType decodeTyped(BinaryFileTypeRegistry.Type<FileType> type, byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (inputStream) {
            var buffer = new ByteBuffer(inputStream);
            var fileVersion = buffer.readShort();
            if (fileVersion != BINARY_VERSION) {
                if (fileVersion < BINARY_VERSION) {
                    throw new UnsupportedOperationException("File was compiled by an older version!");
                }
                throw new UnsupportedOperationException("File was compiled by a newer version!");
            }

            return BinaryFileTypeRegistry.readTyped(type, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
