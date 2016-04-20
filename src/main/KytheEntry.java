package main;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KytheEntry{
	public String file;//kythe entry key
	public String type;//kythe entry value type
	public String data;//kythe entry data
	
	static String kep = "^\"(.*?)\" \"/kythe/(.*?)\" \"(.*?)\" .$";
    static Pattern kytheEntryPattern = Pattern.compile(kep);
    
    //kythe://DataflowJavaSDK?path=examples/src/main/java/com/google/cloud/dataflow/examples/DebuggingWordCount.java
    static Pattern filePattern1 = Pattern.compile("kythe://"+"(.*?)\\?"+"path=(.*/)([^#]*)$");
	//"kythe://DataflowJavaSDK?lang=java?path=sdk/src/main/java/com/google/cloud/dataflow/sdk/util/PackageUtil.java#00004e23b5dd3e008c576100e181e87eca7dd08a2c5ef0cc2fcf7987ff22a44d" "/kythe/loc/end" "730" .
    static Pattern filePattern2 = Pattern.compile("kythe://"+"(.*?)\\?"+"path=(.*/)([^#]*)#(.*?)$");
    //"kythe://jdk?lang=java#00420e380a18d85b5096f6ed31f27e04c02b59eda5785d7db6f03591d88364de" "/kythe/identifier" "exit" .
    
    
    static Pattern unknownPattern = Pattern.compile("kythe:\\?lang=java#(.*?)");
	public String path;
	public String filename;
	
	public String key;
	public Map<String,String> valuemap;
	public KytheEntry(String entry){
		entry = replaceHTMLCode(entry);
        Matcher matcher = kytheEntryPattern.matcher(entry);
        if(matcher.find()){
    		this.file = matcher.group(1);
    		this.type = matcher.group(2);
    		this.data = matcher.group(3);
    		if(Pattern.matches(filePattern1.toString(), file)){
    			Matcher fm = filePattern1.matcher(file);
    			fm.find();
    			path = fm.group(2);
    			filename = fm.group(3);
    		}else if(Pattern.matches(filePattern2.toString(), file)){
    			Matcher m1 = filePattern2.matcher(file);
    			
    		}else if(Pattern.matches(unknownPattern.toString(), file)){
    			Matcher m1 = unknownPattern.matcher(file);
    			
    		}else{
    			System.out.println(entry);
    			System.exit(0);
    		}
    		
    		
    		
        }else{
        	System.out.println("err:constructor failed(KytheEntry)"+entry);
        }
        
        
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	public String toString() {
		return this.file+"\n"+this.type+"\n"+this.data;
	}
	
	
	
	public static String replaceHTMLCode(String str){
		String tmp = str;
		tmp = tmp.replaceAll("%23", "#");
		tmp = tmp.replaceAll("%28", "(");
		tmp = tmp.replaceAll("%29", ")");
		tmp = tmp.replaceAll("%2C", ",");
		tmp = tmp.replaceAll("%3C", "<");
		tmp = tmp.replaceAll("%3E", ">");
		tmp = tmp.replaceAll("%5B", "[");
		tmp = tmp.replaceAll("%5D", "]");
		tmp = tmp.replaceAll("%7B", "{");
		tmp = tmp.replaceAll("%7D", "}");
		return tmp;
	}
}