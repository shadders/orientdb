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
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Serializer for {@link BigDecimal} type.
 * 
 * @author Andrey Lomakin
 * @author Steve Coughlan
 * @since 03.04.12
 */
public class ODecimalSerializer implements IFieldSerializer<BigDecimal> {
	public static final ODecimalSerializer INSTANCE = new ODecimalSerializer();
	public static final byte ID = 18;

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
	public int serialize(OutputStream stream, String fieldName, BigDecimal object) throws IOException {
		OIntegerSerializer.INSTANCE.serialize(stream, fieldName, object.scale());
		byte[] unscaled = object.unscaledValue().toByteArray();
		OBinaryTypeSerializer.INSTANCE.serialize(stream, fieldName, unscaled);
		return unscaled.length + OIntegerSerializer.INT_SIZE;
	}

	@Override
	public BigDecimal deserialize(String fieldName, byte[] stream, int offset, int length) {
		final int scale = OIntegerSerializer.INSTANCE.deserialize(fieldName, stream, offset, length);
		offset += OIntegerSerializer.INT_SIZE;
		length -= OIntegerSerializer.INT_SIZE;

		final byte[] unscaledValue = OBinaryTypeSerializer.INSTANCE.deserialize(fieldName, stream, offset, length);

		return new BigDecimal(new BigInteger(unscaledValue), scale);
	}

}
