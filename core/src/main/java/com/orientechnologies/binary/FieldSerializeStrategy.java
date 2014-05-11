package com.orientechnologies.binary;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;

public abstract class FieldSerializeStrategy {
	
	public static FieldSerializeStrategy DEFAULT = new FieldSerializeStrategy() {
		public IFieldSerializer serializerFor(OType type) {
			return null;
			//return OBinarySerializerFactory.getInstance().getObjectSerializer(type);
		}
	};
	
	private static FieldSerializeStrategy instance = DEFAULT;
	
	private static boolean used = false;
	
	public static FieldSerializeStrategy get() {
		used = true;
		return instance;
	}

	public abstract IFieldSerializer serializerFor(OType type);
	
	public static void setStrategy(FieldSerializeStrategy strategy) {
		if (used)
			throw new IllegalStateException("Cannot set strategy after it has been retrieved with get()");
		instance = strategy;
	}
	
}
