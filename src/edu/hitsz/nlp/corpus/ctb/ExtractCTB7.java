package edu.hitsz.nlp.corpus.ctb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.java.math.NumbUtils;
import edu.hitsz.nlp.struct.CfgTree;

/**
 * 从ctb7中抽取出
 * @author Xinxin Li
 * @since Dec 24, 2012
 */
public class ExtractCTB7 {
	
	/**
	 * 抽取CTB5的原始语料
	 * @since Jan 6, 2013
	 * @param dir
	 * @param outdir
	 */
	public static void extractCTB5Raw(String dir, String outdir) {
		
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		
		try {
			
			FileWriter devWriter = new FileWriter(new File(outdir+"/ctb5-dev-raw"));
			FileWriter testWriter = new FileWriter(new File(outdir+"/ctb5-test-raw"));
			HashMap<Integer, Integer> devList = getList("301-325");
			HashMap<Integer, Integer> testList = getList("271-300");			
			
			for(String fileName : fileNames) {
				int sequence = NumbUtils.elimPrefixZeroInt(fileName.substring(fileName.length()-11, fileName.length()-7));
				if(devList.containsKey(sequence)) {
					String newLine = extractRaw(fileName);
					devWriter.write(newLine);
				}
				if(testList.containsKey(sequence)) {
					String newLine = extractRaw(fileName);
					testWriter.write(newLine);
				}
			}
			
			devWriter.close();
			testWriter.close();
		}
		catch(IOException e) {
			
		}
		
	}
	
	/**
	 * 抽取原始语料
	 * @since Jan 6, 2013
	 * @param fileName
	 * @return
	 */
	public static String extractRaw(String fileName) {
		
		StringBuffer sbuf = new StringBuffer();
		
		try {
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.startsWith("<") && !line.endsWith(">")) {
					sbuf.append(line+"\n");
				}				
			}
			reader.close();				
		}
		catch (IOException e) {
			
		}
		return sbuf.toString();
		
	}
	
	
	
	
	
	/** 提取词和词性
	 * 
	 * @since May 31, 2012
	 * @param dir 语料库包含词和词性的文件夹 postagged
	 * @param outdir 输出词和词性的文件夹, 词和词性按照CoNLL格式
	 */
	public static void extractCTB5SegPos(String dir, String outdir) {
		
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		
		try {
			//
			FileWriter smallTrainWriter = new FileWriter(new File(outdir+"/ctb5-small-train-wp"));
			FileWriter largeTrainWriter = new FileWriter(new File(outdir+"/ctb5-large-train-wp"));
			FileWriter devWriter = new FileWriter(new File(outdir+"/ctb5-dev-wp"));
			FileWriter testWriter = new FileWriter(new File(outdir+"/ctb5-test-wp"));
			HashMap<Integer, Integer> smallTrainList = getList("1-270");
			HashMap<Integer, Integer> largeTrainList = getList("1-270,400-931,1001-1151");
			HashMap<Integer, Integer> devList = getList("301-325");
			HashMap<Integer, Integer> testList = getList("271-300");			
			
			for(String fileName : fileNames) {
				int sequence = NumbUtils.elimPrefixZeroInt(fileName.substring(fileName.length()-11, fileName.length()-7));
				if(smallTrainList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					smallTrainWriter.write(wordPosSequence);
				}		
				if(largeTrainList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					largeTrainWriter.write(wordPosSequence);
				}
				if(devList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					devWriter.write(wordPosSequence);
				}
				if(testList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					testWriter.write(wordPosSequence);
				}
			}
			smallTrainWriter.close();
			largeTrainWriter.close();
			devWriter.close();
			testWriter.close();
			//			
		}
		catch(IOException e) {
			
		}		
		
	}
	
	/**
	 * Wang 2011
	 * @since Dec 25, 2012
	 * @param dir
	 * @param outdir
	 */
	public static void extractCTB7SegPos(String dir, String outdir) {
		
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		
		try {
			//
			FileWriter trainWriter = new FileWriter(new File(outdir+"/ctb7-train-wp"));
			FileWriter devWriter = new FileWriter(new File(outdir+"/ctb7-dev-wp"));
			FileWriter testWriter = new FileWriter(new File(outdir+"/ctb7-test-wp"));
			HashMap<Integer, Integer> devList = getList("41-80,203-233,301-325,400-409,591,613-617,643-673,1022-1035,1120-1129,2110-2159,2270-2294,"
					+ "2510-2569,2760-2799,3040-3109,4040-4059,4084-4085,4090,4096,4106-4108,4113-4115,"
					+ "4121,4128,4132,4135,4158-4162,4169,4189,4196,4236-4261,4322,4335-4336,4407-4411");
			HashMap<Integer, Integer> testList = getList("1-40,144-174,271-300,410-428,592,900-931,1009-1020,1036,1044,1060-1061,1072,1118-1119,1132,1141-1142,"
					+ "1148,2000-2010,2160-2220,2295-2330,2570-2640,2800-2845,3110-3145,4030-4039,4060-4070,"
					+ "4086-4087,4091,4097,4109-4112,4118-4120,4127,4133-4134,4136-4139,4163-4168,4188,4197-4235,"
					+ "4321,4334,4337,4400-4406");
			
			for(String fileName : fileNames) {
				int sequence = NumbUtils.elimPrefixZeroInt(fileName.substring(fileName.length()-11, fileName.length()-7));
				
				if(devList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					devWriter.write(wordPosSequence);
				}
				else if(testList.containsKey(sequence)) {
					String wordPosSequence = extractSegPos(fileName);
					testWriter.write(wordPosSequence);
				}
				else {
					String wordPosSequence = extractSegPos(fileName);
					trainWriter.write(wordPosSequence);
				}
			}
			trainWriter.close();
			devWriter.close();
			testWriter.close();
		}
		catch(IOException e) {
			
		}		
		
	}
	
	/** 提取一个文件中的词和词性到一个字符串中
	 * <p>
	 * <S ID=2>
	 * 新华社_NN 上海_NR 二月_NT 十日_NT 电_NN （_PU 记者_NN 谢金虎_NR 、_PU 张持坚_NR ）_PU 
	 * </S>
	 * @since Dec 25, 2012
	 * @param fileName
	 * @return
	 */
	public static String extractSegPos(String fileName) {
	
		try {

			StringBuffer bf = new StringBuffer();
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length()> 0 && !line.startsWith("<") && !line.endsWith(">")) {
					String[] parts = line.split("\\s+");
					int number = parts.length;					
					for(int i=0; i<number; i++) {
						String[] part = parts[i].split("_");
						if(part.length%2 != 0) {
							System.out.println(fileName);
							System.out.println(line);
							System.out.println("input file Wrong");
							//System.exit(-1);
							break;
						}
						String pos = part[1].split("-")[0];
						bf.append(part[0]+"\t"+pos+"\n");
					}
					bf.append("\n");
				}				
			}
			reader.close();
			
			return bf.toString();
			
		}
		catch(IOException e) {
			
		}
		
		return "";	
		
	}
	
	
	

	
	

	public static void extractCTB2Parse(String dir, String outdir) {
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		try {
			//			
			//CTB2,small
			
			FileWriter trainWriter = new FileWriter(new File(outdir+"/ctb2-train-parse"));
			FileWriter trainWordPosWriter = new FileWriter(new File(outdir+"/ctb2-train-parse-wp"));
			FileWriter trainWordWriter = new FileWriter(new File(outdir+"/ctb2-train-parse-w"));

			FileWriter devWriter = new FileWriter(new File(outdir+"/ctb2-dev-parse"));
			FileWriter devWordPosWriter = new FileWriter(new File(outdir+"/ctb2-dev-parse-wp"));	
			FileWriter devWordWriter = new FileWriter(new File(outdir+"/ctb2-dev-parse-w"));	

			FileWriter testWriter = new FileWriter(new File(outdir+"/ctb2-test-parse"));
			FileWriter testWordPosWriter = new FileWriter(new File(outdir+"/ctb2-test-parse-wp"));
			FileWriter testWordWriter = new FileWriter(new File(outdir+"/ctb2-test-parse-w"));
			
			HashMap<Integer, Integer> trainList = getList("1-270");
			HashMap<Integer, Integer> devList = getList("301-325");
			HashMap<Integer, Integer> testList = getList("271-300");
			
			for(String fileName : fileNames) {
				int sequence = NumbUtils.elimPrefixZeroInt(fileName.substring(fileName.length()-7, fileName.length()-3));
				if(trainList.containsKey(sequence)){
					extractParse(fileName, trainWriter, trainWordPosWriter, trainWordWriter);
				}
				if(devList.containsKey(sequence)){
					extractParse(fileName, devWriter, devWordPosWriter, devWordWriter);
				}	
				if(testList.containsKey(sequence)){
					extractParse(fileName, testWriter, testWordPosWriter, testWordWriter);
				}
			}
			trainWriter.close();
			trainWordPosWriter.close();
			trainWordWriter.close();
			
			devWriter.close();
			devWordPosWriter.close();
			devWordWriter.close();
			
			testWriter.close();
			testWordPosWriter.close();
			testWordWriter.close();
	
		}
		catch(IOException e) {
			
		}
	}
			
	
	/**
	 * Duan 2007
	 * @since Dec 25, 2012
	 * @param dir
	 * @param outdir
	 */
	public static void extractCTB5Parse(String dir, String outdir) {
		
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		try {
			
			FileWriter trainWriter = new FileWriter(new File(outdir+"/ctb5-train-parse"));
			FileWriter trainWordPosWriter = new FileWriter(new File(outdir+"/ctb5-train-parse-wp"));
			FileWriter trainWordWriter = new FileWriter(new File(outdir+"/ctb5-train-parse-w"));

			FileWriter devWriter = new FileWriter(new File(outdir+"/ctb5-dev-parse"));
			FileWriter devWordPosWriter = new FileWriter(new File(outdir+"/ctb5-dev-parse-wp"));	
			FileWriter devWordWriter = new FileWriter(new File(outdir+"/ctb5-dev-parse-w"));	

			FileWriter testWriter = new FileWriter(new File(outdir+"/ctb5-test-parse"));
			FileWriter testWordPosWriter = new FileWriter(new File(outdir+"/ctb5-test-parse-wp"));
			FileWriter testWordWriter = new FileWriter(new File(outdir+"/ctb5-test-parse-w"));
			
			HashMap<Integer, Integer> trainList = getList("1-815,1001-1136");
			HashMap<Integer, Integer> devList = getList("886-931,1148-1151");
			HashMap<Integer, Integer> testList = getList("816-885,1137-1147");
			
			for(String fileName : fileNames) {
				int sequence = NumbUtils.elimPrefixZeroInt(fileName.substring(fileName.length()-7, fileName.length()-3));
				if(trainList.containsKey(sequence)){
					extractParse(fileName, trainWriter, trainWordPosWriter, trainWordWriter);
				}
				if(devList.containsKey(sequence)){
					extractParse(fileName, devWriter, devWordPosWriter, devWordWriter);
				}	
				if(testList.containsKey(sequence)){
					extractParse(fileName, testWriter, testWordPosWriter, testWordWriter);
				}
			}
			trainWriter.close();
			trainWordPosWriter.close();
			trainWordWriter.close();
			
			devWriter.close();
			devWordPosWriter.close();
			devWordWriter.close();
			
			testWriter.close();
			testWordPosWriter.close();
			testWordWriter.close();
	
		}
		catch(IOException e) {
			
		}
	}
	
	
	
	/** 从文件中提取句法树，以Zhang and Clark 2008为规则修建句法树*/
	public static void extractParse(String fileName, FileWriter writer, FileWriter wordPosWriter, FileWriter wordWriter) {
		try {
			StringBuffer allBf = new StringBuffer();
			StringBuffer bf = new StringBuffer();
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			String line = null;
			int number=0;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.startsWith("<") && !line.endsWith(">")) {
					bf.append(line+" ");
				}				
				else {
					String s = bf.toString().trim();
					if(s.length() > 0) {
						//System.out.print(number+++".");
						CfgTree tree = new CfgTree();
						tree.load(s, 0, s.length(), false);
						//tree.output();
						tree.cleanEmptyTerminal();
						//tree.output();
						tree.cleanNonTerminals();
						//tree.output();
						tree.collapseUnaryRule();
						//tree.output();	
						tree.collapseUnaryDiffRule();
						tree.store(writer);
						tree.storeWPColumn(wordPosWriter);
						tree.storeWordRow(wordWriter, true);
					}
					bf = new StringBuffer();
				}
			}
			reader.close();
			
			
		}
		catch(IOException e) {
			
		}		
	}
	
	
	
	public static void constituent2Dependency(String constituentFileName, String dependencyFileName) {
		
		try {
			
			String encoding = FileEncoding.getCharset(constituentFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(constituentFileName), encoding));
			String line = null;
			
			StringBuffer sbuf = new StringBuffer();
			while( (line = reader.readLine()) != null) {
				
				if(line.length() == 0) {
					String sentence = sbuf.toString().trim();
					int sentenceLength = sentence.length();
					if(sentenceLength == 0)
						continue;
					else {
					}
				}
			}
			
			
		}
		catch (IOException e) {
			
		}
			
		
	}
	
	

	
	
	/** 去掉 <> 的行 */
	public static void clear(String dir, String outdir) {
		FileTree tree = new FileTree();
		tree.generateFrom(dir);
		ArrayList<String> fileNames = tree.getFileNames();
		
		for(String fileName : fileNames) {			
			try {
				//
				String encoding = FileEncoding.getCharset(fileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
				FileWriter writer = new FileWriter(new File(outdir+File.separator+new File(fileName).getName()));
				
				String line = null;
				while((line = reader.readLine()) != null) {
					if(line.trim().length()==0 || (line.startsWith("<") && line.endsWith(">"))) {
						continue;
					}	
					writer.write(line + "\n");
				}
				reader.close();
				writer.close();		
			}
			catch (IOException e) {
				
			}
			
		}
	}
	
	
	/**
	 * 解析1,4-6,9-10,12这种类型的句子，将里面的所有数字都列举出来，并存入map
	 * @since Dec 25, 2012
	 * @param line
	 * @return
	 */
	public static HashMap<Integer, Integer> getList(String line) {
		
		if(line == null)
			return null;
		
		HashMap<Integer, Integer> fileNumberMap= new HashMap<Integer, Integer>();
		
		String[] subs = line.trim().split(",");
		for(String sub : subs) {
			sub = sub.trim();
			if(sub.contains("-")) {
				String[] splits = sub.split("-");
				int start = Integer.parseInt(splits[0]);
				int end = Integer.parseInt(splits[1]);
				for(int i=start; i<=end; i++)
					fileNumberMap.put(i, 1);				
			}
			else
				fileNumberMap.put(Integer.parseInt(sub), 1);
						
		}
		return fileNumberMap;
		
	}
	

	/**
	 * 检测数组中是否有相同的元素
	 * @since Jan 9, 2012
	 * @param vector
	 */
	public static void checkEqual(String[] vector){
		int len = vector.length;
		for(int i=0; i<len; i++)
			for(int j=0; j<len; j++){
				if (i < j){
					if(vector[i].equals(vector[j])){
						System.out.println(i+":"+vector[i]+"-"+j+":"+vector[j]);
					}
				}
			}
	}
	
	
	public static void main(String[] args) {
		
		String ctb7Dir = "/media/tm/study/corpora/treebank/CTB_Chinese_Treebank/ctb_7/";
		String rawDir = ctb7Dir + "data/utf-8/raw";  // "/chtb_0515.nw.raw";
		String segmentedDir = ctb7Dir + "raw/data/utf-8/segmented"; // "/chtb_0515.nw.seg";
		String postaggedDir = ctb7Dir + "raw/data/utf-8/postagged"; // "/chtb_0515.nw.pos";
		String bracketedDir = ctb7Dir + "raw/data/utf-8/bracketed"; // "/chtb_0515.nw";
		
		String ctb2OutDir = ctb7Dir + "ctb2";
		String ctb5OutDir = ctb7Dir + "ctb5";
		String ctb7OutDir = ctb7Dir + "ctb7";
		
		//ExtractCTB7.extractCTB5SegPos(postaggedDir, ctb5OutDir);
		//ExtractCTB7.extractCTB7SegPos(postaggedDir, ctb7OutDir);
		
		//ExtractCTB7.extractCTB2Parse(bracketedDir, ctb2OutDir);
		ExtractCTB7.extractCTB5Parse(bracketedDir, ctb5OutDir);
		//ExtractCTB7.extractCTB5Raw(rawDir,ctb5OutDir);
		
				
	
	}
	
}
