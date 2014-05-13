package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.binary.util.BinUtils;
import com.orientechnologies.binary.util.CaselessString;
import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapperNoClass;

/**
 * 
 * 
 * @author Steve Coughlan
 * 
 */
public class OClassVersion extends ODocumentWrapperNoClass {

	protected OClassSet classSet;
	//final protected OSchemaShared iOwner;

	private int classId;
	private int version;
	protected CaselessString className;

	/**
	 * Indexed by user defined order
	 */
	private List<OBinProperty> properties = new ArrayList();
	
	private List<OBinProperty> fixedLengthProperties;
	private List<OBinProperty> variableLengthProperties;

	// replace this with trove IntObjectMap
	// final private TIntObjectHashMap<OBinProperty> fieldIndex = new
	// TIntObjectHashMap();
	private Map<Integer, OBinProperty> fieldIndex = new HashMap();

	private boolean mutable = false;

	// /**
	// * properties should always be ordered with fixed length properties first.
	// *
	// * @param version
	// * @param className
	// * @param properties
	// */
	// public OClassVersion(int version, String className,
	// List<OBinProperty> fields) {
	// super();
	// this.version = version;
	// this.clazz = new CaselessString(className);;
	// this.properties = Collections.unmodifiableList(fields);
	// this.fixedLengthProperties = calculateFixedOffsets();
	// this.variableLengthProperties = collectVariableFields();
	// }

	
	
	OClassVersion(OClassSet classSet, int version, Collection<OBinProperty> properties) {
		//super(iOwner, iName, iClusterIds);
		//this.iOwner = iOwner;
		this.version = version;
		if (classSet != null)
			setClassSet(classSet);
		for (OBinProperty property: properties)
			addProperty(property);
		makeImmutable();
	}
	
//	/**
//	 * for unmarshalling from stored schema
//	 * @param iOwner
//	 * @param document
//	 */
//	OClassVersion(final OSchemaShared iOwner, ODocument document) {
//		super(iOwner, document);
//		this.iOwner = iOwner;
//	}
	
	OClassVersion(OClassSet classSet) {
		super();
		//this.iOwner = null;
		this.version = 0;
		//this.name = className.getCased();
		setClassSet(classSet);
	}
	
	void setClassSet(OClassSet classSet) {
		this.classSet = classSet;
		this.classId = classSet.getClassId();
		this.className = classSet.getClassName();
	}
	

//	private void setClassName() {
//		setNameInternal(className.getCased());
//	}
	
//	public void addProperty(OBinProperty property) {
//		addProperty(property, true);
//	}
	
	private void addProperty(OBinProperty property) {
		property.setMutableInternal(true);
		property.setUserOrder(properties.size());
		int nameId = getClassSet().idFor(property.getName());
		property.setNameId(nameId);
		properties.add(property);
//		if (addToSuper)
//			super.addProperty(property.getName(), property.getType(), null, null);
		
	}

	public void makeImmutable() {
		properties = Collections.unmodifiableList(properties);
		calculateOffsets();
		//final pass to make the properties immutable
		for (OBinProperty property: properties)
			property.setMutable(false);
	}

	private void calculateOffsets() {

		fixedLengthProperties = new ArrayList(properties.size());
		variableLengthProperties = new ArrayList(properties.size());

		// calculate fixed field offsets and save them in eachpropertyIndex
		// OBinProperty
		int offset = 0;
		for (OBinProperty property : properties) {
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

//	private List<OBinProperty> collectVariableFields() {
//
//		ArrayList<OBinProperty> variableLengthProperties = new ArrayList(properties.size());
//
//		for (OBinProperty property : properties) {
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
	public OClassSet getClassSet() {
		return classSet;
	}
	
//	public IPropertyIdProvider getIdProvider() {
//		return classSet.getIdProvider();
//	}
	
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

	
	public OBinProperty getField(String fieldName) {
		int nameId = classSet.idFor(fieldName);
		return getField(nameId);
	}
	
	/**
	 * Gets field by nameId
	 * @param nameId
	 * @return
	 */
	public OBinProperty getField(int nameId) {
		return fieldIndex.get(nameId);
	}

	/**
	 * gets property by index i.e. the order in which it was added to the schema
	 * @param index
	 * @return
	 */
	public OBinProperty getProperty(int index) {
		return properties.get(index);
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the classId
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * @return the fixedLengthProperties
	 */
	public List<OBinProperty> getFixedLengthProperties() {
		return fixedLengthProperties;
	}

	/**
	 * @return the variableLengthProperties
	 */
	public List<OBinProperty> getVariableLengthProperties() {
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
		OClassVersion clone = new OClassVersion(classSet);
		
		clone.version = version;
		clone.classId = classId;
		clone.className = className;
		clone.properties = new ArrayList(properties.size());
		
		// never needs to be mutable;
		clone.fixedLengthProperties = fixedLengthProperties;
		clone.properties.addAll(fixedLengthProperties);
		
		clone.variableLengthProperties = new ArrayList(variableLengthProperties.size());
		
		for (OBinProperty property: variableLengthProperties) {
			OBinProperty mutable = property.getMutableCopy();
			clone.variableLengthProperties.add(property.getMutableCopy());
			clone.properties.add(mutable);
		}
		
		clone.fieldIndex = new HashMap();
		
		for (int i = 0; i < clone.properties.size(); i++) {
			OBinProperty prop = clone.properties.get(i);
			clone.fieldIndex.put(prop.getNameId(), clone.properties.get(i));
		}
		
		clone.mutable = true;
		return clone;
	}
	
	@Override
	protected void fromStream() {
		version = document.field("version", OType.INTEGER);
		//classId = document.field("classId", OType.INTEGER);
		//className = new CaselessString(document.field("className", OType.STRING).toString());
		
		for (OBinProperty property: properties) {
			addProperty(property);
		}
		
		Object obj = document.field("properties", OType.EMBEDDEDLIST);
		if (obj instanceof Collection<?>) {
			final Collection<ODocument> col = (Collection<ODocument>) obj;
			properties = new ArrayList(col.size());
			for (ODocument propertyDoc : col) {
				OBinProperty property = new OBinProperty(getClassSet());
				property.fromStream(propertyDoc);
				addProperty(property);
	      }
		}
		
		makeImmutable();
	}

	@Override
	public ODocument toStream() {
		
		document = new ODocument();
		document.field("version", version, OType.INTEGER);
		//document.field("classId", classId, OType.INTEGER);
		//document.field("className", className.getCased(), OType.STRING);
		
		final List<ODocument> propertyDocs = new ArrayList<ODocument>();
		for (OBinProperty property: properties) {
			propertyDocs.add(property.toStream());
		}
		document.field("properties", propertyDocs, OType.EMBEDDEDLIST);
		
		return document;
	}
	
	/**
	 * Allows OClassSet to bypass marshalling that occurs specific to the version
	 * @return
	 */
	ODocument superToStream() {
		return super.toStream();
	}

	public String toString() {
		return String.format("%s[v:%s]", classSet, version);
	}
	
}
