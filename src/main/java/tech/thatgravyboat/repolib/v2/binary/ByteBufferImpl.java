package tech.thatgravyboat.repolib.v2.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ByteBufferImpl(InputStream input, OutputStream output) implements ByteBuffer {
    public static final int MAX_VARINT_SIZE = 5;
    private static final int MAX_VARLONG_SIZE = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;

    public ByteBufferImpl(InputStream input) {
        this(input, null);
    }

    public ByteBufferImpl(OutputStream output) {
        this(null, output);
    }

    @Override
    public InputStream input() {
        return Objects.requireNonNull(this.input, "Can't read on output byte buffer");
    }

    @Override
    public OutputStream output() {
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
        int out = 0;
        int bytes = 0;

        byte in;
        do {
            in = this.readByte();
            out |= (in & DATA_BITS_MASK) << bytes++ * 7;
            if (bytes > MAX_VARINT_SIZE) {
                throw new RuntimeException("VarInt too big");
            }
        } while (hasContinuationBit(in));

        return out;
    }

    public long readLong() throws IOException {
        long out = 0L;
        int bytes = 0;

        byte in;
        do {
            in = this.readByte();
            out |= (long)(in & DATA_BITS_MASK) << bytes++ * 7;
            if (bytes > MAX_VARLONG_SIZE) {
                throw new RuntimeException("VarLong too big");
            }
        } while (hasContinuationBit(in));

        return out;
    }

    public void writeByte(byte b) {
        try {
            this.output().write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeShort(short s) {
        writeByte((byte) ((s >>> Short.SIZE / 2) & 0xFF));
        writeByte((byte) (s & 0xFF));
    }

    public void writeInt(int value) {
        while ((value & -CONTINUATION_BIT_MASK) != 0) {
            this.writeByte((byte) (value & DATA_BITS_MASK | CONTINUATION_BIT_MASK));
            value >>>= 7;
        }

        this.writeByte((byte) value);
    }

    public void writeLong(long value) {
        while ((value & -128L) != 0L) {
            this.writeByte((byte) ((value & DATA_BITS_MASK) | CONTINUATION_BIT_MASK));
            value >>>= 7;
        }

        this.writeByte((byte) value);
    }

    public void writeByteArray(byte[] array) {
        this.writeInt(array.length);
        try {
            this.output().write(array);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readByteArray() throws IOException {
        var length = this.readInt();
        var actual = this.input().readNBytes(length);
        if (actual.length != length) {
            throw new IOException("Expected " + length + " bytes but only got " + actual);
        }
        return actual;
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

    public void writeDouble(double value) {
        this.writeLong(Double.doubleToRawLongBits(value));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }


    public static boolean hasContinuationBit(byte in) {
        return (in & CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK;
    }
}
