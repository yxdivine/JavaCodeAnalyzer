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
		
		FileHelper.parseDataByBlock();
		FileHelper.closeFiles();
		
	}
	
	private static void init(){
		DBHelper.startDB();
		FileHelper.prepareFileHandles();
	}
	
}

