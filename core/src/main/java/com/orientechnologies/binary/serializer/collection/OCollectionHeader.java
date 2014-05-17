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

import java.util.ArrayList;
import java.util.List;

import com.orientechnologies.binary.IBinHeaderEntry;
import com.orientechnologies.binary.util.Varint;

/**
 * TODO not finished yet
 * 
 * @author Steve Coughlan
 *
 */
public class OCollectionHeader {

	private int size;
	private long dataLength;
	private List<IBinHeaderEntry> entries;
	
	private byte[] bytes;
	private int offset;
	
	private void parseHead() {
		size = Varint.readUnsignedVarInt(bytes, offset);
		offset += Varint.bytesLength(size);
		
		entries = new ArrayList(size);
		
		for (int i = 0; i < size; i++) {
			
		}
	}
	

}
