package edu.hitsz.nlp.nerc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.ml.maxent.jni.MaximumEntropyJNI;
import edu.hitsz.nlp.nermusic.Xml2Column;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.Dict;
import edu.hitsz.nlp.util.TypeDict;

public class Nerc {
	
	TypeDict OrgLocPerList; //0,3 	System.out.println("读机构名列表"); 
							//0,2 	System.out.println("读地名列表"); 	
							//0,1	System.out.println("读人名列表");
	
	TypeDict LocOrgSuffixList;	//1,4 	System.out.println("读地名后缀列表");
								//1,5 	System.out.println("读机构名后缀列表");
	
	Dict WordList; //3 	System.out.println("读词典列表");
	Dict GerWordList; //4 	System.out.println("读单字词列表");
	Dict ChSurnameList; //5 	System.out.println("读中文常用姓列表");
	Dict ChNameCharList; //6 	System.out.println("读中文人名常用列表");
	Dict TranCharList; //7 	System.out.println("读译名常用字列表");
	Dict ChSurname_unusualList; //9 	System.out.println("读中文不常用姓列表");

	Dict PerTitleList; //8 		System.out.println("读人名前缀词列表");
	

	String begin = "BNULL";
	String end = "ENULL";
	
	
	
	/**
	 * 读取词典
	 * @since May 28, 2012
	 * @param ex_type 表示是训练语料还是测试语料, 0表示训练语料，1表示测试语料
	 */
	public void readDict(int ex_type) {
		
		String dir = "/home/tm/disk/disk1/nerc/dict1/";
		
		String OrgSuffixList_filename = null;
		String PERList_filename = null;
		String LOCList_filename = null;
		String ORGList_filename = null;
		
		
		if(ex_type==0)
		{
			OrgSuffixList_filename= dir + "机构名后缀_test.txt";
			PERList_filename=dir + "语料中文人名_3_日本人名_2_单词译名_3_train.txt";
			LOCList_filename=dir + "地名_4_train.txt";
			ORGList_filename=dir + "机构名_2_train.txt";			
		}
		else
		{
			OrgSuffixList_filename=dir + "机构名后缀_test.txt";
			PERList_filename=dir + "中文日文译文人名_13_test.txt";
			LOCList_filename=dir + "地名_13_test.txt";
			ORGList_filename=dir + "机构名_13_test.txt";
		}

		String WordList_filename=dir + "wordlist.txt";
		String GerWordList_filename=dir + "GerWordList.txt";
		String ChSurnameList_filename=dir + "ChSurnameList.txt";
		String ChNameCharList_filename=dir + "ChNameChar.txt";
		String TranCharList_filename=dir + "TransliterationCharList.txt";
		String PerTitleList_filename=dir + "PersonTitleList.txt";

		String LocSuffixList_filename=dir + "地名后缀_train.txt";
		
		ChSurnameList_filename = dir + "中文常用姓_100.txt";
		String ChSurname_unusualList_filename = dir + "中文不常用姓.txt";
			
		OrgLocPerList = TypeDict.read(ORGList_filename, "org");
		System.out.println("读机构名列表");
		OrgLocPerList.add(LOCList_filename, "loc");
		System.out.println("读地名列表");
		OrgLocPerList.add(PERList_filename, "loc");
		System.out.println("读人名列表");
		
		LocOrgSuffixList = TypeDict.read(LocSuffixList_filename, "loc");
		System.out.println("读地名后缀列表");
		LocOrgSuffixList.add(OrgSuffixList_filename, "org");
		System.out.println("读机构名后缀列表");
		
		WordList = Dict.read(WordList_filename); //3
		System.out.println("读词典列表");
		GerWordList = Dict.read(GerWordList_filename); //4
		System.out.println("读单字词列表");
		ChSurnameList = Dict.read(ChSurnameList_filename); //5
		System.out.println("读中文常用姓列表");
		ChSurname_unusualList = Dict.read(ChSurname_unusualList_filename); //9
		System.out.println("读中文不常用姓列表");
		ChNameCharList = Dict.read(ChNameCharList_filename); //6
		System.out.println("读中文人名常用列表");
		TranCharList = Dict.read(TranCharList_filename); //7
		System.out.println("读译名常用字列表");
		PerTitleList = Dict.read(PerTitleList_filename); //8
		System.out.println("读人名前缀词列表");
		
		System.out.println("所有列表读完");
		
				
	}
	
	/**
	 * 抽取crf需要的特征列表
	 * @since May 22, 2012
	 * @param fileName
	 * @param feaName
	 * @param ex_type
	 */
	public void extCRFFea(String fileName, String feaName, int ex_type) {
		
		String label = "";

		String pretwo = begin;
		String preone = begin;
		String current = begin;
		String surone = end;
		String surtwo = end;
		//存储当前字和其tag
		ArrayList<String> characters = new ArrayList<String>();
		ArrayList<String> tags = new ArrayList<String>();
		
		int sentenceNumber = 0;
		
		BufferedReader reader = null;
		FileWriter writer = null;
		String line = null;
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			writer = new FileWriter(new File(feaName));
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					sentenceNumber++;
					if(sentenceNumber % 100 == 0)
						System.out.println(sentenceNumber);
					int length = characters.size();
					String[] prefixTags = matchLocOrgSuffix(characters);
					String[] curOrgLocPerTags = matchOrgLocPer(characters);					
					
					for(int i=0; i<length; i++) {
						current = characters.get(i);
						
						if(i > 0) {
							preone = characters.get(i-1);
							if(i > 1)
								pretwo = characters.get(i-2);
						}
						if(i < length - 1) {
							surone = characters.get(i + 1);
							if(i < length - 2)
								surtwo = characters.get(i + 2);
						}
						String fea = current + "\t" + prefixTags[i] + "\t" + curOrgLocPerTags[i] + "\t" 
								+ dealWithCurrentTwo(pretwo,preone,current,surone,surtwo);
						
						if(ex_type == 0) {		
							label = tags.get(i);
							fea += "\t" + label;
						}
						writer.write(fea+"\n");
					}
					writer.write("\n");
					characters.clear();
					tags.clear();					
				}
				else {					
					String[] parts = line.split("[ \t]");
					if(parts.length > 2) {
						characters.add(parts[0]);
						tags.add(parts[2]);
					}		
					else
						characters.add(parts[0]);
				}
				
			}
			System.out.println(sentenceNumber);
			reader.close();
			writer.close();
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	//其他的词典信息
	public String dealWithCurrentTwo(String pretwo,String preone, String current, String surone, String surtwo) {

		int curintwo = 0;          //常见姓 
		int curintwo_unusual = 0;  //不常见姓
		int curinthree = 0;        //单名
		int curinfive = 0;         //翻译名
		int curinnine = 0;         //单字词列表
		
		int one_curinone = 0;      //2 in 词典列表
		//int two_one_curinone = 0;
		int cur_oneinone = 0;      //2 in 人名前缀词列表

		int one_curinfour = 0;      //2 in 词典列表
		int two_one_curinfour = 0;  //
		int two_one_cur_oneinfour = 0;  //
		int two_one_cur_one_twoinfour = 0; //

		int two_one_curinone = 0;
		int one_cur_oneinone = 0;
		int cur_one_twoinone = 0;
		int two_one_cur_oneinone = 0;
		int one_cur_one_twoinone = 0;
		
		if(current.equals("。")||current.equals("，")||current.equals("、"))
		{
		}
		else
		{
			curintwo = ChSurnameList.find(current); //find(current,5);
			curintwo_unusual = ChSurname_unusualList.find(current);//find(current,9);
			curinthree = ChNameCharList.find(current);//find(current,6);
			curinfive = TranCharList.find(current);//find(current,7);
			curinnine = GerWordList.find(current);//find(current,4);

			String one_cur = preone + current;
			if(!preone.equals(begin) && !current.equals(end))
			{
				one_curinone = WordList.find(one_cur);//find(one_cur,3);
				one_curinfour = PerTitleList.find(one_cur);//find(one_cur,8);
			}

			String cur_one = current + surone;
			if(!current.equals(begin) && !surone.equals(end))
			{
				cur_oneinone = WordList.find(cur_one);//find(cur_one,3);
			}

			String two_one_cur = pretwo + preone + current;
			if(!pretwo.equals(begin) && !current.equals(end))
			{
				two_one_curinfour = PerTitleList.find(two_one_cur);//find(two_one_cur,8);
				two_one_curinone =  WordList.find(two_one_cur);//find(two_one_cur,3);
			}

			String one_cur_one = preone + current + surone;
			if(!preone.equals(begin) && !surone.equals(end))
			{
				one_cur_oneinone = WordList.find(one_cur_one);//find(one_cur_one,3);
			}

			String cur_one_two = current + surone + surtwo;
			if(!current.equals(begin) && !surtwo.equals(end))
			{
				cur_one_twoinone = WordList.find(cur_one_two);//find(cur_one_two,3);
			}
			
			String two_one_cur_one = pretwo + preone + current +surone;
			if(!pretwo.equals(begin) && !surone.equals(end))
			{
				two_one_cur_oneinfour = PerTitleList.find(two_one_cur_one);//find(two_one_cur_one,8);
				two_one_cur_oneinone = WordList.find(two_one_cur_one);//find(two_one_cur_one,3);
			}

			String one_cur_one_two = preone + current + surone + surtwo;
			if(!preone.equals(begin) && !surtwo.equals(end))
			{
				one_cur_one_twoinone = WordList.find(one_cur_one_two);//find(one_cur_one_two,3);
			}

			String two_one_cur_one_two = pretwo + preone + current + surone + surtwo;
			if(!pretwo.equals(begin) && !surtwo.endsWith(end))
			{
				two_one_cur_one_twoinfour = PerTitleList.find(two_one_cur_one_two);//find(two_one_cur_one_two,8);
			}
		}
		StringBuffer sb = new StringBuffer();
		//sb.append(current);sb.append("\t");
		sb.append(curintwo);sb.append("\t");
		sb.append(curinthree);sb.append("\t");
		sb.append(curinfive);sb.append("\t");
		sb.append(curinnine);sb.append("\t");
		sb.append(one_curinone);sb.append("\t");
		sb.append(one_curinfour);sb.append("\t");
		sb.append(two_one_curinfour);sb.append("\t");
		sb.append(two_one_cur_oneinfour);sb.append("\t");
		sb.append(two_one_cur_one_twoinfour);sb.append("\t");
		sb.append(cur_oneinone);sb.append("\t");
		sb.append(curintwo_unusual);sb.append("\t");
		sb.append(two_one_curinone);sb.append("\t");
		sb.append(one_cur_oneinone);sb.append("\t");
		sb.append(cur_one_twoinone);sb.append("\t");
		sb.append(two_one_cur_oneinone);sb.append("\t");
		sb.append(one_cur_one_twoinone);
		return sb.toString();
	
	}
	
	
	/**
	 * 匹配地名、组织名后缀列表
	 */
	public String[] matchLocOrgSuffix(ArrayList<String> characters) {
		int length = characters.size();
		String[] tags = new String[length];
		
		for(int i=0; i < length; i++)
			tags[i] = "O";
		//i 结束位
		for(int i=length-1; i >= 0; i--) {
			//j 起始位
			for(int j=Math.max(0, i-2); j< Math.max(1, i); j++) {
				StringBuffer sb = new StringBuffer();
				sb.append(characters.get(j));
				for(int m=j+1; m<=i; m++) {
					sb.append(characters.get(m));					
				}
				String tag = LocOrgSuffixList.getKeyValue(sb.toString());
				if(!tag.equals("O")) {
					ArrayList<String> words = new ArrayList<String>();
					for(int k=j; k<i+1; k++) {
						words.add(characters.get(k));
					}
					String[] subtags = Xml2Column.word2tags(words, "s"+tag);
					for(int k=j; k<i+1; k++) {
						tags[k] = subtags[i-j];
					}
					i = j;
					break;
				}		
			}
		}
		return tags;		
	}
	
	
	
	/**匹配组织名，机构名，人名清单*/
	public String[] matchOrgLocPer(ArrayList<String> characters) {
		int length = characters.size();
		String[] tags = new String[length];
		
		for(int i=0; i < length; i++)
			tags[i] = "O";
		
		for(int i=length-1; i >= 0; i--) {
			for(int j=Math.max(i-9, 0); j<=i; j++) {
				StringBuffer sb = new StringBuffer();
				ArrayList<String> sbList = new ArrayList<String>();
				sb.append(characters.get(j));
				sbList.add(characters.get(j));
				for(int k=j+1; k<=i && k<length; k++) {
					if(characters.get(k).matches("[a-zA-Z].*"))
						sb.append(" "+characters.get(k));
					else
						sb.append(characters.get(k));
					sbList.add(characters.get(k));
				}
				String tag = OrgLocPerList.getKeyValue(sb.toString());
				if(!tag.equals("O")) {
					String[] subtags = Xml2Column.word2tags(sbList, tag);
					for(int k=j; k<i+1; k++) {
						tags[k] = subtags[k-j];
					}
					i = j;
					break;
				}
			}				
		}
		
		return tags;		
	}
	
	
	/** 获得最大熵的结果*/
	public void getMaxentResult(String modelFileName, String feaFileName, String resultFileName) {
		MaximumEntropyJNI newME = new MaximumEntropyJNI();
		newME.load(modelFileName);
		try {
			String encoding = FileEncoding.getCharset(feaFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(feaFileName), encoding));
			FileWriter writer = new FileWriter(new File(resultFileName));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					//writer.write("\n");
				}
				else {
					String[] context = line.split("[ \t]");
					String label = newME.predict(context);
					writer.write(label+"\n");
				}
			}
		}
		catch (IOException e){
			
		}		
	}
	
	
	
	/**将标准结果和maxent产生的结果合并*/
	public void combGoldPred(String goldFileName, String predictFileName, String resultFileName) {
		try {
			String encoding1 = FileEncoding.getCharset(goldFileName);
			BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), encoding1));
			String encoding2 = FileEncoding.getCharset(predictFileName);
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), encoding2));
			
			FileWriter writer = new FileWriter(new File(resultFileName));
			
			String line1 = null;
			String line2 = null;
			while((line1 = reader1.readLine()) != null && (line2 = reader2.readLine())!= null) {
				line1 = line1.trim();
				line2 = line2.trim();
				if(line1.length() > 0) {
					writer.write(line1 + "\t" + line2 + "\n");
				}
				else
					writer.write("\n");
			}
			reader1.close();
			reader2.close();
			writer.close();
		}
		catch (IOException e) {
			
		}
	}
	
	
	
	
	public static void main(String[] args) {
		int ex_type = 0;
		if(ex_type == 0) {
			String train_file = "/home/tm/disk/disk1/nerc/train_char";
			String trainfeature_file = "/home/tm/disk/disk1/nerc/train-char-crfFea";
			
			Nerc nerc = new Nerc();
			nerc.readDict(ex_type);
			nerc.extCRFFea(train_file, trainfeature_file, ex_type);
		}
		else if(ex_type == 1){
			String test_file = "/home/tm/disk/disk1/nerc/test_char";
			String testfeature_file = "/home/tm/disk/disk1/nerc/test-char-crfFea";
			
			Nerc nerc = new Nerc();
			nerc.readDict(ex_type);
			nerc.extCRFFea(test_file, testfeature_file, ex_type);
		}
		
		/*
		String maxentModelFileName = "/home/tm/disk/disk1/nerc/train-maxent-model";
		String testFeaFileName = "/home/tm/disk/disk1/nerc/test-maxent-fea";
		String testResultFileName = "/home/tm/disk/disk1/nerc/test-maxent-result";
		Nerc nerc = new Nerc();
		//nerc.getMaxentResult(maxentModelFileName, testFeaFileName, testResultFileName);
		
		String goldFileName = "/home/tm/disk/disk1/nerc/gold";
		String testFinalName = "/home/tm/disk/disk1/nerc/test-maxent-final";
		nerc.combGoldPred(goldFileName, testResultFileName, testFinalName);
		*/
	}
	
	

}
