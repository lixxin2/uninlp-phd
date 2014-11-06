package edu.hitsz.ml.crf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;

/**
 * crf模板文件，可用于转化为其它格式的文件
 * @author Xinxin Li
 * @since Jun 12, 2012
 */
public class Template {

	boolean B;
	
	ArrayList<OneTempl> templs;
	
	public Template() {
		templs = new ArrayList<OneTempl>();
		B = false;
	}
	
	/** 
	 * 从文件中读取模板
	 */
	public void read(String fileName) {
		
		BufferedReader reader = null;
		String line = null;
		try{
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			//对于每一行特征
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() < 1 || line.startsWith("#"))
					continue;
				if(line.equals("B")) {
					B = true;
					continue;
				}
				String[] part = line.split(":");
				if(part.length != 2) {
					System.out.println("The format of template is wrong");
					System.exit(-1);
				}
				
				OneTempl onetempl = new OneTempl();
				onetempl.label = part[0];
				
				String[] subtemplStrings = part[1].split("/");
				for(String s : subtemplStrings) {
					int start = s.indexOf("[") + 1;
					int end = s.indexOf("]");
					String subtemplString = s.substring(start, end);
					String[] subs = subtemplString.split(",");
					SubTempl subtempl = new SubTempl(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
					onetempl.subTempls.add(subtempl);
				}
				
				templs.add(onetempl);
				
			}
			reader.close();			
		}
		catch (IOException e) {
			
		}		
		
	}

	
	/**
	 * 根据模板，从一个矩形词组中，抽取出所有的特征的列表
	 * @since May 22, 2012
	 * @param words
	 * @param row
	 * @param column
	 * @param i
	 * @return
	 */
	public ArrayList<String> getFea(ArrayList<ArrayList<String>> words, int row, int column, int i) {
		
		ArrayList<String> features = new ArrayList<String>();
		int templSize = templs.size();
		
		for(int j=0; j<templSize-1; j++) {
			OneTempl onetempl = templs.get(j);
			String oneFeature = onetempl.getFea(words, row, column, i); 
			features.add(oneFeature.trim());		
		}
		OneTempl onetempl = templs.get(templSize-1);
		String oneFeature = onetempl.getFea(words, row, column, i); 
		features.add(oneFeature.trim());
		
		return features;
				
	}
	
	
	
	
}

class OneTempl {
	
	String label;
	ArrayList<SubTempl> subTempls;
	
	public OneTempl() {
		subTempls = new ArrayList<SubTempl>();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(label+":");
		int subtemplsize = subTempls.size();
		for(int i=0; i<subtemplsize-1; i++) {
			SubTempl subtempl = subTempls.get(i);
			sb.append("%x"+subtempl + "/");
		}
		sb.append("%x"+subTempls.get(subtemplsize-1));			
		
		return sb.toString();
	}
	
	public String getFea(ArrayList<ArrayList<String>> words, int row, int column, int i) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(label+":");
		int subtemplsize = subTempls.size();
		for(int j=0; j<subtemplsize-1; j++) {
			SubTempl subtempl = subTempls.get(j);
			String feature = "NONE";
			int curRow = i + subtempl.row;
			int curColumn = subtempl.column;
			if(curRow>=0 && curRow<row && curColumn>=0 && curColumn<column) 
				feature = words.get(curColumn).get(curRow);
			sb.append(feature + "/");
		}
		SubTempl subtempl = subTempls.get(subtemplsize-1);
		String feature = "NONE";
		int curRow = i + subtempl.row;
		int curColumn = subtempl.column;
		if(curRow>=0 && curRow<row && curColumn>=0 && curColumn<column) 
			feature = words.get(curColumn).get(curRow);		
		sb.append(feature);
		
		return sb.toString();		
		
	}
	
}

class SubTempl {
	
	int row;
	int column;
	
	public SubTempl(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public String toString() {
		return new String("["+row+","+column+"]");
	}
	
}
