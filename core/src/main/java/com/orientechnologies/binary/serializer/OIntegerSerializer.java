/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.binary.serializer;

import com.orientechnologies.common.serialization.OBinaryConverter;
import com.orientechnologies.common.serialization.OBinaryConverterFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializer for {@link Integer} type.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 17.01.12
 */
public class OIntegerSerializer implements IFieldSerializer<Integer> {
	private static final OBinaryConverter CONVERTER = OBinaryConverterFactory.getConverter();

	public static OIntegerSerializer INSTANCE = new OIntegerSerializer();
	public static final byte ID = 8;

	/**
	 * size of int value in bytes
	 */
	public static final int INT_SIZE = 4;

	public int getObjectSize(byte[] stream, int startPosition) {
		return INT_SIZE;
	}

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return INT_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Integer object) throws IOException {
		final int value = object;
		stream.write((byte) ((value >>> 24) & 0xFF));
		stream.write((byte) ((value >>> 16) & 0xFF));
		stream.write((byte) ((value >>> 8) & 0xFF));
		stream.write((byte) ((value >>> 0) & 0xFF));
		return INT_SIZE;
	}

	@Override
	public Integer deserialize(String fieldName, byte[] stream, int offset, int length) {
		return (stream[offset]) << 24 | (0xff & stream[offset + 1]) << 16 | (0xff & stream[offset + 2]) << 8
				| ((0xff & stream[offset + 3]));
	}

}
