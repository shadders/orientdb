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
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OCharSerializer implements IFieldSerializer<Character> {
	private static final OBinaryConverter BINARY_CONVERTER = OBinaryConverterFactory.getConverter();

	/**
	 * size of char value in bytes
	 */
	public static final int CHAR_SIZE = 2;

	public static OCharSerializer INSTANCE = new OCharSerializer();
	public static final byte ID = 3;

	public int getObjectSize(final byte[] stream, final int startPosition) {
		return CHAR_SIZE;
	}

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return CHAR_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Character object) throws IOException {
		stream.write((byte) (object >>> 8));
		stream.write((byte) object.charValue());
		return CHAR_SIZE;
	}

	@Override
	public Character deserialize(String fieldName, byte[] stream, int offset, int length) {
		return (char) (((stream[offset] & 0xFF) << 8) + (stream[offset + 1] & 0xFF));
	}

}
