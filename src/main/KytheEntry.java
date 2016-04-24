package main;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KytheEntry {
	public String entry;

	public String file;// kythe entry key
	public String type;// kythe entry value type
	public String data;// kythe entry data

	static String kep = "^\"(.*?)\" \"/kythe/(.*?)\" \"(.*?)\" .$";
	static Pattern kytheEntryPattern = Pattern.compile(kep);

	// kythe://DataflowJavaSDK?path=examples/src/main/java/com/google/cloud/dataflow/examples/DebuggingWordCount.java
	static Pattern filePattern1 = Pattern.compile("kythe://" + "(.*?)\\?"
			+ "path=(.*/)([^#]*)$");
	// "kythe://DataflowJavaSDK?lang=java?path=sdk/src/main/java/com/google/cloud/dataflow/sdk/util/PackageUtil.java#00004e23b5dd3e008c576100e181e87eca7dd08a2c5ef0cc2fcf7987ff22a44d"
	// "/kythe/loc/end" "730" .
	static Pattern filePattern2 = Pattern.compile("kythe://" + "(.*?)\\?"
			+ "path=(.*/)([^#]*)#(.*?)$");
	// "kythe://jdk?lang=java#00420e380a18d85b5096f6ed31f27e04c02b59eda5785d7db6f03591d88364de"
	// "/kythe/identifier" "exit" .
	static Pattern classPattern = Pattern.compile("kythe:?lang=java#(.*?)");

	static Pattern unknownPattern = Pattern.compile("kythe:\\?lang=java#(.*?)");
	public String path;
	public String filename;

	public KytheEntry(String e) {
		entry = replaceHTMLCode(e);
		Matcher matcher = kytheEntryPattern.matcher(entry);
		if (matcher.find()) {
			this.file = matcher.group(1);
			this.type = matcher.group(2);
			this.data = matcher.group(3);
			try {
				judgeType();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// if(Pattern.matches(filePattern1.toString(), file)){
			// Matcher fm = filePattern1.matcher(file);
			// fm.find();
			// path = fm.group(2);
			// filename = fm.group(3);
			// }else if(Pattern.matches(filePattern2.toString(), file)){
			// Matcher m1 = filePattern2.matcher(file);
			//
			// }else if(Pattern.matches(unknownPattern.toString(), file)){
			// Matcher m1 = unknownPattern.matcher(file);
			//
			// }else{
			// System.out.println(entry);
			// System.exit(0);
			// }
		} else {
			System.out.println("err:constructor failed(KytheEntry)" + entry);
		}

	}

	public void judgeType() throws IOException {
		switch (type) {
		case "node/kind": {
			if (data.equals("name")) {// ?好像是函数的参数列表
				// A name identifies zero or more nodes.
				FileHelper.logWriter.append(file + "\n");
			} else if (data.equals("file")) {//文件路径
//				System.out.println(file);
			} else if (data.equals("anchor")) {
				//An anchor connects concrete syntax to abstract syntax.
			} else if (data.equals("tapp")) {
			} else if (data.equals("callable")) {
			} else if (data.equals("function")) {
				TmpStorage.functions.add(file);
			} else if (data.equals("variable")) {
			} else if (data.equals("abs")) {
			} else if (data.equals("absvar")) {
			} else if (data.equals("record")) {
			} else if (data.equals("interface")) {
				TmpStorage.interfaces.add(file);
			} else if (data.equals("package")) {
			} else if (data.equals("constant")) {
			} else if (data.equals("sum")) {
			} else if (data.equals("tbuiltin")) {// Java基础类型
				// A tbuiltin is a type that is supplied by the language itself.
			} else {
				System.out.println(entry);
				System.exit(0);
			}
			break;
		}
		case "text": {
			// 文件正文,应该没用
			break;
		}
		case "identifier": {
			// ???
			break;
		}
		case "subkind": {
			// ???
			break;
		}
		case "text/encoding": {
			// 文件编码(应该没用)
			break;
		}
		case "edge/annotatedby": {
			// A annotatedby B if A provides metadata for B.
			break;
		}
		case "edge/bounded/upper": {
			// ???
			break;
		}
		case "edge/bounded/lower": {
			// ???
			break;
		}
		case "edge/callableas": {
			// A callableas B if A participates in the call graph as B.
			break;
		}
		case "edge/childof": {
			// A childof B if A is contained in or dominated by B.
			break;
		}
		case "edge/defines": {
			// A defines B if A generates the semantic object B.
			break;
		}
		case "edge/defines/binding": {
			// A defines/binding B when A covers an identifier
			// bound to B when that binding is established.
			break;
		}
		case "edge/documents": {
			// A documents B if A describes (in possibly
			// marked up natural language) the semantic object B.
			break;
		}
		case "edge/extends": {// /////看起来很重要！
			// A extends B if A is a direct nominal subtype of B.
			break;
		}
		case "edge/named": {// 重要
			// A named B if B identifies A
			break;
		}
		case "edge/overrides": {// /////看起来很重要！
			// A overrides B if A directly overrides B in an inheritance-based
			// relationship.
			break;
		}
		case "edge/overrides/transitive": {
			// A overrides/transitive B if A transitively overrides B,
			// but the relationship A [overrides] B doesn’t exist.
			break;
		}
		case "edge/param": {// 关系
			// A param.N B if B is the Nth parameter of A.
			break;
		}
		case "edge/ref": {
			// A ref B if A refers to some previously-defined B.
			break;
		}
		case "edge/ref/call": {
			// A ref/call C if A is an anchor that calls C.
			break;
		}
		case "edge/ref/doc": {
			// A ref/doc C if A is an anchor inside a block of
			// documentation that refers to C.
			break;
		}
		case "edge/typed": {
			// A is typed B if A has the type B.
			break;
		}
		case "loc/end": {
			// ???
			break;
		}
		case "loc/start": {
			// ???
			break;
		}
		case "snippet/start": {
			// ???
			break;
		}
		case "snippet/end": {
			// ???
			break;
		}
		default: {
			System.out.println(entry);
			System.exit(0);
		}

		}

	}

	@Override
	public String toString() {
		return this.entry;
	}

	public static String replaceHTMLCode(String str) {
		String tmp = str;
		tmp = tmp.replaceAll("%20", " ");
		tmp = tmp.replaceAll("%21", "!");
		tmp = tmp.replaceAll("%23", "#");
//		tmp = tmp.replaceAll("%24", "$");
		tmp = tmp.replaceAll("%28", "(");
		tmp = tmp.replaceAll("%29", ")");
		tmp = tmp.replaceAll("%2C", ",");
		tmp = tmp.replaceAll("%3C", "<");
		tmp = tmp.replaceAll("%3E", ">");
		tmp = tmp.replaceAll("%3F", "?");
		tmp = tmp.replaceAll("%5B", "[");
		tmp = tmp.replaceAll("%5D", "]");
		tmp = tmp.replaceAll("%7B", "{");
		tmp = tmp.replaceAll("%7D", "}");
		return tmp;
	}
}