/*
 * Copyright 2017 Patrick Favre-Bulle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package at.favre.lib.bytes;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.*;

/**
 * Common Util methods to convert or modify byte arrays
 */
final class Util {
    private static final int BUF_SIZE = 0x1000; // 4K

    private Util() {
    }

    /**
     * Returns the values from each provided byteArray combined into a single byteArray.
     * For example, {@code append(new byte[] {a, b}, new byte[] {}, new
     * byte[] {c}} returns the byteArray {@code {a, b, c}}.
     *
     * @param arrays zero or more {@code byte} arrays
     * @return a single byteArray containing all the values from the source arrays, in
     * order
     */
    static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    /**
     * Returns the start position of the first occurrence of the specified {@code
     * target} within {@code array}, or {@code -1} if there is no such occurrence.
     * <p>
     * <p>More formally, returns the lowest index {@code i} such that {@code
     * java.util.Arrays.copyOfRange(array, i, i + target.length)} contains exactly
     * the same elements as {@code target}.
     *
     * @param array  the array to search for the sequence {@code target}
     * @param target the array to search for as a sub-sequence of {@code array}
     */
    static int indexOf(byte[] array, byte[] target, int start, int end) {
        Objects.requireNonNull(array, "array must not be null");
        Objects.requireNonNull(target, "target must not be null");
        if (target.length == 0 || start < 0) {
            return -1;
        }

        outer:
        for (int i = start; i < Math.min(end, array.length - target.length + 1); i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last appearance of the value {@code target} in
     * {@code array}.
     *
     * @param array  an array of {@code byte} values, possibly empty
     * @param target a primitive {@code byte} value
     * @return the greatest index {@code i} for which {@code array[i] == target},
     * or {@code -1} if no such index exists.
     */
    static int lastIndexOf(byte[] array, byte target, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Counts the occurrence of target in the the in the subject array
     *
     * @param array  to count in
     * @param target to count
     * @return number of times target is in subject
     */
    static int countByte(byte[] array, byte target) {
        int count = 0;
        for (byte b : array) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the times given pattern (ie. an array) can be found in given array
     *
     * @param array   to count in
     * @param pattern to match in array
     * @return number of times pattern is in subject
     */
    static int countByteArray(byte[] array, byte[] pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        if (pattern.length == 0 || array.length == 0) {
            return 0;
        }
        int count = 0;
        outer:
        for (int i = 0; i < array.length - pattern.length + 1; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (array[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            count++;
        }
        return count;
    }

    /**
     * Copies a collection of {@code Byte} instances into a new array of
     * primitive {@code byte} values.
     * <p>
     * <p>Elements are copied from the argument collection as if by {@code
     * collection.toArray()}.  Calling this method is as thread-safe as calling
     * that method.
     *
     * @param collection a collection of {@code Byte} objects
     * @return an array containing the same values as {@code collection}, in the
     * same order, converted to primitives
     * @throws NullPointerException if {@code collection} or any of its elements
     *                              is null
     */
    static byte[] toArray(Collection<Byte> collection) {
        Object[] boxedArray = collection.toArray();
        int len = boxedArray.length;
        byte[] array = new byte[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Byte) boxedArray[i];
        }
        return array;
    }

    /**
     * Converts given array to list of boxed bytes. Will create a new list
     * and not reuse the array reference.
     *
     * @param array to convert
     * @return list with same length and content as array
     */
    static List<Byte> toList(byte[] array) {
        List<Byte> list = new ArrayList<>(array.length);
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }

    /**
     * Converts this primitive array to an boxed object array.
     * Will create a new array and not reuse the array reference.
     *
     * @param array to convert
     * @return new array
     */
    static Byte[] toBoxedArray(byte[] array) {
        Byte[] objectArray = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }
        return objectArray;
    }

    /**
     * Converts this object array to an primitives type array.
     * Will create a new array and not reuse the array reference.
     *
     * @param objectArray to convert
     * @return new array
     */
    static byte[] toPrimitiveArray(Byte[] objectArray) {
        byte[] primitivesArray = new byte[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            primitivesArray[i] = objectArray[i];
        }
        return primitivesArray;
    }

    /**
     * Creates a byte array from given int array.
     * The resulting byte array will have length intArray * 4.
     *
     * @param intArray to convert
     * @return resulting byte array
     */
    static byte[] toByteArray(int[] intArray) {
        byte[] primitivesArray = new byte[intArray.length * 4];
        for (int i = 0; i < intArray.length; i++) {
            byte[] intBytes = ByteBuffer.allocate(4).putInt(intArray[i]).array();
            System.arraycopy(intBytes, 0, primitivesArray, (i * 4), intBytes.length);
        }
        return primitivesArray;
    }

    /**
     * Creates a byte array from given long array.
     * The resulting byte array will have length longArray * 8
     *
     * @param longArray to convert
     * @return resulting byte array
     */
    static byte[] toByteArray(long[] longArray) {
        byte[] primitivesArray = new byte[longArray.length * 8];
        for (int i = 0; i < longArray.length; i++) {
            byte[] longBytes = ByteBuffer.allocate(8).putLong(longArray[i]).array();
            System.arraycopy(longBytes, 0, primitivesArray, (i * 8), longBytes.length);
        }
        return primitivesArray;
    }

    /**
     * Simple Durstenfeld shuffle
     * <p>
     * See: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
     *
     * @param array
     * @param random
     */
    static void shuffle(byte[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            byte a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    /**
     * Reverses the elements of {@code array} between {@code fromIndex} inclusive and {@code toIndex}
     * exclusive. This is equivalent to {@code
     * Collections.reverse(Bytes.asList(array).subList(fromIndex, toIndex))}, but is likely to be more
     * efficient.
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0}, {@code toIndex > array.length}, or
     *                                   {@code toIndex > fromIndex}
     */
    static void reverse(byte[] array, int fromIndex, int toIndex) {
        Objects.requireNonNull(array);
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            byte tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Read bytes, buffered, from given input stream. Pass -1 to read the whole stream or limit with length
     * parameter.
     *
     * @param inputStream     to read from
     * @param maxLengthToRead how many bytes to read from input stream; pass -1 to read whole stream
     * @return all bytes from the stream (possibly limited by maxLengthToRead); output length is never longer than stream size
     */
    static byte[] readFromStream(InputStream inputStream, final int maxLengthToRead) {
        final boolean readWholeStream = maxLengthToRead == -1;
        int remaining = maxLengthToRead;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(readWholeStream ? 32 : maxLengthToRead);
            byte[] buf = new byte[0];
            while (readWholeStream || remaining > 0) {
                int bufSize = Math.min(BUF_SIZE, readWholeStream ? BUF_SIZE : remaining);
                if (buf.length != bufSize) {
                    buf = new byte[bufSize];
                }
                int r = inputStream.read(buf);
                if (r == -1) {
                    break;
                }
                remaining -= r;
                out.write(buf, 0, r);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("could not read from input stream", e);
        }
    }

    /**
     * Read all bytes until length from given byte array.
     *
     * @param dataInput to read from
     * @return all bytes from the dataInput
     */
    static byte[] readFromDataInput(DataInput dataInput, int length) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        try {
            byte[] buf;
            int remaining = length;
            for (int i = 0; i < length; i++) {
                buf = new byte[Math.min(remaining, BUF_SIZE)];
                dataInput.readFully(buf);
                out.write(buf);
                remaining -= buf.length;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("could not read from data input", e);
        }
    }

    /**
     * Combines a single argument with a vararg to a single array
     *
     * @param firstByte first arg
     * @param moreBytes varargs
     * @return array containing all args
     */
    static byte[] concatVararg(byte firstByte, byte[] moreBytes) {
        if (moreBytes == null) {
            return new byte[]{firstByte};
        } else {
            return concat(new byte[]{firstByte}, moreBytes);
        }
    }

    /**
     * Reads all bytes from a file
     *
     * @param file the file to read
     * @return byte content
     */
    static byte[] readFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file must not be null, has to exist and must be a file (not a directory) " + file);
        }

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("could not read from file", e);
        }
    }

    /**
     * Shows the length and a preview of max 8 bytes of the given byte
     *
     * @param bytes to convert to string
     * @return string representation
     */
    static String toString(Bytes bytes) {
        String preview;
        if (bytes.isEmpty()) {
            preview = "";
        } else if (bytes.length() > 8) {
            preview = "(0x" + bytes.copy(0, 4).encodeHex() + "..." + bytes.copy(bytes.length() - 4, 4).encodeHex() + ")";
        } else {
            preview = "(0x" + bytes.encodeHex() + ")";
        }

        return bytes.length() + " " + (bytes.length() == 1 ? "byte" : "bytes") + " " + preview;
    }

    /**
     * Light shift of whole byte array by shiftBitCount bits.
     * This method will alter the input byte array.
     */
    static byte[] shiftLeft(byte[] byteArray, int shiftBitCount) {
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) ((1 << shiftMod) - 1);
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = 0; i < byteArray.length; i++) {
            sourceIndex = i + offsetBytes;
            if (sourceIndex >= byteArray.length) {
                byteArray[i] = 0;
            } else {
                byte src = byteArray[sourceIndex];
                byte dst = (byte) (src << shiftMod);
                if (sourceIndex + 1 < byteArray.length) {
                    dst |= byteArray[sourceIndex + 1] >>> (8 - shiftMod) & carryMask;
                }
                byteArray[i] = dst;
            }
        }
        return byteArray;
    }

    /**
     * Unsigned/logical right shift of whole byte array by shiftBitCount bits.
     * This method will alter the input byte array.
     */
    static byte[] shiftRight(byte[] byteArray, int shiftBitCount) {
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) (0xFF << (8 - shiftMod));
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = byteArray.length - 1; i >= 0; i--) {
            sourceIndex = i - offsetBytes;
            if (sourceIndex < 0) {
                byteArray[i] = 0;
            } else {
                byte src = byteArray[sourceIndex];
                byte dst = (byte) ((0xff & src) >>> shiftMod);
                if (sourceIndex - 1 >= 0) {
                    dst |= byteArray[sourceIndex - 1] << (8 - shiftMod) & carryMask;
                }
                byteArray[i] = dst;
            }
        }
        return byteArray;
    }

    static void checkIndexBounds(int length, int index, int primitiveLength, String type) {
        if (index < 0 || index + primitiveLength > length) {
            throw new IndexOutOfBoundsException("cannot get " + type + " from index out of bounds: " + index);
        }
    }

    static void checkExactLength(int length, int expectedLength, String type) {
        if (length != expectedLength) {
            throw new IllegalStateException("cannot convert to " + type + " if length != " + expectedLength + " bytes (was " + length + ")");
        }
    }

    static boolean equals(byte[] obj, Byte[] anotherArray) {
        if (anotherArray == null) return false;
        if (obj.length != anotherArray.length) return false;
        for (int i = 0; i < obj.length; i++) {
            if (anotherArray[i] == null || obj[i] != anotherArray[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * See https://codahale.com/a-lesson-in-timing-attacks/
     */
    static boolean constantTimeEquals(byte[] obj, byte[] anotherArray) {
        if (anotherArray == null || obj.length != anotherArray.length) return false;

        int result = 0;
        for (int i = 0; i < obj.length; i++) {
            result |= obj[i] ^ anotherArray[i];
        }
        return result == 0;
    }

    /**
     * Convert UUID to a newly generated 16 byte long array representation. Puts the 8 byte most significant bits and
     * 8 byte least significant bits into an byte array.
     *
     * @param uuid to convert to array
     * @return buffer containing the 16 bytes
     */
    static ByteBuffer getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb;
    }

    /**
     * Hashcode implementation for a byte array and given byte order
     *
     * @param byteArray to calculate hashCode of
     * @param byteOrder to calculate hashCode of
     * @return hashCode
     */
    static int hashCode(byte[] byteArray, ByteOrder byteOrder) {
        int result = Arrays.hashCode(byteArray);
        result = 31 * result + (byteOrder != null ? byteOrder.hashCode() : 0);
        return result;
    }

    /*
    =================================================================================================
     Copyright 2011 Twitter, Inc.
     -------------------------------------------------------------------------------------------------
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this work except in compliance with the License.
     You may obtain a copy of the License in the LICENSE file, or at:

      http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
     =================================================================================================
     */

    /**
     * Class that calculates the entropy factor
     *
     * @param <T>
     */
    static final class Entropy<T> {
        private final Map<T, Integer> map = new HashMap<>();
        private int total = 0;

        public Entropy(Iterable<T> elements) {
            for (T element : elements) {
                if (!map.containsKey(element)) {
                    map.put(element, 0);
                }
                map.put(element, map.get(element) + 1);
                total++;
            }
        }

        private double Log2(double n) {
            return Math.log(n) / Math.log(2);
        }

        public double entropy() {
            double entropy = 0;
            for (int count : map.values()) {
                double prob = (double) count / total;
                entropy -= prob * Log2(prob);
            }
            return entropy;
        }
    }

    /**
     * A simple iterator for the bytes class, which does not support remove
     */
    static final class BytesIterator implements Iterator<Byte> {
        private final byte[] array;
        /**
         * Index of element to be returned by subsequent call to next.
         */
        private int cursor = 0;

        BytesIterator(byte[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return cursor != array.length;
        }

        @Override
        public Byte next() {
            try {
                int i = cursor;
                Byte next = array[i];
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("The Bytes iterator does not support removing");
        }
    }
}
