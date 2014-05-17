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
import java.nio.charset.Charset;

import com.orientechnologies.common.io.UnsafeByteArrayOutputStream;
import com.orientechnologies.orient.core.serialization.OBinaryProtocol;

/**
 * Serializer for {@link String} type.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OStringSerializer implements IFieldSerializer<String> {
	public static final OStringSerializer INSTANCE = new OStringSerializer();
	public static final byte ID = 13;
	public static final Charset charset = Charset.forName("UTF-8");

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return false;
	}

	public int getFixedLength() {
		throw new UnsupportedOperationException("Length of serialized string is not fixed.");
	}

//	public void serialize(final String object, final byte[] stream, int startPosition, Object... hints) {
//		int length = object.length();
//		OIntegerSerializer.INSTANCE.serialize(length, stream, startPosition);
//
//		startPosition += OIntegerSerializer.INT_SIZE;
//		char[] stringContent = new char[length];
//
//		object.getChars(0, length, stringContent, 0);
//
//		for (char character : stringContent) {
//			stream[startPosition] = (byte) character;
//			startPosition++;
//
//			stream[startPosition] = (byte) (character >>> 8);
//			startPosition++;
//		}
//	}
//
//	public String deserialize(final byte[] stream, int startPosition) {
//		int len = OIntegerSerializer.INSTANCE.deserialize(stream, startPosition);
//		char[] buffer = new char[len];
//
//		startPosition += OIntegerSerializer.INT_SIZE;
//
//		for (int i = 0; i < len; i++) {
//			buffer[i] = (char) ((0xFF & stream[startPosition]) | ((0xFF & stream[startPosition + 1]) << 8));
//			startPosition += 2;
//		}
//
//		return new String(buffer);
//	}

	@Override
	public int serialize(OutputStream stream, String fieldName, String object) throws IOException {
		if (stream instanceof UnsafeByteArrayOutputStream) {
			UnsafeByteArrayOutputStream out = (UnsafeByteArrayOutputStream) stream;
			int size = out.size();
			OBinaryProtocol.string2bytes(object, stream);
			return out.size() - size;
		} else {
			//return OBinaryTypeSerializer.INSTANCE.serialize(stream, fieldName, object.getBytes(charset));
			throw new RuntimeException("Cannot serialize String to " + stream.getClass() + " and determine number of bytes written");
		}
	}

	@Override
	public String deserialize(String fieldName, byte[] stream, int offset, int length) {
		//return new String(Arrays.copyOfRange(stream, offset, length), charset);
		return OBinaryProtocol.bytes2string(stream, offset, length);
	}

}
