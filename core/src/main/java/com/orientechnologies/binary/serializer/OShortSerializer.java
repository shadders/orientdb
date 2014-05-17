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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializer for {@link Short}.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OShortSerializer implements IFieldSerializer<Short> {

	public static OShortSerializer INSTANCE = new OShortSerializer();
	public static final byte ID = 12;

	/**
	 * size of short value in bytes
	 */
	public static final int SHORT_SIZE = 2;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return SHORT_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Short object) throws IOException {
		final short value = object;
		stream.write((byte) ((value >>> 8) & 0xFF));
		stream.write((byte) ((value >>> 0) & 0xFF));
		return SHORT_SIZE;
	}

	@Override
	public Short deserialize(String fieldName, byte[] stream, int offset, int length) {
		return (short) ((stream[offset] << 8) | (stream[offset + 1] & 0xff));
	}

}
