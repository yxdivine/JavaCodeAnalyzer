package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {
	// 日志文件
	// static File logfile = new
	// File("tmp/log"+System.currentTimeMillis()+".txt");
	static File logfile = new File("tmp/log.txt");
	public static FileWriter logWriter;
	// 数据包
	static File datafile = new File("data/kythe.nq");

	// 初始化
	public static void prepareFileHandles() {
		try {
			logWriter = new FileWriter(logfile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 解析数据
	public static void parseData() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			int line = 1;
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

	// 改进版数据解析
	public static void parseDataByBlock() {
		// 因为原kythe数据是key-value型,将同一个Node的数据全部从文件读取再一起处理比原来的一条一条处理要好一些
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("entryid", "start");// 一切的原点，并没有什么用
			while ((curStr = reader.readLine()) != null) {// 从源文件一次读取一行
				Matcher m = kytheEntryPattern.matcher(curStr);
				if (m.find()) {
					if (entry.get("entryid").equals(m.group(1))) {
						// 新的一行和之前一行属于同一节点，merge
						entry.put(m.group(2), m.group(3));
					} else {
						// TODO entry 数据读取完毕，处理后扔进graph db
						// System.out.println(entry.get("entryid")+"\t"+entry.size());

						// 切换entry
						entry = new HashMap<String, String>();
						entry.put("entryid", m.group(1));
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

	// 关闭文件
	public static void closeFiles() {
		try {
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
