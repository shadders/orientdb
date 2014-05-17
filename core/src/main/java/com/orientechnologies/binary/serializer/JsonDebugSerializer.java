/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.binary.serializer.FieldSerializerStrategy;
import com.orientechnologies.binary.serializer.IFieldSerializer;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * A json serializer that stores field name and value for easier debug.
 * 
 * @author Steve Coughlan
 *
 */
public class JsonDebugSerializer implements IFieldSerializer {

	private static ObjectMapper mapper = new ObjectMapper();
	private static JsonDebugSerializer instance = new JsonDebugSerializer();
	
	@Override
	public int serialize(OutputStream stream, String fieldName, Object object) throws IOException {
		byte[] bytes;
		try {
			Map value = new HashMap();
			value.put(fieldName == null ? "val" : fieldName, object);
			bytes = mapper.writeValueAsBytes(value);
			//return bytes;
			bytes = ("\r\n" + new String(bytes) + "\r\n").getBytes();
			stream.write(bytes);
			return bytes.length;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object deserialize(String fieldName, byte[] stream, int offset, int length) {
		String string = new String(stream, offset, length);
		if (length == 0)
			return null;
		try {
			Map value = mapper.readValue(stream, offset, length, Map.class);
			return value.get(fieldName == null ? "val" : fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isFixedLength() {
		return false;
	}

	@Override
	public int getFixedLength() {
		return -1;
	}
	
	public static FieldSerializerStrategy JSON_STRATEGY = new FieldSerializerStrategy() {
		
		@Override
		public IFieldSerializer serializerFor(OProperty property, Object value) {
			return instance;
		}
	};

}