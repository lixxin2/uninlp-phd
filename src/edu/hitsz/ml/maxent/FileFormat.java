package edu.hitsz.ml.maxent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;

/**
 * 文件格式:maximum entropy model of Zhang Le
 * <p> 可以将maxent格式转换为arff(decision tree), svm格式
 * 
 * @author Xinxin Li
 * @since Apr 28, 2012
 */
public class FileFormat {
	
	private boolean isTrain;
	private HashMap<String, Integer> classes;
	private int classesNumber;
	private ArrayList<HashMap<String, Integer>> features;
	private int featuresNumber;
	
	public FileFormat() {
		this.isTrain = true;
		classes = new HashMap<String, Integer>();
		classesNumber = 0;
		featuresNumber = 0;
		features = new ArrayList<HashMap<String, Integer>>();
	}
	
	public FileFormat(boolean isTrain) {
		this();
		this.isTrain = isTrain;
	}
	
	/**
	 * 获取数据中的所有特征数目，保存每一个特征列的特征数目
	 * @since May 22, 2012
	 * @param meFile
	 * @return
	 */
	private int[] getClassAndFeatureNumberColumn(String meFile) {
		
		boolean firstLine = true;
		try{
			String encoding = FileEncoding.getCharset(meFile); 
		    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(meFile), encoding)); 
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("[ \t]");
				String oneClass = parts[0];
				if(!classes.containsKey(oneClass))
					classes.put(oneClass, classes.size()+1);
				if(firstLine) {				
					for(int i=1; i<parts.length; i++) {
						HashMap<String, Integer> feature = new HashMap<String, Integer>();
						feature.put(parts[i], 1);
						features.add(feature);
					}
					firstLine = false;					
				}
				else {
					for(int i=1; i<parts.length; i++) {		
						HashMap<String, Integer> feature = features.get(i-1);
						if(!feature.containsKey(parts[i])) {
							feature.put(parts[i], 1);
						}
					}
				}				
			}
			reader.close();	
		}
		catch (IOException e) {
			
		}
		//比features的数目多1，其中第一个为0
		int listNumber = features.size();

		int[] featureNumberList = new int[listNumber + 1];
		for(int i=1; i<featureNumberList.length; i++) {
			featureNumberList[i] += featureNumberList[i-1] + features.get(i-1).size();
		}
		
		classesNumber = classes.size();
		featuresNumber = featureNumberList[featureNumberList.length-1];
		features.clear();
		System.out.println("There are totally "+classesNumber + " classes, "
				+ "and " + featuresNumber + " features");		
				
		return featureNumberList;
	}
	 
	
	
	/**
	 * 获取文件中的类别和特征Map
	 * 是按照最大熵文件有相同的列，并且每一列里面都是同一个特征。
	 * 特征排序也是按照列来排的
	 * @since Apr 27, 2012
	 * @param meFile
	 */
	private void getClassAndFeaturesColumn(String meFile) {
		int[] featureNumberList = getClassAndFeatureNumberColumn(meFile);
		
		boolean firstLine = true;
		try{
			String encoding = FileEncoding.getCharset(meFile); 
		    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(meFile), encoding)); 
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("[ \t]");
				if(firstLine) {					
					for(int i=1; i<parts.length; i++) {
						HashMap<String, Integer> feature = new HashMap<String, Integer>();
						feature.put(parts[i], featureNumberList[i-1] + feature.size() + 1);
						features.add(feature);
					}
					firstLine = false;					
				}
				else {
					for(int i=1; i<parts.length; i++) {		
						HashMap<String, Integer> feature = features.get(i-1);
						if(!feature.containsKey(parts[i])) {
							feature.put(parts[i], featureNumberList[i-1] + feature.size() + 1);
						}
					}
				}				
			}
			reader.close();	
		}
		catch (IOException e) {
			
		}

	}
	
	
	
	
	
	public void me2arff(String meFile, String arffFile) {
		
		getClassAndFeaturesColumn(meFile);
		
		try{
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(arffFile));
			writer.write("\n");
			writer.write("@relation "+meFile);
			writer.write("\n");
			
			for(int i=0; i<features.size(); i++) {
				HashMap<String, Integer> feature = features.get(i);
				if(feature.size() > 0) {
					Iterator<Entry<String, Integer>> iter = feature.entrySet().iterator();
					StringBuffer sb = new StringBuffer(2048);
					sb.append("@attribute f");
					sb.append(i);
					sb.append(" {");
					while(iter.hasNext()) {
						Entry<String, Integer> entry = iter.next();
						sb.append(entry.getKey().replace("[^a-zA-Z]", "."));
						sb.append(",");
					}
					String s = sb.substring(0, sb.length()-1);
					writer.write(s + "}\n");
				}
				else {
					System.out.println("there is a feature size less than 1");
					System.exit(-1);
				}
			}
			Iterator<Entry<String, Integer>> iter = classes.entrySet().iterator();
			StringBuffer sb = new StringBuffer();
			sb.append("@attribute class {");
			while(iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				sb.append(entry.getKey().replace(",", "."));
				sb.append(",");
			}
			writer.write(sb.substring(0, sb.length()-1) + "}\n");
			
			writer.write("\n@data\n\n");
			
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(meFile)));
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.trim().split(" ");
				sb = new StringBuffer();
				for(int i=1; i<parts.length; i++) {						
					sb.append(parts[i].replace(",", "."));
					sb.append(",");
				}				
				sb.append(parts[0]);
				writer.write(sb.toString()+"\n");
			}
			reader.close();	
			writer.close();
		}
		catch (IOException e) {
			
		}	
		
	}
	
	
	/**
	 * 将最大熵训练文件转换为svm格式
	 * @since May 25, 2012
	 * @param maxentFileName
	 * @param dictFileName
	 * @param svmFileName
	 */
	public void me2svmTrainColumn(String maxentFileName, String dictFileName, String svmFileName) {
		getClassAndFeaturesColumn(maxentFileName);
		storeDict(dictFileName);
		int size = 1 + features.size();
		try {
			String encoding = FileEncoding.getCharset(maxentFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(maxentFileName), encoding));
			FileWriter writer = new FileWriter(new File(svmFileName));
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("[ \t]");
				if(parts.length != size) {
					System.out.println("feature number is wrong");
					System.exit(-1);
				}
				StringBuffer sb = new StringBuffer();
				int classNumber = classes.get(parts[0]);
				sb.append(classNumber);
				for(int i=1; i<parts.length; i++) {
					if(features.get(i-1).containsKey(parts[i])) {
						sb.append(" ");
						sb.append(features.get(i-1).get(parts[i]));
						sb.append(":1");
					}
					else {
						System.out.println("there must be wrong");
						System.exit(-1);
					}
				}
				writer.write(sb.toString() + "\n");
				
			}
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {
			
		}
		
		
	}
	
	
	/**
	 * 将最大熵测试文件转换为svm格式
	 * @since May 25, 2012
	 * @param dictFileName
	 * @param maxentFileName
	 * @param svmFileName
	 */
	public void me2svmTestColumn(String dictFileName, String maxentFileName, String svmFileName) {
		
		readDict(dictFileName);
		int size = 1 + features.size();
		try {
			String encoding = FileEncoding.getCharset(maxentFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(maxentFileName), encoding));
			FileWriter writer = new FileWriter(new File(svmFileName));
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("[ \t]");
				if(parts.length != size) {
					System.out.println("feature number is wrong");
					System.exit(-1);
				}
				StringBuffer sb = new StringBuffer();
				int classNumber = 0;
				if(classes.containsKey(parts[0]))
					classNumber = classes.get(parts[0]);
				sb.append(classNumber);
				for(int i=1; i<parts.length; i++) {
					if(features.get(i-1).containsKey(parts[i])) {
						sb.append(" ");
						sb.append(features.get(i-1).get(parts[i]));
						sb.append(":1");
					}
					//else {
					//	sb.append(" ");
					//	sb.append(featuresNumber);
					//	sb.append(":1");
					//}
				}
				writer.write(sb.toString() + "\n");
				
			}
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {			
		}
	}
	
	/**
	 * 获取svm结果，比如1->B-PER, 2->I-PER
	 */
	public void getSvmResult(String dictFileName, String svmRawResultFileName, String svmResultFileName) {
		readDict(dictFileName);
		//
		HashMap<Integer, String> types = new HashMap<Integer, String>();
		Iterator<Map.Entry<String, Integer>> iter = classes.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			String type = entry.getKey();
			int number = entry.getValue();
			types.put(number, type);
		}
		//		
		try {
			String encoding = FileEncoding.getCharset(svmRawResultFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(svmRawResultFileName), encoding));
			FileWriter writer = new FileWriter(new File(svmResultFileName));
			String line = null;
			while((line = reader.readLine()) != null) {
				String svmResult = line.trim();
				if(svmResult.length() < 1) {
					System.out.println("wrong is svm ");
					System.exit(-1);
				}
				int svmInt = Integer.parseInt(svmResult);
				String finalResult = "O";
				if(types.containsKey(svmInt))
					finalResult = types.get(svmInt);
				writer.write(finalResult + "\n");
				
			}
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {		
			
		}
		
		
	}
	
	/**结合 */
	public void comb(String testFileName, String svmResultFileName, String svmFinalResultFileName) {
		
		try {
			String encoding1 = FileEncoding.getCharset(testFileName); 
			BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(testFileName), encoding1));
						
			String encoding2 = FileEncoding.getCharset(svmResultFileName); 
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(svmResultFileName), encoding2));
			
			FileWriter writer = new FileWriter(new File(svmFinalResultFileName));
			
			String line1 = null;
			String line2 = null;
			
			while((line1 = reader1.readLine()) != null) {
				String testLine = line1.trim();
				if(testLine.length() < 1) {
					writer.write("\n");
					continue;
				}
				
				line2 = reader2.readLine().trim();
				
				writer.write(line1 + "\t" + line2 + "\n");
								
			}
			reader1.close();
			reader2.close();
			writer.close();		
			
		}
		catch (IOException e) {	
			
		}
		
		
		
		
		
	}
		
	
	private void storeDict(String dictFileName) {
		try {
			System.out.println("Storing dict...");
			FileOutputStream fos = new FileOutputStream(dictFileName);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(classes);
		    oos.writeObject(classesNumber);
		    oos.writeObject(features);
		    oos.writeObject(featuresNumber);
		    System.out.println("done");
		}
		catch(IOException e){
			e.printStackTrace();
		}		
	}
	
	private void readDict(String dictFileName) {
		try{
			System.out.println("Reading dict...");
			FileInputStream fis = new FileInputStream(dictFileName);
		    ObjectInputStream iis = new ObjectInputStream(fis);
			classes =  (HashMap<String, Integer>)iis.readObject();
			classesNumber = (Integer) iis.readObject();
			features = (ArrayList<HashMap<String, Integer>>) iis.readObject();
			featuresNumber = (Integer) iis.readObject();
			System.out.println("done");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		FileFormat me = new FileFormat();
		String meFile = "/home/tm/disk/disk1/conll2012/data/en_part_auto_fea";
		String arffFile = "/home/tm/disk/disk1/conll2012/data/en_train_fea.arff";
		//me.me2arff(meFile, arffFile);
		
		String maxentFileName = "/home/tm/disk/disk1/nermusic/test-maxent-fea";
		String dictFileName = "/home/tm/disk/disk1/nermusic/train-me2svm-dict";
		String svmFileName = "/home/tm/disk/disk1/nermusic/test-svm-fea";
		
		//maxentFileName = args[0];
		//dictFileName = args[1];
		//svmFileName = args[2];
		
		//me.me2svmTrainColumn(maxentFileName, dictFileName, svmFileName);
		//me.me2svmTestColumn(dictFileName, maxentFileName, svmFileName);
		
		String svmRawResultFileName = "/home/tm/disk/disk1/nermusic/test-liblinear-result";
		String svmResultFileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result";
		//me.getSvmResult(dictFileName, svmResultFileName, finalResultFileName);
		
		String testFea = "/home/tm/disk/disk1/nermusic/test-fea";
		String svmFinalResultFileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result-end";
		me.comb(testFea, svmResultFileName, svmFinalResultFileName);
		
	}
	
	
	
	

}
