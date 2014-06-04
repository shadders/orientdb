package com.orientechnologies.orient.core.record.impl;

import org.testng.annotations.Test;

import com.orientechnologies.binary.BinaryDocumentSerializer;

@Test
public class ODocumentBinarySerializationTest extends ODocumentSerializationTest{

	public ODocumentBinarySerializationTest() {
		super(new BinaryDocumentSerializer());
	}
	
}
