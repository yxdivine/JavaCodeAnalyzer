package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullKytheEntry {
	static Map id_typeMap = new HashMap();

	enum type {// TODO 这个应该怎么弄呢？
		INTERFACE
	}

	static Pattern idPattern = Pattern.compile("#([0-9a-f]{64})");

	public FullKytheEntry(Map<String, Vector<String>> entry) {
		if (entry.containsKey("node/kind")) {
			String nk = entry.get("node/kind").firstElement();
			if (nk.equals("interface")) {// deal with interface
				// System.out.println(entry.get("entryid").firstElement());
				try {
					FileHelper.interfaceWriter.append(entry.get("entryid")
							.firstElement()
							+ "\t"
							+ entry.get("edge/named").firstElement()
									.replace("kythe:?lang=java#", "")
							+ "\t"
							+ entry.get("identifier").firstElement() + "\n");

					DBHelper.addInterfaceNode(entry.get("edge/named")
							.firstElement().replace("kythe:?lang=java#", ""),
							entry.get("entryid").firstElement());
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (nk.equals("package")) {// deal with package
				try {
					FileHelper.packageWriter.append(entry.get("entryid")
							.firstElement()
							+ "\t"
							+ entry.get("edge/named").firstElement()
									.replace("kythe:?lang=java#", "") + "\n");

					DBHelper.addPackageNode(entry.get("edge/named")
							.firstElement().replace("kythe:?lang=java#", ""),
							entry.get("entryid").firstElement());

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (nk.equals("record")) {
				if (entry.get("subkind").firstElement().equals("class")
						&& !entry.get("identifier").firstElement().equals("")) {
					try {
						FileHelper.classWriter.append(entry.get("entryid")
								.firstElement()
								+ "\t"
								+ entry.get("edge/named").firstElement()
										.replace("kythe:?lang=java#", "")
								+ "\t"
								+ entry.get("identifier").firstElement()
								+ "\n");

						DBHelper.addClassNode(entry.get("edge/named")
								.firstElement()
								.replace("kythe:?lang=java#", ""),
								entry.get("entryid").firstElement());

						if (entry.get("edge/extends") != null) {
							for (String exid : entry.get("edge/extends")) {
								Matcher m = idPattern.matcher(exid);
								if (m.find()) {
									String nid = m.group(1);
									String selfid = entry.get("entryid")
											.firstElement();
									// DBHelper.findExtendsNode(nid);
									if (!DBHelper.nodeExists(nid)) {
										DBHelper.addNode(nid);// 暂存
									}
								}
							}
							// System.exit(0);

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (nk.equals("anchor")) {
				if (entry.get("edge/ref") != null) {
					Matcher m = idPattern.matcher(entry.get("edge/ref")
							.firstElement());
					if (m.find()) {
						String refid = m.group(1);
						// System.out.println(entry.get("entryid").firstElement()
						// + "\t" + refid);
						// System.exit(0);
					}
				} else if (entry.get("edge/ref/call") != null) {

				}

			} else if (nk.equals("function")) {
				if (entry.get("edge/childof") != null) {
					Matcher m = idPattern.matcher(entry.get("edge/childof")
							.firstElement());
					if (m.find()) {
						String id = m.group(1);
						try {
							FileHelper.functionWriter.append(entry.get(
									"entryid").firstElement()
									+ "\t"
									+ entry.get("edge/named").firstElement()
											.replace("kythe:?lang=java#", "")
									+ "\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				// System.out.println(nk);
			}

		} else {
			try {
				FileHelper.logWriter
						.append(entry.get("entryid").firstElement());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
