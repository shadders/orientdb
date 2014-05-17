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

package com.orientechnologies.binary.serializer.collection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import com.orientechnologies.binary.serializer.IFieldSerializer;

/**
 * TODO not finished yet
 * 
 * @author Steve Coughlan
 *
 * @param <E>
 */
public abstract class OCollectionSerializer<E> implements IFieldSerializer<Collection<E>> {

	

	public abstract byte getId();
	
	@Override
	public int serialize(OutputStream stream, String fieldName, Collection<E> col) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<E> deserialize(String fieldName, byte[] stream, int offset, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFixedLength() {
		return false;
	}

	@Override
	public int getFixedLength() {
		return -1;
	}

}
