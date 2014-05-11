package com.orientechnologies.binary.jackson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.binary.FieldSerializeStrategy;
import com.orientechnologies.binary.IFieldSerializer;
import com.orientechnologies.orient.core.metadata.schema.OType;


public class JsonSerializer implements IFieldSerializer {

	private static ObjectMapper mapper = new ObjectMapper();
	private static JsonSerializer instance = new JsonSerializer();
	
	@Override
	public byte[] serialize(String fieldName, Object object) {
		byte[] bytes;
		try {
			Map value = new HashMap();
			value.put(fieldName == null ? "val" : fieldName, object);
			bytes = mapper.writeValueAsBytes(value);
			//return bytes;
			return ("\r\n" + new String(bytes) + "\r\n").getBytes();
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object deserialize(String fieldName, byte[] stream, int offset, int length) {
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
	
	public static FieldSerializeStrategy JSON_STRATEGY = new FieldSerializeStrategy() {
		
		@Override
		public IFieldSerializer serializerFor(OType type) {
			return instance;
		}
	};

}