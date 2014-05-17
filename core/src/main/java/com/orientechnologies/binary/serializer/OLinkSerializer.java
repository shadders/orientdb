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

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.OClusterPositionFactory;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

/**
 * Serializer for
 * {@link com.orientechnologies.orient.core.metadata.schema.OType#LINK}
 * 
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @author Steve Coughlan
 * @since 07.02.12
 */
public class OLinkSerializer implements IFieldSerializer<OIdentifiable> {
	private static final int CLUSTER_POS_SIZE = OClusterPositionFactory.INSTANCE.getSerializedSize();

	public static OLinkSerializer INSTANCE = new OLinkSerializer();
	public static final byte ID = 9;
	public static final int RID_SIZE = OShortSerializer.SHORT_SIZE + CLUSTER_POS_SIZE;

	public byte getId() {
		return ID;
	}

	public boolean isFixedLength() {
		return true;
	}

	public int getFixedLength() {
		return RID_SIZE;
	}

	@Override
	public int serialize(OutputStream stream, String fieldName, OIdentifiable object) throws IOException {
		ORID r = object.getIdentity();
		OShortSerializer.INSTANCE.serialize(stream, fieldName, (short) r.getClusterId());
		OBinaryTypeSerializer.INSTANCE.serialize(stream, fieldName, r.getClusterPosition().toStream());
		return RID_SIZE;
	}

	@Override
	public OIdentifiable deserialize(String fieldName, byte[] stream, int offset, int length) {
		if (length == 0)
			return null;
		return new ORecordId(OShortSerializer.INSTANCE.deserialize(fieldName, stream, offset, length), 
				OClusterPositionFactory.INSTANCE.fromStream(stream, offset + OShortSerializer.SHORT_SIZE));
	}

}
