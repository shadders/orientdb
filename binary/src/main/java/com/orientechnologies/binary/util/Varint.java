/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Modified from original source at: http://svn.apache.org/repos/asf/mahout/trunk/core/src/main/java/org/apache/mahout/math/Varint.java
 */

package com.orientechnologies.binary.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * <p>
 * Encodes signed and unsigned values using a common variable-length scheme,
 * found for example in <a
 * href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
 * Google's Protocol Buffers</a>. It uses fewer bytes to encode smaller values,
 * but will use slightly more bytes to encode large values.
 * </p>
 * 
 * <p>
 * Signed values are further encoded using so-called zig-zag encoding in order
 * to make them "compatible" with variable-length encoding.
 * </p>
 * 
 * Modified from original source at: http://svn.apache.org/repos/asf/mahout/trunk/core/src/main/java/org/apache/mahout/math/Varint.java
 */
public final class Varint {

	public static void main(String[] args) {
		int num = -64;
		System.out.println("len1: " + bytesLength(num));
		System.out.println("len2: " + writeSignedVarInt(num, new ByteArrayOutputStream()));
		
	}
	
	private Varint() {
	}

	/**
	 * Return the length in bytes of the given number when encoded
	 * 
	 *FIXME handled signed/unsigned properly
	 * 
	 * @param value
	 * @return
	 */
	public static int bytesLength(int value) {
		if (value < 128)
			return 1;
		if (value < 16384)
			return 2;
		if (value < 2097152)
			return 3;
		if (value < 268435456)
			return 4;
		return 5;
	}
	
	/**
	 * Return the length in bytes of the given number when encoded
	 * 
	 * FIXME handled signed/unsigned properly
	 * 
	 * @param value
	 * @return
	 */
	public static int bytesLength(long value) {
		if (value < 0x80l) //2^7
			return 1;
		if (value < 0x4000l) //2^14
			return 2;
		if (value < 0x200000l) //2^21
			return 3;
		if (value < 0x10000000l) //2^28
			return 4;
		if (value < 0x1000000000l) //2^35
			return 5;
		if (value < 0x40000000000l) //2^42
			return 6;
		if (value < 0x2000000000000l) //2^49
			return 7;
		return 8;
	}
	
	/**
	 * Encodes a value using the variable-length encoding from <a
	 * href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
	 * Google Protocol Buffers</a>. It uses zig-zag encoding to efficiently
	 * encode signed values. If values are known to be nonnegative,
	 * {@link #writeUnsignedVarLong(long, DataOutput)} should be used.
	 * 
	 * @param value
	 *            value to encode
	 * @param out
	 *            to write bytes to
	 * @throws IOException
	 *             if {@link DataOutput} throws {@link IOException}
	 */
	public static void writeSignedVarLong(long value, ByteArrayOutputStream bos) {
		// Great trick from
		// http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
		writeUnsignedVarLong((value << 1) ^ (value >> 63), bos);
	}

	/**
	 * Encodes a value using the variable-length encoding from <a
	 * href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
	 * Google Protocol Buffers</a>. Zig-zag is not used, so input must not be
	 * negative. If values can be negative, use
	 * {@link #writeSignedVarLong(long, DataOutput)} instead. This method treats
	 * negative input as like a large unsigned value.
	 * 
	 * @param value
	 *            value to encode
	 * @param out
	 *            to write bytes to
	 *  @return the number of bytes written
	 * @throws IOException
	 *             if {@link DataOutput} throws {@link IOException}
	 */
	public static int writeUnsignedVarLong(long value, ByteArrayOutputStream bos) {
		int size = 1;
		while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
			//out.writeByte(((int) value & 0x7F) | 0x80);
			bos.write(((int) value & 0x7F) | 0x80);
			value >>>= 7;
			size++;
		}
		//out.writeByte((int) value & 0x7F);
		bos.write((int) value & 0x7F);
		return size;
	}
	
	/**
	 * Encodes a value using the variable-length encoding from <a
	 * href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
	 * Google Protocol Buffers</a>. Zig-zag is not used, so input must not be
	 * negative. If values can be negative, use
	 * {@link #writeSignedVarLong(long, DataOutput)} instead. This method treats
	 * negative input as like a large unsigned value.
	 * 
	 * @param value
	 *            value to encode
	 * @param out
	 *            to write bytes to
	 * @return the number of bytes written
	 * @throws IOException
	 *             if {@link DataOutput} throws {@link IOException}
	 */
	public static int writeUnsignedVarLong(long value, byte[] bytes, int offset) {
		int size = 1;
		while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
			//out.writeByte(((int) value & 0x7F) | 0x80);
			bytes[offset++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
			size++;
		}
		//out.writeByte((int) value & 0x7F);
		bytes[offset++] = (byte) (value & 0x7F);
		return size;
	}

	/**
	 * @see #writeSignedVarLong(long, DataOutput)
	 */
	public static int writeSignedVarInt(int value, ByteArrayOutputStream bos) {
		// Great trick from
		// http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
		return writeUnsignedVarInt((value << 1) ^ (value >> 31), bos);
	}

	/**
	 * @see #writeUnsignedVarLong(long, DataOutput)
	 */
	public static int writeUnsignedVarInt(int value, ByteArrayOutputStream bos) {
		int size = 1;
		while ((value & 0xFFFFFF80) != 0L) {
			//out.writeByte((value & 0x7F) | 0x80);
			bos.write((value & 0x7F) | 0x80);
			value >>>= 7;
			size++;
		}
		//out.writeByte(value & 0x7F);
		bos.write(value & 0x7F);
		return size;
	}
	
	/**
	 * @see #writeUnsignedVarLong(long, DataOutput)
	 */
	public static int writeUnsignedVarInt(int value, byte[] bytes, int offset) {
		int size = 1;
		while ((value & 0xFFFFFF80) != 0L) {
			//out.writeByte((value & 0x7F) | 0x80);
			bytes[offset++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
			size++;
		}
		//out.writeByte(value & 0x7F);
		bytes[offset] = (byte) (value & 0x7F);
		return size;
	}

	/**
	 * @param in
	 *            to read bytes from
	 * @return decode value
	 * @throws IOException
	 *             if {@link DataInput} throws {@link IOException}
	 * @throws IllegalArgumentException
	 *             if variable-length value does not terminate after 9 bytes
	 *             have been read
	 * @see #writeSignedVarLong(long, DataOutput)
	 */
	public static long readSignedVarLong(byte[] bytes, int offset) {
		long raw = readUnsignedVarLong(bytes, offset);
		// This undoes the trick in writeSignedVarLong()
		long temp = (((raw << 63) >> 63) ^ raw) >> 1;
		// This extra step lets us deal with the largest signed values by
		// treating
		// negative results from read unsigned methods as like unsigned values
		// Must re-flip the top bit if the original read value had it set.
		return temp ^ (raw & (1L << 63));
	}

	/**
	 * @param in
	 *            to read bytes from
	 * @return decode value
	 * @throws IOException
	 *             if {@link DataInput} throws {@link IOException}
	 * @throws IllegalArgumentException
	 *             if variable-length value does not terminate after 9 bytes
	 *             have been read
	 * @see #writeUnsignedVarLong(long, DataOutput)
	 */
	public static long readUnsignedVarLong(byte[] bytes, int offset) {
		long value = 0L;
		int i = 0;
		long b;
		while (((b = bytes[offset++]) & 0x80L) != 0) {
			value |= (b & 0x7F) << i;
			i += 7;
			if (i > 63)
				throw new IllegalArgumentException("Variable length quantity is too long (must be <= 63)");
		}
		return value | (b << i);
	}

	/**
	 * @throws IllegalArgumentException
	 *             if variable-length value does not terminate after 5 bytes
	 *             have been read
	 * @throws IOException
	 *             if {@link DataInput} throws {@link IOException}
	 * @see #readSignedVarLong(DataInput)
	 */
	public static int readSignedVarInt(byte[] bytes, int offset) {
		int raw = readUnsignedVarInt(bytes, offset);
		// This undoes the trick in writeSignedVarInt()
		int temp = (((raw << 31) >> 31) ^ raw) >> 1;
		// This extra step lets us deal with the largest signed values by
		// treating
		// negative results from read unsigned methods as like unsigned values.
		// Must re-flip the top bit if the original read value had it set.
		return temp ^ (raw & (1 << 31));
	}

	/**
	 * @throws IllegalArgumentException
	 *             if variable-length value does not terminate after 5 bytes
	 *             have been read
	 * @throws IOException
	 *             if {@link DataInput} throws {@link IOException}
	 * @see #readUnsignedVarLong(DataInput)
	 */
	public static int readUnsignedVarInt(byte[] bytes, int offset) {
		int value = 0;
		int i = 0;
		int b;
		while (((b = bytes[offset++]) & 0x80) != 0) {
			value |= (b & 0x7F) << i;
			i += 7;
			if (i > 35)
				throw new IllegalArgumentException("Variable length quantity is too long (must be <= 35)");

		}
		return value | (b << i);
	}

}
