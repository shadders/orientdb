package com.orientechnologies.binary;
import java.util.List;

import com.orientechnologies.binary.util.CaselessString;


/**
 * A set of Schemas of different versions that belong to one class.   This class provides fast lookup
 * so that when a Record's first 'version' byte is read the correct OClassVersion can be obtained to read the
 * rest of the record.
 * 
 * @author Steve Coughlan
 *
 */
public class OBinaryClassSet {

	final private PropertyIdProvider idProvider;
	
	final private CaselessString className;
	private OClassVersion[] versions;
	private OClassVersion current;
	private int schemaId;
	
	
	/**
	 * Schemas should be in order of version.  i.e. version 0 should be at index 0 of the list.
	 * @param className
	 * @param oClassVersions
	 */
	public OBinaryClassSet(String className, List<OClassVersion> oClassVersions) {
		this.className = new CaselessString(className);
		this.idProvider = PropertyIdProvider.getForClass(className);
		this.versions = oClassVersions.toArray(new OClassVersion[oClassVersions.size()]);
		this.current = versions[versions.length - 1];
	}
	
	private OBinaryClassSet(String className) {
		this.className = new CaselessString(className);
		idProvider = PropertyIdProvider.getForClass(this.className.getCaseless());
	}
	
	public static OBinaryClassSet newSchemaSet(String className) {
		OBinaryClassSet set = new OBinaryClassSet(className);
		OSchemaIndex.addClass(set);
		OClassVersion version0 = new OClassVersion(set, 0);
		set.updateSchema(version0);
		return set;
	}

	/**
	 * @return the className
	 */
	public CaselessString getClassName() {
		return className;
	}

	public OClassVersion getSchema(int version) {
		return versions[version];
	}
	
	public OClassVersion currentSchema() {
		return current;
	}
	
	public void updateSchema(OClassVersion newSchema) {
		OClassVersion[] newVersions;
		if (versions == null) {
			newVersions = new OClassVersion[1];
		} else {
			newVersions = new OClassVersion[versions.length + 1];
			for (int i = 0; i < newVersions.length; i++) {
				newVersions[i] = versions[i];
			}
		}

		newVersions[newVersions.length - 1] = newSchema;
		current = newSchema;
		versions = newVersions;
		/**
		 * scan previous schemas and find any versions that are incompatible or will require forced
		 * update.  i.e. if a field has a constraint added to it like NOT_NULL we must scan all records
		 * of that oClassInstance version to verify the constraints are obeyed.
		 * 
		 * Not sure what behaviour is defined by Orient for non-conforming records?  Throw an error to user?
		 * Ignore?
		 */
		
		//TODO Persist the new schema
	}

	public PropertyIdProvider getIdProvider() {
		return idProvider;
	}

	/**
	 * @return the schemaId
	 */
	public int getSchemaId() {
		return schemaId;
	}

	/**
	 * @param schemaId the schemaId to set
	 */
	void setSchemaId(int schemaId) {
		this.schemaId = schemaId;
	}
	
	
}
