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
	// ��־�ļ�
	// static File logfile = new
	// File("tmp/log"+System.currentTimeMillis()+".txt");
	static File logfile = new File("tmp/log.txt");
	public static FileWriter logWriter;
	// ���ݰ�
	static File datafile = new File("data/kythe.nq");

	// ��ʼ��
	public static void prepareFileHandles() {
		try {
			logWriter = new FileWriter(logfile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��������
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

	// �Ľ������ݽ���
	public static void parseDataByBlock() {
		// ��Ϊԭkythe������key-value��,��ͬһ��Node������ȫ�����ļ���ȡ��һ�����ԭ����һ��һ������Ҫ��һЩ
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(datafile));
			String curStr = null;
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("entryid", "start");// һ�е�ԭ�㣬��û��ʲô��
			while ((curStr = reader.readLine()) != null) {// ��Դ�ļ�һ�ζ�ȡһ��
				Matcher m = kytheEntryPattern.matcher(curStr);
				if (m.find()) {
					if (entry.get("entryid").equals(m.group(1))) {
						// �µ�һ�к�֮ǰһ������ͬһ�ڵ㣬merge
						entry.put(m.group(2), m.group(3));
					} else {
						// TODO entry ���ݶ�ȡ��ϣ�������ӽ�graph db
						// System.out.println(entry.get("entryid")+"\t"+entry.size());

						// �л�entry
						entry = new HashMap<String, String>();
						entry.put("entryid", m.group(1));
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

	// �ر��ļ�
	public static void closeFiles() {
		try {
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
