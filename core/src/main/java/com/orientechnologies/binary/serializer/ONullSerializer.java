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
 * Serialize and deserialize null values
 * <p/>
 * <a href="mailto:gmandnepr@gmail.com">Evgeniy Degtiarenko</a>
 * @author Steve Coughlan
 */
public class ONullSerializer implements IFieldSerializer<Object> {

	public static ONullSerializer INSTANCE = new ONullSerializer();
	public static final byte ID = 11;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return 0;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Object object) throws IOException {
		return 0;
	}

	@Override
	public Object deserialize(String fieldName, byte[] stream, int offset, int length) {
		return null;
	}

}
