package edu.hitsz.nlp.nermusic;

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
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.Dict;
import edu.hitsz.nlp.util.TypeDict;

/**
 * 提取crf需要的文件格式
 * @author Xinxin Li
 * @since May 23, 2012
 */
public class NerMusic {
	
	TypeDict ArtistBandList ; //0,1	System.out.println("读歌手名列表");	
	TypeDict PrefixList; // 	System.out.println("读前缀列表");
	
	TypeDict EnglishNameList; //英文名字，First Name, Last Name
	
	Dict WordList; //3 	System.out.println("读词典列表");
	Dict GerWordList; //4 	System.out.println("读单字词列表");
	Dict ChSurnameList; //5 	System.out.println("读中文常用姓列表");
	Dict ChSurname_unusualList; //9 	System.out.println("读中文不常用姓列表");
	Dict ChNameCharList; //6 	System.out.println("读中文人名常用列表");
	Dict TranCharList; //7 	System.out.println("读译名常用字列表");
	

	String begin = "BNULL";
	String end = "ENULL";
	
	ArrayList<String> albumList;
	
	
	/**
	 * 读取crf模型需要的特征
	 * @since May 22, 2012
	 */
	public void readDict( ) {
		
		String dir = "/home/tm/disk/disk1/nermusic/dict1/";
		
		String ArtistList_filename=dir + "artists";
		String BandList_filename = dir + "bands";

		String  PrefixList_filename=dir + "prefix";
		
		String EnglishLastName = dir + "EnglishLastName";
		String EnglishFirstNameFemale = dir + "EnglishFirstNameFemale";
		String EnglishFirstNameMale = dir + "EnglishFirstNameMale";

		String WordList_filename=dir + "wordlist.txt";
		String GerWordList_filename=dir + "GerWordList.txt";
		String ChSurnameList_filename=dir + "ChSurnameList.txt";
		String ChNameCharList_filename=dir + "ChNameChar.txt";
		String TranCharList_filename=dir + "TransliterationCharList.txt";
		String PerTitleList_filename=dir + "PersonTitleList.txt";

		String LocSuffixList_filename=dir + "地名后缀_train.txt";
		

		ChSurnameList_filename = dir + "中文常用姓_100.txt";
		String ChSurname_unusualList_filename = dir + "中文不常用姓.txt";

		
		ArtistBandList = TypeDict.read(ArtistList_filename, "ARTIST");
		ArtistBandList.add(BandList_filename, "BAND");
		
		EnglishNameList = TypeDict.read(EnglishLastName, "EnglishLastName");
		EnglishNameList.add(EnglishFirstNameFemale, "EnglishFirstName");
		EnglishNameList.add(EnglishFirstNameMale, "EnglishFirstName");
		
		PrefixList = TypeDict.read(PrefixList_filename);
				
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
		
		System.out.println("所有列表读完");
		
		albumList = new ArrayList<String>();
		albumList.add("辑");albumList.add("EP");albumList.add("碟");

				
	}
	
	/**
	 * 抽取crf需要的特征列表
	 * @since May 22, 2012
	 * @param fileName
	 * @param feaName
	 */
	public void extCRFFea(String fileName, String feaName) {
		
		String label = "";

		String pretwo = begin;
		String preone = begin;
		String current = begin;
		String surone = end;
		String surtwo = end;
		
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
					String[] prefixTags = matchPrefix(characters);
					String[] artistBandTags = matchArtistBand(characters);					
					
					for(int i=0; i<length; i++) {
						current = characters.get(i);
						label = tags.get(i);
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
						String fea = current + "\t" + prefixTags[i] + "\t" + artistBandTags[i] + "\t" 
								+ dealWithCurrentTwo(pretwo,preone,current,surone,surtwo);
						
						int containAlbumBefore = 0;
						for(int j=i-1; j>=0 && !characters.get(j).equals(","); j--)
							if(albumList.contains(characters.get(j))) {
								containAlbumBefore = 1;
								break;
							}
						int containAlbumAfter = 0;
						for(int j=i+1; j<length && !characters.get(j).equals(","); j++)
							if(albumList.contains(characters.get(j))) {
								containAlbumAfter = 1;
								break;
							}
						
						fea += "\t" + containAlbumBefore + "\t" + containAlbumAfter + "\t" + label;
						writer.write(fea+"\n");
					}
					writer.write("\n");
					characters.clear();
					tags.clear();					
				}
				else {					
					String[] parts = line.split("[ \t]");
					if(parts.length == 2) {
						characters.add(parts[0]);
						tags.add(parts[1]);
					}				
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
	
	
	
	/** 提取字典特征 */
	public String dealWithCurrentTwo(String pretwo,String preone, String current, String surone, String surtwo) {

		int curintwo = 0;
		int curintwo_unusual = 0;
		int curinthree = 0;
		int curinfive = 0;
		int curinnine = 0;
		
		int one_curinone = 0;
		//int two_one_curinone = 0;
		int cur_oneinone = 0;

		int one_curinArtist = 0; //infour
		int two_one_curinArtist = 0;
		int two_one_cur_oneinArtist = 0;
		int two_one_cur_one_twoinArtist = 0;

		int two_one_curinone = 0;
		int one_cur_oneinone = 0;
		int cur_one_twoinone = 0;
		int two_one_cur_oneinone = 0;
		int one_cur_one_twoinone = 0;
		
		int one_curinBand = 0;
		int two_one_curinBand = 0;
		
		int one_curinSong = 0;
		int two_one_curinSong = 0;
		
		int one_curinAlbum = 0;
		int two_one_curinAlbum = 0;
		
		int curType = 0;		
		
		curintwo = ChSurnameList.find(current); //find(current,5);
		curintwo_unusual = ChSurname_unusualList.find(current);//find(current,9);
		curinthree = ChNameCharList.find(current);//find(current,6);
		curinfive = TranCharList.find(current);//find(current,7);
		curinnine = GerWordList.find(current);//find(current,4);

		String one_cur = preone + current;
		if(!preone.equals(begin) && !current.equals(end))
		{
			one_curinone = WordList.find(one_cur);//find(one_cur,3);
			String tag = PrefixList.getKeyValue(one_cur);
			if(tag.equals("ARTIST"))
				one_curinArtist = 1;
			else if(tag.equals("BAND"))
				one_curinBand = 1;
			else if(tag.equals("SONG"))
				one_curinSong = 1;
			else if(tag.equals("ALBUM"))
				one_curinAlbum = 1;
		}

		String cur_one = current + surone;
		if(!current.equals(begin) && !surone.equals(end))
		{
			cur_oneinone = WordList.find(cur_one);//find(cur_one,3);
		}

		String two_one_cur = pretwo + preone + current;
		if(!pretwo.equals(begin) && !current.equals(end))
		{
			two_one_curinone =  WordList.find(two_one_cur);//find(two_one_cur,3);
			String tag = PrefixList.getKeyValue(two_one_cur);
			if(tag.equals("ARTIST"))
				two_one_curinArtist = 1;
			else if(tag.equals("BAND"))
				two_one_curinBand = 1;
			else if(tag.equals("SONG"))
				two_one_curinSong = 1;
			else if(tag.equals("ALBUM"))
				two_one_curinAlbum = 1;
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
			//two_one_cur_oneinArtist = PrefixList.get(two_one_cur_one);//find(two_one_cur_one,8);
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
			//two_one_cur_one_twoinfour = PerTitleList.find(two_one_cur_one_two);//find(two_one_cur_one_two,8);
		}
		
		curType = getType(current);
		
		StringBuffer sb = new StringBuffer();
		//sb.append(current);sb.append("\t");
		sb.append(curintwo);sb.append("\t");
		sb.append(curinthree);sb.append("\t");
		sb.append(curinfive);sb.append("\t");
		sb.append(curinnine);sb.append("\t");
		sb.append(one_curinone);sb.append("\t");
		sb.append(one_curinArtist);sb.append("\t");
		sb.append(two_one_curinArtist);sb.append("\t");
		sb.append(two_one_cur_oneinArtist);sb.append("\t");
		sb.append(two_one_cur_one_twoinArtist);sb.append("\t");
		sb.append(cur_oneinone);sb.append("\t");
		sb.append(curintwo_unusual);sb.append("\t");
		sb.append(two_one_curinone);sb.append("\t");
		sb.append(one_cur_oneinone);sb.append("\t");
		sb.append(cur_one_twoinone);sb.append("\t");
		sb.append(two_one_cur_oneinone);sb.append("\t");
		sb.append(one_cur_one_twoinone);sb.append("\t");

		sb.append(one_curinBand);sb.append("\t");
		sb.append(two_one_curinBand);sb.append("\t");
		sb.append(one_curinSong);sb.append("\t");
		sb.append(two_one_curinSong);sb.append("\t");
		sb.append(one_curinAlbum);sb.append("\t");
		sb.append(two_one_curinAlbum);sb.append("\t");
		sb.append(curType);
		/*
		sb.append("\t");
		int i=0;
		if(EnglishNameList.getKeyValue(current).equals("EnglishFirstName"))
			i = 1;
		sb.append(i);sb.append("\t");
		i=0;
		if(EnglishNameList.getKeyValue(current).equals("EnglishLastName"))
			i = 1;
		sb.append(i);sb.append("\t");
		*/
		
		return sb.toString();
	
	}
	
	private static int getType(String s) {
		if(s.matches("[a-zA-Z].*"))
			return 0;
		else if("[。？！，、；：“”．﹁﹂‘’﹃﹄『』（）［］〔〕【】《》〈〉………-—━——──－－～·﹏﹏﹏＿]".contains(s))
			return 1;
		else if (s.matches("[0-9０１２３４５６７８９零一二三四五六七八九十百千万亿].*"))
			return 2;
		else
			return 3;
	}
	
	
	
	/**匹配前缀列表*/
	public String[] matchPrefix(ArrayList<String> characters) {
		int length = characters.size();
		String[] tags = new String[length];
		
		for(int i=0; i < length; i++)
			tags[i] = "O";
		
		for(int i=length-1; i >= 0; i--) {
			for(int j=Math.max(0, i-2); j< Math.max(1, i); j++) {
				StringBuffer sb = new StringBuffer();
				sb.append(characters.get(j));
				for(int m=j+1; m<=i; m++) {
					sb.append(characters.get(m));					
				}
				String tag = PrefixList.getKeyValue(sb.toString());
				if(!tag.equals("O")) {
					ArrayList<String> words = new ArrayList<String>();
					for(int k=j; k<i+1; k++) {
						words.add(characters.get(k));
					}
					String[] subtags = Xml2Column.word2tags(words, "p"+tag);
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
	
	/**匹配歌手名，组合名*/
	public String[] matchArtistBand(ArrayList<String> characters) {
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
				String tag = ArtistBandList.getKeyValue(sb.toString());
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
					writer.write("\n");
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
		int ex_type = 1;

		String dir = "";
		
		String train_file = null;
		String trainfeature_file = null;
		
		train_file =  "/home/tm/disk/disk1/nermusic/test-tag";	///home/tm/windows/try/source/corpus/Train_char_bios_hcrf.txt";
		trainfeature_file = "/home/tm/disk/disk1/nermusic/test-fea";	//"/home/tm/windows/try/source/corpus/train_fea";
				
		NerMusic nerc = new NerMusic();
		nerc.readDict( );
		nerc.extCRFFea(train_file, trainfeature_file);
		
		
		//nerc.combGoldPred("/home/tm/disk/disk1/nermusic/test-tag", 
		//		"/home/tm/disk/disk1/nermusic/test-maxent-predict", "/home/tm/disk/disk1/nermusic/test-maxent-result");
	}
	
	
	
	
	

}
