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
import java.util.Arrays;

import com.orientechnologies.common.serialization.OBinaryConverter;
import com.orientechnologies.common.serialization.OBinaryConverterFactory;

/**
 * Serializer for byte arrays .
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 20.01.12
 */
public class OBinaryTypeSerializer implements IFieldSerializer<byte[]> {
	private static final OBinaryConverter CONVERTER = OBinaryConverterFactory.getConverter();

	public static final OBinaryTypeSerializer INSTANCE = new OBinaryTypeSerializer();
	public static final byte ID = 17;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return false;
	}

	public int getFixedLength() {
		return 0;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, byte[] object) throws IOException {
		stream.write(object);
		return object.length;
	}

	@Override
	public byte[] deserialize(String fieldName, byte[] stream, int offset, int length) {
		return Arrays.copyOfRange(stream, offset, length);
	}

}
