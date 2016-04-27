package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataParser {
	public static void main(String[] args){
		//First reads data from the file extracted from kythe
		
		init();
		
		parseDataFromFile(FileHelper.datafile.getPath());
		FileHelper.closeFiles();
		
	}
	
	private static void init(){
		DBHelper.startDB();
		FileHelper.prepareFileHandles();
	}
	
    public static void parseDataFromFile(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
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
}

