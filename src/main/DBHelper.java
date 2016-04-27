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
		try{
			interfaces = index.forNodes("interface");
			tx.success();
		}finally{
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

	public static void addNode() {
		Transaction tx = graphDB.beginTx();
		try {
			Node testNode = graphDB.createNode();
			testNode.setProperty("hello", "world");
			interfaces.add(testNode, PRIMARY_KEY, "helloworld");
			tx.success();
		} finally {
			tx.close();
		}
	}
	
	public static void addInterfaceNode(String interfaceName) {
		Transaction tx = graphDB.beginTx();
		try {
			Node newInterface = graphDB.createNode();
			newInterface.setProperty(PRIMARY_KEY, interfaceName);
			interfaces.add(newInterface, PRIMARY_KEY, interfaceName);
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
