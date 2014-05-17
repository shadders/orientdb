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

import com.orientechnologies.orient.core.metadata.schema.OProperty;

/**
 * Responsible for selecting the appropriate IFieldSerializer for a property.  Essentially a factory
 * class, this enables easy switching of the serializer on a field level and potentially allows
 * different serialization based on attributes of the property.
 * 
 * @author Steve Coughlan
 *
 */
public abstract class FieldSerializerStrategy {
	
	/**
	 * Marker for unknown value to distinguish from null
	 */
	protected static final Object UNKNOWN = new Object();
	
	public static FieldSerializerStrategy DEFAULT = new FieldSerializerStrategy() {
		public IFieldSerializer serializerFor(OProperty property, Object value) {
			return null;
			//return OBinarySerializerFactory.getInstance().getObjectSerializer(type);
		}
	};
	
	private static FieldSerializerStrategy instance = DEFAULT;
	
	private static boolean used = false;
	
	public static FieldSerializerStrategy get() {
		used = true;
		return instance;
	}

	public abstract IFieldSerializer serializerFor(OProperty property, Object value);
	
	public IFieldSerializer serializerFor(OProperty property) {
		return serializerFor(property, UNKNOWN);
	}
	
	public static void setStrategy(FieldSerializerStrategy strategy) {
		if (used)
			throw new IllegalStateException("Cannot set strategy after it has been retrieved with get()");
		instance = strategy;
	}

	
}
