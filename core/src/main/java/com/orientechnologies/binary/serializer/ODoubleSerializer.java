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
 * Serializer for {@link Double}
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 17.01.12
 */
public class ODoubleSerializer implements IFieldSerializer<Double> {

	public static ODoubleSerializer INSTANCE = new ODoubleSerializer();
	public static final byte ID = 6;

	/**
	 * size of double value in bytes
	 */
	public static final int DOUBLE_SIZE = 8;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return DOUBLE_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Double object) throws IOException {
		return OLongSerializer.INSTANCE.serialize(stream, fieldName, Double.doubleToLongBits(object));
	}

	@Override
	public Double deserialize(String fieldName, byte[] stream, int offset, int length) {
		return Double.longBitsToDouble(OLongSerializer.INSTANCE.deserialize(fieldName, stream, offset, length));
	}

}
