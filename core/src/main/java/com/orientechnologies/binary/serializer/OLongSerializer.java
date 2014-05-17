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
 * Serializer for {@link Long} type.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OLongSerializer implements IFieldSerializer<Long> {
	private static final OBinaryConverter CONVERTER = OBinaryConverterFactory.getConverter();

	public static OLongSerializer INSTANCE = new OLongSerializer();
	public static final byte ID = 10;

	/**
	 * size of long value in bytes
	 */
	public static final int LONG_SIZE = 8;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return LONG_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Long object) throws IOException {
		final long value = object;
		stream.write((byte) ((value >>> 56) & 0xFF));
		stream.write((byte) ((value >>> 48) & 0xFF));
		stream.write((byte) ((value >>> 40) & 0xFF));
		stream.write((byte) ((value >>> 32) & 0xFF));
		stream.write((byte) ((value >>> 24) & 0xFF));
		stream.write((byte) ((value >>> 16) & 0xFF));
		stream.write((byte) ((value >>> 8) & 0xFF));
		stream.write((byte) ((value >>> 0) & 0xFF));
		return LONG_SIZE;
	}

	@Override
	public Long deserialize(String fieldName, byte[] stream, int offset, int length) {
		return ((0xff & stream[offset + 7]) | (0xff & stream[offset + 6]) << 8 | (0xff & stream[offset + 5]) << 16
				| (long) (0xff & stream[offset + 4]) << 24 | (long) (0xff & stream[offset + 3]) << 32
				| (long) (0xff & stream[offset + 2]) << 40 | (long) (0xff & stream[offset + 1]) << 48 | (long) (0xff & stream[offset]) << 56);
	}

}
