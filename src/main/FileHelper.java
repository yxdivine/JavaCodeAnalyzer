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
	// 日志文件
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
	// 数据包
	static File datafile = new File("data/tomcatjdbc.nq");

	// 初始化
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

	// 解析数据
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

	// 改进版数据解析
	public static void parseDataByBlock() {
		// 因为原kythe数据是key-value型,将同一个Node的数据全部从文件读取再一起处理比原来的一条一条处理要好一些
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			Map<String, Vector<String>> entry = new HashMap<String, Vector<String>>();
			entry.put("entryid", new Vector<String>());
			entry.get("entryid").add("start");
			while ((curStr = reader.readLine()) != null) {// 从源文件一次读取一行
				curStr = replaceHTMLCode(curStr);
				Matcher m = kytheEntryPattern.matcher(curStr);
				if (m.find()) {
					String idstr = m.group(1);
					Matcher m1 = acPattern.matcher(idstr);
					if (m1.find()) {
						idstr = m1.group(1);
					}
					if (entry.get("entryid").firstElement().equals(idstr)) {
						// 新的一行和之前一行属于同一节点，merge
						if (entry.get(m.group(2)) == null) {
							entry.put(m.group(2), new Vector<String>());
						}
						entry.get(m.group(2)).add(m.group(3));
					} else {
						// TODO entry 数据读取完毕，处理后扔进graph db
						// System.out.println(entry.get("entryid")+"\t"+entry.size());
						FullKytheEntry e = new FullKytheEntry(entry);
						
						// 切换entry
						entry = new HashMap<String, Vector<String>>();
						entry.put("entryid", new Vector<String>());
						entry.get("entryid").add(idstr);
						entry.put(m.group(2), new Vector<String>());
						entry.get(m.group(2)).add(m.group(3));
					}

				} else {
					// 读到的数据和预计数据格式不一样，gg
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

	// 处理文件中的html编码
	public static final Pattern htmlCode = Pattern
			.compile("%([A-F2-7][A-F0-9])");

	public static String replaceHTMLCode(String str) {
		// 被正则占用的符号
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

	// 关闭文件
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
