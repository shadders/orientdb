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
import java.util.Calendar;
import java.util.Date;

/**
 * Serializer for {@link Date} type.
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 20.01.12
 */
public class ODateTimeSerializer implements IFieldSerializer<Date> {
	public static ODateTimeSerializer INSTANCE = new ODateTimeSerializer();
	public static final byte ID = 5;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return OLongSerializer.LONG_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, Date object) throws IOException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(object);
		OLongSerializer longSerializer = OLongSerializer.INSTANCE;
		longSerializer.serialize(stream, fieldName, calendar.getTimeInMillis());
		return OLongSerializer.LONG_SIZE;
	}

	@Override
	public Date deserialize(String fieldName, byte[] stream, int offset, int length) {
		Calendar calendar = Calendar.getInstance();
		OLongSerializer longSerializer = OLongSerializer.INSTANCE;
		calendar.setTimeInMillis(longSerializer.deserialize(fieldName, stream, offset, length));
		return calendar.getTime();
	}

}
