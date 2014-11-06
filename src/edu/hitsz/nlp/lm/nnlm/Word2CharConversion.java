package edu.hitsz.nlp.lm.nnlm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.SequenceTagCandidate;

/**
 * 将word和对应的tag,转化为BMES
 * @author Xinxin Li
 * @since Nov 28, 2013
 */
public class Word2CharConversion {

	static String[] OntoNotesPosTaggers = {"AD","AS","BA","CC","CD","CS","DEC","DEG","DER","DEV","DT","ETC","FW","JJ","LB","LC","M","MSP","NN","NR","NT","OD","P","PN","PU","SB","SP","VA","VC","VE","VV"};	//"IJ","ON","URL","X"
	static int OntoNotesPosTaggerNumber = OntoNotesPosTaggers.length;

	static String delimiter = "-";

	/**
	 * segment word to characters by different tag sets, according to (Zhao, 2010)
	 * A Unified Character-Based Tagging Framework for Chinese Word Segmentation
	 *
	 * 我们	NN
	 * 相信	VV
	 * 他	NN
	 * ->
	 * 我	B-NN
	 * 们	E-NN
	 * 相	B-VV
	 * 信	E-VV
	 * 他	S-NN
	 *
	 * segmentation tag lists
	 * 2-tag   2        B, E -> B, BE, BEE, ...
	 * 3-tag/a 3a       B, E, S -> S, BE, BEE, ...
	 * 3-tag/b 3b       B, M, E -> B, BE, BME, BMME, ...
	 * 4-tag   4        B, M, E, S -> S, BE, BME, BMME, ...
	 * 5-tag   5        B, B2, M, E, S -> S, BE, BB2E, BB2ME, BB2MME, ...
	 * 6-tag   6        B, B2, B3, M, E, S -> S, BE, BB2E, BB2B3E, BB2B3ME, ...
	 * 7-tag   7        B, B2, B3, B4, M, E, S -> S, BE, BB2E, BB2B3 E, BB2B3B4E, BB2B3B4ME, ...
	 *
	 * to
	 *     single multiStart multiSecond multiThird multiFourth multiMore  ... multiEnd
	 * 2    B        B           E            E         E           E             E
	 * 3a   S        B           E            E         E           E             E
	 * 3b   B        B           M            M         M           M             E
	 * 4    S        B           M            M         M           M             E
	 * 5    S        B           B2           M         M           M             E
	 * 6    S        B           B2           B3        M           M             E
	 * 7    S        B           B2           B3        B4          M             E
	 * @param inFileName
	 * @param outFileName
	 * @param tagset Tagset: 2, 3a, 3b, 4, 5, 6, 7
	 */
	static String single = "";
	static String  multiStart  = "";
	static String  multiSecond  = "";
	static String  multiThird  = "";
	static String  multiFourth  = "";
	static String  multiMore  = "";
	static String  multiEnd = "";

	public static void setLabel(String tagset){
		if(tagset.equals("2")){
			single = multiStart = "B";
			multiSecond = multiThird = multiFourth = multiMore = multiEnd = "E";
		}
		else if(tagset.equals("3a")){
			single = "S";
			multiStart = "B";
			multiSecond = multiThird = multiFourth = multiMore = multiEnd = "E";
		}
		else if(tagset.equals("3b")){
			single = multiStart = "B";
			multiSecond = multiThird = multiFourth = multiMore = "M";
			multiEnd = "E";
		}
		else if(tagset.equals("4")){
			single = "S";
			multiStart = "B";
			multiSecond = multiThird = multiFourth = multiMore = "M";
			multiEnd = "E";
		}
		else if(tagset.equals("5")){
			single = "S";
			multiStart = "B";
			multiSecond = "B2";
			multiThird = multiFourth = multiMore = "M";
			multiEnd = "E";
		}
		else if(tagset.equals("6")){
			single = "S";
			multiStart = "B";
			multiSecond = "B2";
			multiThird = "B3";
			multiFourth = multiMore = "M";
			multiEnd = "E";
		}
		else if(tagset.equals("7")){
			single = "S";
			multiStart = "B";
			multiSecond = "B2";
			multiThird = "B3";
			multiFourth = "B4";
			multiMore = "M";
			multiEnd = "E";
		}
		else{
			System.out.println("Error: no such tagset. Please check");
			System.exit(-1);
		}
	}

	
	/** 
	 * 将word+pos结构通过tagset转换为char+subpos(word boundary+ pos)结构
	 * @param inFileName
	 * @param outFileName
	 * @param tagset 标注集
	 * @param bContainTag 是否包含标注
	 */
	public static void word2Character(String inFileName, String outFileName, String tagset){
		setLabel(tagset);
		try {
			String encoding = FileEncoding.getCharset(inFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), encoding)); 
			String line = null;
			int sentenceNumber = 0;
			int wordNumber = 0;
			int characterNumber = 0;
			
			FileWriter outWriter = new FileWriter(outFileName);
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					if(sentenceNumber % 10000 == 0) {
						System.out.print(sentenceNumber + ".");
						if(sentenceNumber % 100000 == 0)
							System.out.println();						
					}
					sentenceNumber++;
					StringBuffer strbuf = new StringBuffer();
					String[] words = line.split("\\s+");
					for(String word : words) {
						word = word.trim();
						int formSize = word.length();
						if(formSize > 0) {
							wordNumber++;
							characterNumber += formSize;
							if(formSize == 1)
								strbuf.append(word.charAt(0)+"_"+single+" ");
							else{
								for(int i=0; i<formSize; i++){
									if(i == 0)
										strbuf.append(word.charAt(i)+"_"+multiStart+" ");
									else if(i==formSize-1)
										strbuf.append(word.charAt(i)+"_"+multiEnd+" ");
									else if(i == 1)
										strbuf.append(word.charAt(i)+"_"+multiSecond+" ");
									else if(i == 2)
										strbuf.append(word.charAt(i)+"_"+multiThird+" ");
									else if(i == 3)
										strbuf.append(word.charAt(i)+"_"+multiFourth+" ");
									else
										strbuf.append(word.charAt(i)+"_"+multiMore+" ");
								}
							}
						}
					}
					outWriter.write(strbuf.toString().trim()+"\n");
				}
			}
			reader.close();
			outWriter.close();
			System.out.println("sentenceNumber: " + sentenceNumber);
			System.out.println("wordNumber: " + wordNumber);
			System.out.println("characterNumber: " + characterNumber);
		}
		catch (IOException e){
			System.out.println("IOException" + e);
		}
	}

	public static void main(String[] args) {
		String wordFileName = "/home/tm/disk/disk1/word2vec/data.pdgmwsogou.word.clean";
		String charFileName = "/home/tm/disk/disk3/word2vec/data.pdgmwsogou.word2character";
		Word2CharConversion.word2Character(wordFileName, charFileName, "4");		
	}


}

