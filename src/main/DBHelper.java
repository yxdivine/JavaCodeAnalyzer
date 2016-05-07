package main;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.io.fs.FileUtils;

public class DBHelper {
	private static final String PRIMARY_KEY = "name";
	private static String DB_PATH = "db/";

	static private GraphDatabaseService graphDB;

	private static IndexManager index;
	private static Index<Node> interfaces;
	private static Index<Node> packages;
	private static Index<Node> classes;
	private static Index<Node> empty;

	private static enum RelTypes implements RelationshipType {
		IMPLEMENTS, EXTENDS, CALLS, REF
	}

	public static void startDB() {
		clearDB();
		graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(
				DB_PATH));
		index = graphDB.index();
		registerShutdownHook(graphDB);
		Transaction tx = graphDB.beginTx();
		try {
			interfaces = index.forNodes("interface");
			packages = index.forNodes("package");
			classes = index.forNodes("class");
			empty = index.forNodes("empty");

			tx.success();
		} finally {
			tx.close();
		}

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDB2) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDB.shutdown();
			}
		});
	}

	public static void addNode(String nid) {
		Transaction tx = graphDB.beginTx();
		try {
			Node emptyNode = graphDB.createNode();
			emptyNode.setProperty("nid", nid);

			empty.add(emptyNode, "nid", nid);
			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void addInterfaceNode(String interfaceName, String id) {
		Transaction tx = graphDB.beginTx();
		try {
			Node newInterface = graphDB.createNode();
			newInterface.setProperty(PRIMARY_KEY, interfaceName);
			newInterface.setProperty("nid", id);
			interfaces.add(newInterface, PRIMARY_KEY, interfaceName);
			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void addPackageNode(String pName, String id) {
		Transaction tx = graphDB.beginTx();
		try {
			Node newPackage = graphDB.createNode();
			newPackage.setProperty(PRIMARY_KEY, pName);
			newPackage.setProperty("nid", id);
			packages.add(newPackage, PRIMARY_KEY, pName);
			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void addClassNode(String cName, String id) {
		Transaction tx = graphDB.beginTx();
		try {
			Node newClass = graphDB.createNode();
			newClass.setProperty(PRIMARY_KEY, cName);
			newClass.setProperty("nid", id);
			classes.add(newClass, PRIMARY_KEY, cName);
			tx.success();
		} finally {
			tx.close();
		}
	}

	public static boolean nodeExists(String nid) {
		Transaction tx = graphDB.beginTx();
		Node found;
		try {
			found = interfaces.get("nid", nid).getSingle();
			tx.success();
		} finally {
			tx.close();
		}
		return (found != null);
	}
	
	public static void findExtendsNode(String nid,String curid) {
		Transaction tx = graphDB.beginTx();
		try {
			Node found = interfaces.get("nid", nid).getSingle();
			if (found == null) {
				found = classes.get("nid", nid).getSingle();
			}
			//class和interface都没找到，暂存
			found = graphDB.createNode();
			found.setProperty("nid", nid);
			empty.add(found, nid, nid);
			Node current = classes.get("nid", curid).getSingle();
			current.createRelationshipTo(found, RelTypes.EXTENDS);
			tx.success();
		} finally {
			tx.close();
		}
	}

	private static void clearDB() {
		try {
			FileUtils.deleteRecursively(new File(DB_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void shutdown() {
		graphDB.shutdown();
	}
}
