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

import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Serializer for boolean type .
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OBooleanSerializer implements IFieldSerializer {
	/**
	 * size of boolean value in bytes
	 */
	public static final int BOOLEAN_SIZE = 1;

	public static OBooleanSerializer INSTANCE = new OBooleanSerializer();
	public static final byte ID = 1;

	public int getObjectSize(Boolean object, Object... hints) {
		return BOOLEAN_SIZE;
	}

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return BOOLEAN_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Object object) throws IOException {
		Boolean bool;
		if (object instanceof Boolean)
			bool = (Boolean) object;
		else
			bool = (Boolean) OType.convert(object, Boolean.class);
		stream.write(Boolean.TRUE.equals(bool) ? 1 : 0);
		return BOOLEAN_SIZE;
	}

	@Override
	public Boolean deserialize(String fieldName, byte[] stream, int offset, int length) {
		return stream[offset] == 1;
	}
}
