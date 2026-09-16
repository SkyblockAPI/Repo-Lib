package tech.thatgravyboat.repolib.v2.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

public record ByteBuffer(ByteArrayInputStream input, ByteArrayOutputStream output) {
    public ByteBuffer(ByteArrayInputStream input) {
        this(input, null);
    }

    public ByteBuffer(ByteArrayOutputStream output) {
        this(null, output);
    }

    @Override
    public ByteArrayInputStream input() {
        return Objects.requireNonNull(this.input, "Can't read on output byte buffer");
    }

    @Override
    public ByteArrayOutputStream output() {
        return Objects.requireNonNull(this.output, "Can't write on input byte buffer");
    }

    public byte readByte() throws IOException {
        return this.input().readNBytes(1)[0];
    }

    public short readShort() throws IOException {
        short value = (short) (readByte() << Short.SIZE / 2);
        value |= readByte();
        return value;
    }

    public int readInt() throws IOException {
        int value = readShort() << Integer.SIZE / 2;
        value |= readShort();
        return value;
    }

    public long readLong() throws IOException {
        int value = readInt() << Long.SIZE / 2;
        value |= readInt();
        return value;
    }

    public void writeByte(byte b) {
        this.output().write(b);
    }

    public void writeShort(short s) {
        writeByte((byte) ((s >>> Short.SIZE / 2) & 0xFF));
        writeByte((byte) (s & 0xFF));
    }

    public void writeInt(int i) {
        writeShort((short) ((i >>> Integer.SIZE / 2) & 0xFFFF));
        writeShort((short) (i & 0xFFFF));
    }

    public void writeLong(long l) {
        writeInt((int) ((l >>> Long.SIZE / 2) & 0xFFFFFFFFL));
        writeInt((int) (l & 0xFFFFFFFFL));
    }

    public void writeByteArray(byte[] array) {
        this.writeInt(array.length);
        this.output().writeBytes(array);
    }

    public byte[] readByteArray() throws IOException {
        var length = this.readInt();
        var bytes = new byte[length];
        var actual = this.input().read(bytes);
        if (actual != length) {
            throw new IOException("Expected " + length + " bytes but only got " + actual);
        }
        return bytes;
    }

    public void writeString(String value) {
        this.writeByteArray(value.getBytes(StandardCharsets.UTF_8));
    }

    public String readString() throws IOException {
        return new String(this.readByteArray(), StandardCharsets.UTF_8);
    }

    public void writeBoolean(boolean bool) {
        writeByte((byte) (bool ? 1 : 0));
    }

    public boolean readBoolean() throws IOException {
        return readByte() == 1;
    }

    public <Data extends Encodable> void write(DataType<Data> dataType, Data data) {
        data.encode(this);
    }

    public <Data extends Encodable> Data read(DataType<Data> dataType) throws IOException {
        return dataType.decode(this);
    }

    public <Data> void writeCollection(Collection<Data> data, BiConsumer<Data, ByteBuffer> serializer) {
        writeInt(data.size());
        for (var datum : data) {
            serializer.accept(datum, this);
        }
    }

    public <Data> Collection<Data> readCollection(Decoder<Data> decoder) throws IOException {
        var size = readInt();
        if (size == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Data>(size);
        for (var i = 0; i < size; i++) {
            list.add(decoder.decode(this));
        }
        return list;
    }

    public void writeDouble(double value) {
        this.writeLong(Double.doubleToRawLongBits(value));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }
}
