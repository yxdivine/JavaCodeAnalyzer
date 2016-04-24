package main;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class DBHelper {
	static GraphDatabaseFactory factory = new GraphDatabaseFactory();
	static private GraphDatabaseService graphDB;
	private static String DB_PATH = "db/";

	private static enum RelTypes implements RelationshipType {
		IMPLEMENTS, EXTENDS, CALLS, REF
	}

	public static void startDB() {
		clearDB();
		graphDB = factory.newEmbeddedDatabase(new File(DB_PATH));

		registerShutdownHook(graphDB);
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
		 Node startNode = graphDB.createNode();
		 
		 
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
