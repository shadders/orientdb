package com.orientechnologies.binary.runnabletest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.orientechnologies.binary.BinaryDocumentSerializer;
import com.orientechnologies.binary.OClassSet;
import com.orientechnologies.binary.OBinaryDocument;
import com.orientechnologies.binary.OBinProperty;
import com.orientechnologies.binary.OClassVersion;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

/**
 * 
 * @author Steve Coughlan
 *
 */
public class TestOdbBinMain {

	public static void main(String[] args) throws Exception {

		ORecordSerializerFactory.instance().register(BinaryDocumentSerializer.NAME, new BinaryDocumentSerializer());

		File dir = new File("/home/git/junk/rubbish/orientdb");
		if (dir.exists())
			for (File file : dir.listFiles())
				if (file.isFile())
					file.delete();

		ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getAbsolutePath()).create();// .open("admin",
																										// "admin");

		List<OBinProperty> fields = new ArrayList();
		fields.add(new OBinProperty());

		OClassSet schemaSet = OClassSet.newSchemaSet("employee");
		OClassVersion schema = schemaSet.currentSchema();
		schema.addProperty(new OBinProperty(schema, "fullname", OType.STRING));
		schema.addProperty(new OBinProperty(schema, "age", OType.INTEGER));
		schema.addProperty(new OBinProperty(schema, "dateofbirth", OType.DATE));
		schema.addProperty(new OBinProperty(schema, "nullString", OType.STRING));
		schema.addProperty(new OBinProperty(schema, "nullFloat", OType.FLOAT));
		schema.makeImmutable();

		db.getMetadata().getSchema().createClass(schema.getName());
		
		OBinaryDocument doc = new OBinaryDocument(schema);
		
		//OBinaryDocument doc = new OBinaryDocument();
		//ODocument doc = new ODocument();
		long _25Years = 25 * 1000l * 60 * 60 * 24 * 365;
		
		doc.field("fullname", "Luca Garulli", OType.STRING);
		doc.field("age", 25, OType.INTEGER);
		doc.field("dateofbirth", new Date(System.currentTimeMillis() - _25Years), OType.DATE);
		doc.field("nullString", (Object) null);
		doc.field("nullFloat", (Object) null);
		// startServer();
		doc.field("randomInt", 99, OType.SHORT);
		doc.field("randomString", "My Random String", OType.STRING);

		System.out.println("old: " + prettyDoc(doc));	
		
		byte[] bytes = new byte[100];
		BinaryDocumentSerializer ser = new BinaryDocumentSerializer();
		
		bytes = ser.toStream(doc, false);
		saveBytes(bytes, new File(dir, "data/serialized.bin"));
		OBinaryDocument newDoc = (OBinaryDocument) ser.fromStream(bytes, new OBinaryDocument());
		
		System.out.println("new: " + prettyDoc(newDoc));	
		
		OBinaryDocument partDoc1 = (OBinaryDocument) ser.fromStream(bytes, new OBinaryDocument(), 
				new String[] {"fullname", "randomInt", "age"});
		
		System.out.println("part1: " + prettyDoc(partDoc1));	
		
		OBinaryDocument partDoc2 = (OBinaryDocument) ser.fromStream(bytes, new OBinaryDocument(), 
				new String[] {"dateofbirth", "randomString", "nullFloat", "nullString", });
		
		System.out.println("part2: " + prettyDoc(partDoc2));	
		
		
		//db.save(doc, schema.getName());

		
		//doc = db.load(doc.getIdentity());
		
		//System.out.println(doc);		
		
		db.close();

	}

	private static String prettyDoc(ODocument doc) {
		List<String> fields = new ArrayList(Arrays.asList(doc.fieldNames()));
		Collections.sort(fields);
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getClassName()).append("{");
		boolean first = true;
		for (String field: fields) {
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(field).append(":");
			sb.append(doc.field(field));
		}
		sb.append("}");
		return sb.toString();
	}
	
	private static void saveBytes(byte[] bytes, File file) throws Exception {
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(file, false);
		out.write(bytes);
		out.close();
	}
	
	private static void startServer() throws Exception {
		OServer server = OServerMain.create();
		server.startup("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<orient-server>"
				+ "<network>"
				+ "<protocols>"
				+ "<protocol name=\"binary\" implementation=\"com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary\"/>"
				+ "<protocol name=\"http\" implementation=\"com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb\"/>"
				+ "</protocols>"
				+ "<listeners>"
				+ "<listener ip-address=\"0.0.0.0\" port-range=\"2424-2430\" protocol=\"binary\"/>"
				+ "<listener ip-address=\"0.0.0.0\" port-range=\"2480-2490\" protocol=\"http\"/>"
				+ "</listeners>"
				+ "</network>"
				+ "<users>"
				+ "<user name=\"root\" password=\"ThisIsA_TEST\" resources=\"*\"/>"
				+ "</users>"
				+ "<properties>"
				+ "<entry name=\"orientdb.www.path\" value=\"/home/git/junk/rubbish/orientdb\"/>"
				+ "<entry name=\"orientdb.config.file\" value=\"C:/work/dev/orientechnologies/orientdb/releases/1.0rc1-SNAPSHOT/config/orientdb-server-config.xml\"/>"
				+ "<entry name=\"server.cache.staticResources\" value=\"false\"/>"
				+ "<entry name=\"log.console.level\" value=\"info\"/>"
				+ "<entry name=\"log.file.level\" value=\"fine\"/>"
				// The following is required to eliminate an error or warning
				// "Error on resolving property: ORIENTDB_HOME"
				+ "<entry name=\"plugin.dynamic\" value=\"false\"/>" + "</properties>" + "</orient-server>");
		server.activate();
	}

}
