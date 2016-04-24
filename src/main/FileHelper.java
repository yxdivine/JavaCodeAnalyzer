package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
	//��־�ļ�
//	static File logfile = new File("tmp/log"+System.currentTimeMillis()+".txt");
	static File logfile = new File("tmp/log.txt");
	public static FileWriter logWriter;
	//���ݰ�
	static File datafile = new File("data/kythe.nq");
	
	//��ʼ��
	public static void prepareFileHandles(){
		try{
			logWriter = new FileWriter(logfile);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//��������
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

	//�ر��ļ�
	public static void closeFiles() {
		try {
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
