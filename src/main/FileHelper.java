package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {
	// ��־�ļ�
	// static File logfile = new
	// File("tmp/log"+System.currentTimeMillis()+".txt");
	static File logfile = new File("tmp/log.txt");
	static File interfacelog = new File("tmp/interfaces.txt");
	static File classlog = new File("tmp/classes.txt");
	static File packagelog = new File("tmp/packages.txt");
	static File functionlog = new File("tmp/functions.txt");
	public static FileWriter logWriter;
	public static FileWriter interfaceWriter;
	public static FileWriter classWriter;
	public static FileWriter packageWriter;
	public static FileWriter functionWriter;
	// ���ݰ�
	static File datafile = new File("data/tomcatjdbc.nq");

	// ��ʼ��
	public static void prepareFileHandles() {
		try {
			logWriter = new FileWriter(logfile);
			interfaceWriter = new FileWriter(interfacelog);
			classWriter = new FileWriter(classlog);
			packageWriter = new FileWriter(packagelog);
			functionWriter = new FileWriter(functionlog);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��������
	@Deprecated
	public static void parseData() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			while ((curStr = reader.readLine()) != null) {
				KytheEntry kentry = new KytheEntry(curStr);
			}
			System.out.println("Reading File Finished");
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	static String kep = "^\"(.*?)\" \"/kythe/(.*?)\" \"(.*?)\" .$";
	static Pattern kytheEntryPattern = Pattern.compile(kep);

	static Pattern acPattern = Pattern.compile(".*?#([0-9a-f]{64})");

	// �Ľ������ݽ���
	public static void parseDataByBlock() {
		// ��Ϊԭkythe������key-value��,��ͬһ��Node������ȫ�����ļ���ȡ��һ�����ԭ����һ��һ������Ҫ��һЩ
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			Map<String, Vector<String>> entry = new HashMap<String, Vector<String>>();
			entry.put("entryid", new Vector<String>());
			entry.get("entryid").add("start");
			while ((curStr = reader.readLine()) != null) {// ��Դ�ļ�һ�ζ�ȡһ��
				curStr = replaceHTMLCode(curStr);
				Matcher m = kytheEntryPattern.matcher(curStr);
				if (m.find()) {
					String idstr = m.group(1);
					Matcher m1 = acPattern.matcher(idstr);
					if (m1.find()) {
						idstr = m1.group(1);
					}
					if (entry.get("entryid").firstElement().equals(idstr)) {
						// �µ�һ�к�֮ǰһ������ͬһ�ڵ㣬merge
						if (entry.get(m.group(2)) == null) {
							entry.put(m.group(2), new Vector<String>());
						}
						entry.get(m.group(2)).add(m.group(3));
					} else {
						// TODO entry ���ݶ�ȡ��ϣ�������ӽ�graph db
						// System.out.println(entry.get("entryid")+"\t"+entry.size());
						FullKytheEntry e = new FullKytheEntry(entry);
						
						// �л�entry
						entry = new HashMap<String, Vector<String>>();
						entry.put("entryid", new Vector<String>());
						entry.get("entryid").add(idstr);
						entry.put(m.group(2), new Vector<String>());
						entry.get(m.group(2)).add(m.group(3));
					}

				} else {
					// ���������ݺ�Ԥ�����ݸ�ʽ��һ����gg
					System.out
							.println("err! Input does not match kythe entry format.");
				}

			}
			System.out.println("Reading File Finished");
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

	}

	// �����ļ��е�html����
	public static final Pattern htmlCode = Pattern
			.compile("%([A-F2-7][A-F0-9])");

	public static String replaceHTMLCode(String str) {
		// ������ռ�õķ���
		String tmp = str.replaceAll("%24", Matcher.quoteReplacement("$"))
				.replaceAll("%5C", Matcher.quoteReplacement("\\"));
		Matcher m = htmlCode.matcher(tmp);
		while (m.find()) {
			String s = m.group(1);
			tmp = tmp
					.replaceAll("%" + s, ((char) Integer.parseInt(s, 16)) + "");
		}
		return tmp;
	}

	// �ر��ļ�
	public static void closeFiles() {
		try {
			logWriter.close();
			classWriter.close();
			packageWriter.close();
			interfaceWriter.close();
			functionWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
