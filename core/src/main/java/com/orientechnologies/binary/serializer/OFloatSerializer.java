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
 * Serializer for {@link Float} type.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OFloatSerializer implements IFieldSerializer<Float> {

	public static OFloatSerializer INSTANCE = new OFloatSerializer();
	public static final byte ID = 7;

	/**
	 * size of float value in bytes
	 */
	public static final int FLOAT_SIZE = 4;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return FLOAT_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Float object) throws IOException {
		return OIntegerSerializer.INSTANCE.serialize(stream, fieldName, Float.floatToIntBits(object));
	}

	@Override
	public Float deserialize(String fieldName, byte[] stream, int offset, int length) {
		return Float.intBitsToFloat(OIntegerSerializer.INSTANCE.deserialize(fieldName, stream, offset, length));
	}

}
