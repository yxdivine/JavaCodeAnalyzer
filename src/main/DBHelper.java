package main;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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
	// 以id建立索引
	private static IndexManager index;
	private static Index<Node> interfaces;
	private static Index<Node> packages;
	private static Index<Node> classes;
	private static Index<Node> files;
	private static Index<Node> empty;

	private static Vector<String> rmList;

	private static enum RelTypes implements RelationshipType {
		IMPLEMENTS, EXTENDS, CALLS, REF, CHILDOF, TMPEXT
	}

	public static void startDB() {
		clearDB();
		rmList = new Vector<String>();
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
			files = index.forNodes("file");

			tx.success();
		} finally {
			tx.close();
		}

	}

	/**
	 * @param id
	 *            寻找的节点的id
	 * @param curid
	 *            出发节点的id
	 * @param curtype
	 *            出发节点的类型
	 */
	public static void findExtendNode(String id, String curid, String type) {
		Transaction tx = graphDB.beginTx();
		try {
			Node current = null;
			if (type.equals("class")) {
				current = classes.get("nid", curid).getSingle();
			} else if (type.equals("")) {
			}

			Node target = null;
			if (classes.get("nid", id).getSingle() != null) {// 在class中找到
				target = classes.get("nid", id).getSingle();
				current.createRelationshipTo(target, RelTypes.EXTENDS);

			} else {
				target = interfaces.get("nid", id).getSingle();
				if (target != null) {// 在interface中找到
					current.createRelationshipTo(target, RelTypes.IMPLEMENTS);
				} else if (empty.get("nid", id).getSingle() != null) {// 之前已经放进empty了
					target = empty.get("nid", id).getSingle();
					target.createRelationshipTo(current, RelTypes.TMPEXT);
				} else {// 都没找到，放入empty并加一个反向edge作为标记
					target = graphDB.createNode();
					target.setProperty("nid", id);
					empty.add(target, "nid", id);
					target.createRelationshipTo(current, RelTypes.TMPEXT);
				}
			}

			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void addFileNode(String name, String id) {
		Transaction tx = graphDB.beginTx();
		try {
			Node target = empty.get("nid", id).getSingle();
			if (target != null) {
				target.setProperty(PRIMARY_KEY, name);
				files.add(target, "nid", target.getProperty("nid"));
				empty.remove(target);
			} else {
				target = graphDB.createNode();
				target.setProperty(PRIMARY_KEY, name);
				target.setProperty("nid", id);
				files.add(target, "nid", id);
			}
			target.setProperty("type", "file");

			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void addClassNode(String classname, String id, String fileid) {
		Transaction tx = graphDB.beginTx();
		try {
			// 首先在数据库中寻找这个类的id是否已经被其他实体引用过了，如果有直接拿之前的node
			// 如果没有，创建一个新节点
			Node target = empty.get("nid", id).getSingle();
			if (target != null) {
				target.setProperty(PRIMARY_KEY, classname);
				empty.remove(target);
				classes.add(target, "nid", target.getProperty("nid"));
				for (Relationship r : target.getRelationships()) {
					if (r.isType(RelTypes.TMPEXT)) {
						Node parent = r.getEndNode();
						parent.createRelationshipTo(target, RelTypes.EXTENDS);
						r.delete();
					}
				}
			} else {
				target = graphDB.createNode();
				target.setProperty(PRIMARY_KEY, classname);
				target.setProperty("nid", id);
				classes.add(target, "nid", id);
			}
			target.setProperty("type", "class");
			if (fileid != null) {
				if (files.get("nid", fileid).getSingle() != null) {
					target.createRelationshipTo(files.get("nid", fileid)
							.getSingle(), RelTypes.CHILDOF);
				} else {
					if (empty.get("nid", fileid).getSingle() != null) {
						Node emp = empty.get("nid", fileid).getSingle();
						target.createRelationshipTo(emp, RelTypes.CHILDOF);
					} else {
						Node emp = graphDB.createNode();
						emp.setProperty("nid", fileid);
						empty.add(emp, "nid", fileid);
						target.createRelationshipTo(emp, RelTypes.CHILDOF);
					}
				}
			} else {// 没fileid,可能是内置类

			}
			tx.success();
		} finally {
			tx.close();
		}

	}

	public static void addInterfaceNode(String iname, String id, String fileid) {
		Transaction tx = graphDB.beginTx();
		try {
			// 首先在数据库中寻找这个类的id是否已经被其他实体引用过了，如果有直接拿之前的node
			// 如果没有，创建一个新节点
			Node target = empty.get("nid", id).getSingle();
			if (target != null) {
				target.setProperty(PRIMARY_KEY, iname);
				empty.remove(target);
				interfaces.add(target, "nid", target.getProperty("nid"));
				if (target.hasRelationship()) {
					for (Relationship r : target.getRelationships()) {
						if (r.isType(RelTypes.TMPEXT)) {
							Node parent = r.getEndNode();
							parent.createRelationshipTo(target,
									RelTypes.IMPLEMENTS);
							r.delete();
						}
					}
				}

			} else {
				target = graphDB.createNode();
				target.setProperty(PRIMARY_KEY, iname);
				target.setProperty("nid", id);
				interfaces.add(target, "nid", id);
			}

			if (fileid != null) {
				Node filenode = files.get("nid", fileid).getSingle();
				if (filenode != null) {
					target.createRelationshipTo(filenode, RelTypes.CHILDOF);
				} else {
					// 应该不会,先写一个放着
					System.out.println("dbg:no such file:" + fileid);
					Node newfile = graphDB.createNode();
					newfile.setProperty("nid", fileid);
					target.createRelationshipTo(newfile, RelTypes.CHILDOF);
					files.add(newfile, "nid", fileid);
				}
			} else {// 可能来自jar包或者java内置
			}

			target.setProperty("type", "interface");
			tx.success();
		} finally {
			tx.close();
		}

	}

	public static void addToRemoveList(String nid) {
		rmList.add(nid);
	}

	public static void clean() {
		Transaction tx = graphDB.beginTx();
		try {
			for (String id : rmList) {
				if (empty.get("nid", id).getSingle() != null) {
					Node del = empty.get("nid", id).getSingle();
					for (Relationship r : del.getRelationships()) {
						if (r.isType(RelTypes.TMPEXT)) {
							r.delete();
						}
					}
					del.delete();
				}
			}
			Node jbi = files.get("nid", "JavaBuiltIn").getSingle();
			Node jbiclasses = graphDB.createNode();
			for (Relationship r : jbi.getRelationships()) {
				Node other = r.getOtherNode(jbi);
				int count = 0;
				for (Relationship rt : other.getRelationships()) {
					count++;
				}
				if (count <= 1) {//
					// jbi.setProperty((String) other.getProperty("nid"),
					// "class");
					jbiclasses.setProperty((String) other.getProperty("nid"),
							"class");
					for (Relationship rt : other.getRelationships()) {
						rt.delete();
					}
					other.delete();
				}
			}
			jbiclasses.createRelationshipTo(jbi, RelTypes.CHILDOF);
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

	private static void registerShutdownHook(final GraphDatabaseService graphDB2) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDB.shutdown();
			}
		});
	}

	private void shutdown() {
		graphDB.shutdown();
	}
}
