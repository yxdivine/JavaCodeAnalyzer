package main;

public class DataParser {
	public static void main(String[] args){
		//First reads data from the file extracted from kythe
		
		init();
		
		FileHelper.parseDataByBlock();
		FileHelper.closeFiles();
		
		DBHelper.clean();
	}
	
	private static void init(){
		DBHelper.startDB();
		FileHelper.prepareFileHandles();
	}
	
}

