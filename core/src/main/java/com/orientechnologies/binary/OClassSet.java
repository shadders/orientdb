package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.binary.util.CaselessString;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * A set of classes of different versions that belong to one class. This class
 * provides fast lookup so that when a Record's first 'version' byte is read the
 * correct OClassVersion can be obtained to read the rest of the record.
 * 
 * FIXME It's a little bit back to front having OClassSet extend OClassVersion
 * when OClassSet is a logical parent. Look at whether we actually need
 * OClassVersion to be an implementation of OClass as it's mainly used to
 * consolidate properties and generate OBinRecordHeader.
 * 
 * @author Steve Coughlan
 * 
 */
public class OClassSet extends OClassVersion {

	// final private IPropertyIdProvider idProvider;

	private OClassVersion[] versions;
	private OClassVersion current;
	private int classId;

	/**
	 * List of field names, the indexes correspond to nameId for the property
	 * name.
	 */
	private final List<String> names = new ArrayList();

	/**
	 * Reverse lookup of nameId from field name.
	 */
	// private final TObjectIntMap<CaselessString> ids = new
	// TObjectIntHashMap<CaselessString>(10, 0.5f, -1);
	private final Map<String, Integer> ids = new HashMap<String, Integer>();

	/**
	 * To maintain index order any properties deleted from the OClassVersion are
	 * simply marked as available space (a hole) and reused for new properties.
	 */
	// private TIntLinkedList holes = new TIntLinkedList();
	private LinkedList<Integer> holes = new LinkedList();

	/**
	 * commented out as part of DODGY DRUNKEN EXPERIMENT
	 */
	// /**
	// * Schemas should be in order of version. i.e. version 0 should be at
	// index 0 of the list.
	// * @param className
	// * @param oClassVersions
	// */
	// public OClassSet(String className, List<OClassVersion> oClassVersions) {
	// this.className = new CaselessString(className);
	// this.idProvider = IPropertyIdProvider.getForClass(className);
	// this.versions = oClassVersions.toArray(new
	// OClassVersion[oClassVersions.size()]);
	// this.current = versions[versions.length - 1];
	// }

	/**
	 * Should only be called by OSchemaShared
	 * 
	 * @param iOwner
	 * @param iName
	 * @param iClusterIds
	 */
	public OClassSet(final OSchemaShared iOwner, final String iName, final int[] iClusterIds) {
		super(null, 0, iOwner, iName, iClusterIds);
		setClassSet(this);
		// idProvider =
		// IPropertyIdProvider.getForClass(this.className.getCaseless());
		OClassIndex.newClass(this);
		updateSchema(this);
	}

	/**
	 * Constructor used for creating the singleton classless class.
	 */
	OClassSet() {
		super();
		setClassSet(this);
		// idProvider =
		// IPropertyIdProvider.getForClass(this.className.getCaseless());
		OClassIndex.newClass(this);
		updateSchema(this);
		makeImmutable();
	}

	// public static OClassSet newSchemaSet(String className) {
	// OClassSet set = new OClassSet(className);
	// OClassIndex.addClass(set);
	// OClassVersion version0 = new OClassVersion(set, 0);
	// return set;
	// }

	/**
	 * for unmarshalling from stored schema
	 * @param iOwner
	 * @param document
	 */
	public OClassSet(final OSchemaShared iOwner, ODocument document) {
		super(iOwner, document);
		classSet = this;
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
		// return current;
		return this;
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
		 * scan previous schemas and find any versions that are incompatible or
		 * will require forced update. i.e. if a field has a constraint added to
		 * it like NOT_NULL we must scan all records of that oClassInstance
		 * version to verify the constraints are obeyed.
		 * 
		 * Not sure what behaviour is defined by Orient for non-conforming
		 * records? Throw an error to user? Ignore?
		 */

		// TODO Persist the new schema
	}

	// public IPropertyIdProvider getIdProvider() {
	// return idProvider;
	// }

	/**
	 * @return the classId
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * @param classId
	 *            the classId to set
	 */
	void setClassId(int classId) {
		this.classId = classId;
	}

	/**
	 * Provides the identity copy of the name for this id.
	 * 
	 * @param id
	 * @return
	 */
	public String nameFor(int id) {
		String string = names.get(id);
		return string;
	}

	/**
	 * Provides the nameId for the given property name
	 * 
	 * @param name
	 * @return
	 */
	public int idFor(String name) {
		Integer id = ids.get(name);
		return id == null ? newName(name) : id;
	}

	public synchronized int newName(String name) {

		int id = -1;

		if (holes.isEmpty()) {
			id = names.size();
			names.add(name);
		} else {
			id = holes.remove(0);
			names.set(id, name);
		}
		ids.put(name, id);

		return id;
	}

	@Override
	public void fromStream() {
		
		super.fromStream();

		// get extra fields
		classId = document.field("classId", OType.INTEGER);

		Object obj = document.field("fieldNames", OType.EMBEDDEDLIST);

		names.clear();
		ids.clear();
		
		if (obj instanceof Collection<?>) {
			final Collection<String> col = (Collection<String>) obj;
			for (final String name : col) {
				if (name != null)
					ids.put(name, names.size());
				names.add(name);
			}
		}
		
		obj = document.field("fieldNameHoles", OType.EMBEDDEDLIST);

		holes.clear();
		
//		if (obj instanceof Collection<?>) {
//			final Collection col = (Collection) obj;
//			for (final Object holesIndex : col) {
//				holes.add(new Integer(String.valueOf(holesIndex)));
//			}
//		}
		
		if (obj instanceof Collection<?>) {
			final Collection<Integer> col = (Collection) obj;
			for (final Integer holesIndex : col) {
				holes.add(holesIndex);
			}
		}
		
		obj = document.field("versions", OType.EMBEDDEDSET);
		if (obj instanceof Collection<?>) {
			final Collection<ODocument> col = (Collection<ODocument>) obj;
			versions = new OClassVersion[col.size()];
			for (ODocument versionDoc : col) {
				OClassVersion version = new OClassVersion(this, -1, iOwner, getName(), getClusterIds());
				version.fromStream(versionDoc);
				versions[version.getVersion()] = version;
	      }
		}
		
		OClassIndex.registerClass(this);
	}

	@Override
	public ODocument toStream() {
		
		ODocument document = super.toStream();

		/*
		 * FIXME this is a bit dodgy, super.toStream() does this as well.
		 */
		document.setInternalStatus(ORecordElement.STATUS.UNMARSHALLING);

		try {

			document.field("classId", classId, OType.INTEGER);
			document.field("fieldNames", names, OType.EMBEDDEDLIST);
			document.field("fieldNameHoles", holes, OType.EMBEDDEDLIST);
			
			final Set<ODocument> classVersions = new LinkedHashSet<ODocument>();
			if (!"".equals(className)) {
			for (OClassVersion version: versions) {
//				if (version instanceof OClassSet)
//					//schemaless class will be an OClassSet
//					//so avoid a stack overflow
//					continue;
				classVersions.add(version.toStream());
			}
			document.field("versions", classVersions, OType.EMBEDDEDSET);
			}

		} finally {
			document.setInternalStatus(ORecordElement.STATUS.LOADED);
		}
		return document;
	}

}
