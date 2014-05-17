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
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.metadata.schema.OProperty;

/**
 * Json serializer.  Values are wrapped in a singleton list so will be stored as a Json Array.
 * @author Steve Coughlan
 *
 */
public class JsonSerializer implements IFieldSerializer {

	private static ObjectMapper mapper = new ObjectMapper();
	static JsonSerializer INSTANCE = new JsonSerializer();
	
	@Override
	public int serialize(OutputStream stream, String fieldName, Object object) throws IOException {
		byte[] bytes;
		try {
			/**
			 * TODO check if writeValueAsBytes really needs to be wrapped in a list or if it
			 * can take any type.
			 */
			List value = Collections.singletonList(object);
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
			List value = mapper.readValue(stream, offset, length, List.class);
			return value.get(0);
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
			return INSTANCE;
		}
	};

}