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

import com.orientechnologies.binary.OBinaryDocument;
import com.orientechnologies.orient.core.record.ORecordInternal;

/**
 * Serializer for boolean type .
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 18.01.12
 */
public class OEmbeddedSerializer implements IFieldSerializer<ORecordInternal> {
	
	public static OEmbeddedSerializer INSTANCE = new OEmbeddedSerializer();
	/**
	 * FIXME work out what value this should be.
	 */
	public static final byte ID = 99;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return false;
	}

	public int getFixedLength() {
		return -1;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, ORecordInternal object) throws IOException {
		byte[] bytes = object.toStream();
		stream.write(bytes);
		return bytes.length;
	}

	@Override
	public ORecordInternal deserialize(String fieldName, byte[] stream, int offset, int length) {
		if (length == 0)
			return null;
		OBinaryDocument doc = new OBinaryDocument(Arrays.copyOfRange(stream, offset, offset + length));
		return doc;
	}
}
