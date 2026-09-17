package tech.thatgravyboat.repolib.v2.binary;

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
            var nameTable = NameTable.builder();

            nameTable.insert(loader.rootFile());
            nameTable.insert(loader.rootList());

            loader.stackFiles().forEach((key, value) -> {
                nameTable.insert(key);
                nameTable.insert(value);
            });

            loader.files().forEach((name, file) -> {
                nameTable.insert(name);
                nameTable.insert(file);
            });

            nameTable.encode(buffer);

            var context = new EncoderContext(nameTable.freezeForEncode(), buffer);

            buffer.writeBoolean(loader.rootFile() != null);
            if (loader.rootFile() != null) {
                BinaryFileTypeRegistry.writeUntyped(context, loader.rootFile());
            }

            ExpressionCodec.writeNullable(loader.rootList(), context);

            context.writeCollection(
                    loader.stackFiles().entrySet(), (entry, _) -> {
                        context.writeLiteral(entry.getKey());
                        BinaryFileTypeRegistry.writeUntyped(context, entry.getValue());
                    });

            context.writeCollection(
                    loader.files().entrySet(), (entry, _) -> {
                        context.writeLiteral(entry.getKey());
                        BinaryFileTypeRegistry.write(context, entry.getValue());
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encode(TypedFile<?> file) {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream; var gzipOutputStream = new GZIPOutputStream(outputStream)) {
            var buffer = new ByteBufferImpl(gzipOutputStream);
            buffer.writeShort(BINARY_VERSION);
            var nameTable = NameTable.builder();
            file.precode(nameTable);
            nameTable.encode(buffer);
            BinaryFileTypeRegistry.write(new EncoderContext(nameTable.freezeForEncode(), buffer), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public static TypedFile<?> decode(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (inputStream; var gzipInputStream = new GZIPInputStream(inputStream)) {
            var buffer = new ByteBufferImpl(gzipInputStream);
            var fileVersion = buffer.readShort();
            if (fileVersion != BINARY_VERSION) {
                if (fileVersion < BINARY_VERSION) {
                    throw new UnsupportedOperationException("File was compiled by an older version!");
                }
                throw new UnsupportedOperationException("File was compiled by a newer version!");
            }

            var nameTable = NameTable.decode(buffer);

            return BinaryFileTypeRegistry.read(new DecoderContext(nameTable.freezeForDecode(), buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static <FileType extends TypedFile<FileType> & Encodable> FileType decodeTyped(
            BinaryFileTypeRegistry.Type<FileType> type,
            byte[] bytes
    ) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (inputStream; var gzipInputStream = new GZIPInputStream(inputStream)) {
            var buffer = new ByteBufferImpl(gzipInputStream);
            var fileVersion = buffer.readShort();
            if (fileVersion != BINARY_VERSION) {
                if (fileVersion < BINARY_VERSION) {
                    throw new UnsupportedOperationException("File was compiled by an older version!");
                }
                throw new UnsupportedOperationException("File was compiled by a newer version!");
            }

            var nameTable = NameTable.decode(buffer);

            return BinaryFileTypeRegistry.readUntyped(type, new DecoderContext(nameTable.freezeForDecode(), buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
