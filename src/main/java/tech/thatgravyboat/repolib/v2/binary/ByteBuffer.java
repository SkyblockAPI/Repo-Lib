package tech.thatgravyboat.repolib.v2.binary;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;

public interface ByteBuffer {

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    void writeByte(byte b);

    void writeShort(short s);

    void writeInt(int i);

    void writeLong(long l);

    void writeByteArray(byte[] array);

    byte[] readByteArray() throws IOException;

    void writeString(String value);

    String readString() throws IOException;

    void writeBoolean(boolean bool);

    boolean readBoolean() throws IOException;

    void writeDouble(double value);

    double readDouble() throws IOException;

    interface Forwarding extends ByteBuffer {
        ByteBuffer delegate();

        @Override
        default byte readByte() throws IOException {
            return delegate().readByte();
        }

        @Override
        default short readShort() throws IOException {
            return delegate().readShort();
        }

        @Override
        default int readInt() throws IOException {
            return delegate().readInt();
        }

        @Override
        default long readLong() throws IOException {
            return delegate().readLong();
        }

        @Override
        default void writeByte(byte b) {
            delegate().writeByte(b);
        }

        @Override
        default void writeShort(short s) {
            delegate().writeShort(s);
        }

        @Override
        default void writeInt(int i) {
            delegate().writeInt(i);
        }

        @Override
        default void writeLong(long l) {
            delegate().writeLong(l);
        }

        @Override
        default void writeByteArray(byte[] array) {
            delegate().writeByteArray(array);
        }

        @Override
        default byte[] readByteArray() throws IOException {
            return delegate().readByteArray();
        }

        @Override
        default void writeString(String value) {
            delegate().writeString(value);
        }

        @Override
        default String readString() throws IOException {
            return delegate().readString();
        }

        @Override
        default void writeBoolean(boolean bool) {
            delegate().writeBoolean(bool);
        }

        @Override
        default boolean readBoolean() throws IOException {
            return delegate().readBoolean();
        }

        @Override
        default void writeDouble(double value) {
            delegate().writeDouble(value);
        }

        @Override
        default double readDouble() throws IOException {
            return delegate().readDouble();
        }

    }
}
