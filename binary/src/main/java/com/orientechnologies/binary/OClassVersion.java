package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.binary.util.BinUtils;
import com.orientechnologies.binary.util.CaselessString;
import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;

/**
 * 
 * 
 * @author Steve Coughlan
 * 
 */
public class OClassVersion extends OClassImpl {

	final private OBinaryClassSet schemaSet;

	private int schemaId;
	final private int version;
	private CaselessString className;

	/**
	 * Indexed by user defined order
	 */
	private List<OBinaryProperty> properties = new ArrayList();
	
	private List<OBinaryProperty> fixedLengthProperties;
	private List<OBinaryProperty> variableLengthProperties;

	// replace this with trove IntObjectMap
	// final private TIntObjectHashMap<OBinaryProperty> fieldIndex = new
	// TIntObjectHashMap();
	private Map<Integer, OBinaryProperty> fieldIndex = new HashMap();

	private boolean mutable = false;

	// /**
	// * properties should always be ordered with fixed length properties first.
	// *
	// * @param version
	// * @param className
	// * @param properties
	// */
	// public OClassVersion(int version, String className,
	// List<OBinaryProperty> fields) {
	// super();
	// this.version = version;
	// this.clazz = new CaselessString(className);;
	// this.properties = Collections.unmodifiableList(fields);
	// this.fixedLengthProperties = calculateFixedOffsets();
	// this.variableLengthProperties = collectVariableFields();
	// }

	OClassVersion(OBinaryClassSet schemaSet, int version) {
		super(new OSchemaShared(false));
		this.schemaSet = schemaSet;
		this.schemaId = schemaSet.getSchemaId();
		this.version = version;
		this.className = schemaSet.getClassName();
		setClassName();
	}

	private void setClassName() {
		setNameInternal(className.getCased());
	}

	public void addProperty(OBinaryProperty property) {
		property.setUserOrder(properties.size());
		properties.add(property);
	}

	public void makeImmutable() {
		properties = Collections.unmodifiableList(properties);
		calculateOffsets();
		//final pass to make the properties immutable
		for (OBinaryProperty property: properties)
			property.setMutable(false);
	}

	private void calculateOffsets() {

		fixedLengthProperties = new ArrayList(properties.size());
		variableLengthProperties = new ArrayList(properties.size());

		// calculate fixed field offsets and save them in eachpropertyIndex
		// OBinaryProperty
		int offset = 0;
		for (OBinaryProperty property : properties) {
			if (property.isSchemaless())
				continue;
			fieldIndex.put(property.getNameId(), property);
			if (BinUtils.isFixedLength(property.getType())) {
				property.setInternalOrder(fixedLengthPropertyCount());
				fixedLengthProperties.add(property);
				property.setInDataOffset(offset);
				property.setDataLength(BinUtils.fieldLength(property.getType()));
				offset += property.getDataLength();
			} else {
				variableLengthProperties.add(property);
				// unknown has to be read from record header.
				property.setInDataOffset(-1);
				property.setDataLength(-1);
			}
		}
		//set internal order for var length properties as we didn't know
		//fixedLengthPropertyCount() on the first pass.
		for (int i = 0; i < variableLengthPropertyCount(); i++) {
			variableLengthProperties.get(i).setInternalOrder(i + fixedLengthPropertyCount());
		}
		
		((ArrayList) fixedLengthProperties).trimToSize();
		fixedLengthProperties = Collections.unmodifiableList(fixedLengthProperties);
		
		((ArrayList) variableLengthProperties).trimToSize();
		variableLengthProperties = Collections.unmodifiableList(variableLengthProperties);
		
	}

//	private List<OBinaryProperty> collectVariableFields() {
//
//		ArrayList<OBinaryProperty> variableLengthProperties = new ArrayList(properties.size());
//
//		for (OBinaryProperty property : properties) {
//			fieldIndex.put(property.getNameId(), property);
//			if (!BinUtils.isFixedLength(property.getType())) {
//				variableLengthProperties.add(property);
//				// unknown has to be read from record header.
//				property.setDataOffset(-1);
//				property.setDataLength(-1);
//			}
//		}
//		variableLengthProperties.trimToSize();
//		return Collections.unmodifiableList(variableLengthProperties);
//	}

	/**
	 * @return the parent schema for this class containing all versions of the schema
	 */
	public OBinaryClassSet getSchemaSet() {
		return schemaSet;
	}
	
	public PropertyIdProvider getIdProvider() {
		return schemaSet.getIdProvider();
	}
	
	/**
	 * number of fixed length properties.
	 * 
	 * @return
	 */
	public int fixedLengthPropertyCount() {
		return fixedLengthProperties.size();
	}

	/**
	 * number of variable length properties.
	 * 
	 * @return
	 */
	public int variableLengthPropertyCount() {
		return variableLengthProperties.size();
	}

	/**
	 * number of fixed length + variable length properties
	 * 
	 * @return
	 */
	public int propertyCount() {
		return properties.size();
	}

	
	public OBinaryProperty getField(String fieldName) {
		int nameId = schemaSet.getIdProvider().idFor(fieldName);
		return getField(nameId);
	}
	
	/**
	 * Gets field by nameId
	 * @param nameId
	 * @return
	 */
	public OBinaryProperty getField(int nameId) {
		return fieldIndex.get(nameId);
	}

	/**
	 * gets property by index i.e. the order in which it was added to the schema
	 * @param index
	 * @return
	 */
	public OBinaryProperty getProperty(int index) {
		return properties.get(index);
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the schemaId
	 */
	public int getSchemaId() {
		return schemaId;
	}

	/**
	 * @return the fixedLengthProperties
	 */
	public List<OBinaryProperty> getFixedLengthProperties() {
		return fixedLengthProperties;
	}

	/**
	 * @return the variableLengthProperties
	 */
	public List<OBinaryProperty> getVariableLengthProperties() {
		return variableLengthProperties;
	}

	void setMutable(boolean mutable) {
		checkMutable();
		this.mutable = mutable;
	}

	private void checkMutable() {
		if (!mutable)
			throw new RuntimeException("Attempt to modify immutable header entry");
	}

	public OClassVersion getMutableCopy() {
		// TODO do this manually do we can use the object pool
		OClassVersion clone = new OClassVersion(schemaSet, version);
		//clone.schemaSet = schemaSet;
		//clone.version = version;
		clone.schemaId = schemaId;
		clone.className = className;
		clone.properties = new ArrayList(properties.size());
		
		// never needs to be mutable;
		clone.fixedLengthProperties = fixedLengthProperties;
		clone.properties.addAll(fixedLengthProperties);
		
		clone.variableLengthProperties = new ArrayList(variableLengthProperties.size());
		
		for (OBinaryProperty property: variableLengthProperties) {
			OBinaryProperty mutable = property.getMutableCopy();
			clone.variableLengthProperties.add(property.getMutableCopy());
			clone.properties.add(mutable);
		}
		
		clone.fieldIndex = new HashMap();
		
		for (int i = 0; i < clone.properties.size(); i++) {
			OBinaryProperty prop = clone.properties.get(i);
			clone.fieldIndex.put(prop.getNameId(), clone.properties.get(i));
		}
		
		clone.mutable = true;
		return clone;
	}

}
