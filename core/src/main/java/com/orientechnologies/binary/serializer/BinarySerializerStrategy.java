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
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Serializer strategy for binary serialization.  Falls back to Jackson JSON serialization
 * if and appropriate serializer can't be found
 * 
 * @author Steve Coughlan
 *
 */
public class BinarySerializerStrategy extends FieldSerializerStrategy {

	@Override
	public IFieldSerializer serializerFor(OProperty property, Object value) {
		
		OType type = property.getType();
		
		if (type != null && type.isMultiValue()) {
			//collection
			type = property.getLinkedType();
			
			//TODO handle this
		}
		
		if (type == null || type == OType.ANY) {
			if (value == null)
				return ONullSerializer.INSTANCE;
			type =  OType.getTypeByClass(value.getClass());
		}
			
		switch (type) {
		case ANY:
			return serializerFor(value);
		case BYTE:
			return OByteSerializer.INSTANCE;
		case SHORT:
			return OShortSerializer.INSTANCE;
		case INTEGER:
			return OIntegerSerializer.INSTANCE;
		case LONG:
			return OLongSerializer.INSTANCE;
		case FLOAT:
			return OFloatSerializer.INSTANCE;
		case DOUBLE:
			return ODoubleSerializer.INSTANCE;
		case STRING:
			return OStringSerializer.INSTANCE;
		case DECIMAL:
			return ODecimalSerializer.INSTANCE;
		case DATE:
			return ODateSerializer.INSTANCE;
		case DATETIME:
			return ODateTimeSerializer.INSTANCE;
		case BOOLEAN:
			return OBooleanSerializer.INSTANCE;
		case BINARY:
			return OBinaryTypeSerializer.INSTANCE;
		case LINK:
			return OLinkSerializer.INSTANCE;
		case EMBEDDED:
			return OEmbeddedSerializer.INSTANCE;
			
		default:
			return serializerFor(value);
		}
	}
	
	private IFieldSerializer serializerFor(Object value) {
		if (value instanceof Character)
			return OCharSerializer.INSTANCE;
		//fallback for any unhandled types
		return JsonSerializer.INSTANCE;
	}

}
