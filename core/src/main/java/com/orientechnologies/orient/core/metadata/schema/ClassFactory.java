package com.orientechnologies.orient.core.metadata.schema;

import com.orientechnologies.binary.OBinProperty;
import com.orientechnologies.binary.OClassSet;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * 
 * Factory class to enable easy switching of original implementation with binary serialization
 * implementation.
 * 
 * @author Steve Coughlan
 *
 */
public abstract class ClassFactory {

	public static ClassFactory get() {
		return instance;
	}
	
	public static void set(ClassFactory factory) {
		instance = factory;
	}
	
	public ClassFactory() {
	}

	public abstract OPropertyImpl newProperty(OClassImpl iOwner, ODocument iDocument);

	public abstract OPropertyImpl newProperty(OClassImpl oClassImpl, String iName, OType iType);
	
	public abstract OClassImpl newClass(final OSchemaShared iOwner, final String iName, final int[] iClusterIds);
	
	public abstract OClassImpl newClass(final OSchemaShared iOwner, ODocument document);
	
	public static final ClassFactory BINARY_FACTORY = new ClassFactory() {

		@Override
		public OPropertyImpl newProperty(OClassImpl iOwner, ODocument iDocument) {
			return new OBinProperty(iOwner, iDocument);
		}

		@Override
		public OPropertyImpl newProperty(OClassImpl iOwner, String iName, OType iType) {
			return new OBinProperty(iOwner, iName, iType);
		}

		@Override
		public OClassImpl newClass(OSchemaShared iOwner, String iName, int[] iClusterIds) {
			return new OClassSet(iOwner, iName, iClusterIds);
		}

		@Override
		public OClassImpl newClass(OSchemaShared iOwner, ODocument document) {
			return new OClassSet(iOwner, document);
		}
		
	};
	
	public static final ClassFactory LEGACY_FACTORY = new ClassFactory() {

		@Override
		public OPropertyImpl newProperty(OClassImpl iOwner, ODocument iDocument) {
			return new OPropertyImpl(iOwner, iDocument);
		}

		@Override
		public OPropertyImpl newProperty(OClassImpl iOwner, String iName, OType iType) {
			return new OPropertyImpl(iOwner, iName, iType);
		}

		@Override
		public OClassImpl newClass(OSchemaShared iOwner, String iName, int[] iClusterIds) {
			return new OClassImpl(iOwner, iName, iClusterIds);
		}

		@Override
		public OClassImpl newClass(OSchemaShared iOwner, ODocument document) {
			return new OClassImpl(iOwner, document);
		}
		
	};
	
	
	private static ClassFactory instance = BINARY_FACTORY;
	
}
