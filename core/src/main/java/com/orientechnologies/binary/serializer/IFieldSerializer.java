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

/**
 * A serializer for an individual property.  There are already a number of collections of serializers
 * within OrientDB with varying interfaces.  This one assumes that a separate record header exists that
 * contains the offset and length of a data field and as such serialization of length within the data
 * is not necessary however it must enable detection of length in bytes written during serialzation.
 * 
 * This is returned as an int from the serialize method.  The current implementation using this interface
 * performs a check of size of the OutputStream before and after to verify the correct length is returned
 * however future usages of this interface may not use an implementation of OutputStream that is
 * capable of interrogating number of bytes written so this return value should be calculated properly.
 * 
 * @author Steve Coughlan
 *
 * @param <T>
 */
public interface IFieldSerializer<T> {

	/**
	 * 
	 * Serialize a single field object to a byte array
	 * 
	 * @param stream
	 * @param fieldName
	 * @param object
	 * @return number of bytes serialized
	 * @throws IOException
	 */
	public int serialize(OutputStream stream, String fieldName, T object) throws IOException;

	  /**
	   * Reads object from the stream starting from the startPosition
	   *
	   * @param stream        is the stream from object will be read
	   * @param offset is the position to start reading from
	   * @param length of the serialized data if known (read from the header)
	   * @return INSTANCE of the deserialized object
	   */
	public T deserialize(String fieldName, byte[] stream, int offset, int length);

	  /**
	   * @return <code>true</code> if binary presentation of object always has the same length.
	   */
	public boolean isFixedLength();

	  /**
	   * @return Length of serialized data if {@link #isFixedLength()} method returns <code>true</code>. If {@link #isFixedLength()}
	   *         method return <code>false</code> returned value is undefined.
	   */
	public int getFixedLength();

	
}
