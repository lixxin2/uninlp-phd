package edu.hitsz.nlp.segpos;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.SequenceTagCandidate;

/**
 * 将word和对应的tag,转化为BMES
 * @author Xinxin Li
 * @since Nov 28, 2013
 */
public class Word2Char {

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
	public static void word2Character(String inFileName, String outFileName, String tagset, boolean bContainTag){
		
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		
		if(newFile.getSentenceNumber()>0){
			setLabel(tagset);
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				//read a sentence
				for(ConllSentence newSentence : newFile.getTotalSentence()){
					if(newSentence.getSentenceLength()>0){
						ArrayList<String> form = newSentence.getWords(0);
						
						//contain tag
						if(newSentence.getWords().size() > 1 && bContainTag)
							bContainTag = true;						
						ArrayList<String> pos = new ArrayList<String>();
						if(bContainTag)
							pos = newSentence.getWords(1);
						
						//for each word
						for(int m=0; m<form.size(); m++){
							String singleForm = form.get(m);
							String singlePos = "";
							if(bContainTag)
								singlePos = delimiter + pos.get(m);
							int formSize = singleForm.length();
							if(formSize == 1)
								outWriter.write(singleForm.charAt(0)+"\t"+single+singlePos+"\n");
							else{
								for(int i=0; i<formSize; i++){
									if(i == 0)
										outWriter.write(singleForm.charAt(i)+"\t"+multiStart+singlePos+"\n");
									else if(i==formSize-1)
										outWriter.write(singleForm.charAt(i)+"\t"+multiEnd+singlePos+"\n");
									else if(i == 1)
										outWriter.write(singleForm.charAt(i)+"\t"+multiSecond+singlePos+"\n");
									else if(i == 2)
										outWriter.write(singleForm.charAt(i)+"\t"+multiThird+singlePos+"\n");
									else if(i == 3)
										outWriter.write(singleForm.charAt(i)+"\t"+multiFourth+singlePos+"\n");
									else
										outWriter.write(singleForm.charAt(i)+"\t"+multiMore+singlePos+"\n");
								}
							}
						}
					}
					outWriter.write("\n");
				}
				outWriter.close();
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}


	/**
	 * convert characters back to word by tagset (Zhao, 2010)
	 * @param inFileName
	 * @param outFileName
	 * @param tagset
	 */
	public static void character2Word(String inFileName, 
			String outFileName, 
			String tagset){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		if(newFile.getSentenceNumber()>0){
			setLabel(tagset);
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				int m=0;
				for(ConllSentence newSentence : newFile.getTotalSentence()){
					System.out.println(m++);
					if(newSentence.getSentenceLength()>0){
						StringBuffer newBuffer = new StringBuffer();
						ArrayList<String> form = newSentence.getWords(0);
						ArrayList<String> tags = newSentence.getWords(1);
						int i=0;
						while(i<form.size()){
							//
							String[] thisTags = tags.get(i).split(delimiter);
							String thisTag = thisTags[0];
							String thisPos = "S";
							if(thisTags.length > 1)
								thisPos = thisTags[1];
							if(thisTag.equals(multiStart)){
								newBuffer.append(form.get(i));
								i++;
								while(i<form.size()){
									String[] nextTags = tags.get(i).split(delimiter);
									String nextTag = nextTags[0];
									String nextPos = "S";
									if(nextTags.length > 1)
										nextPos = nextTags[1];
									if(!nextTag.equals(single) && !nextTag.equals(multiStart)
											&& !nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
										newBuffer.append(form.get(i));
										i++;
									}
									else if(nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
										newBuffer.append(form.get(i));
										i++;
										break;
									}
									else{
										break;
									}
								}
								newBuffer.append("\t"+thisPos+"\n");
							}
							else if(thisTag.equals(single)){
								newBuffer.append(form.get(i)+"\t"+thisPos+"\n");
								i++;
							}
							else{
								newBuffer.append(form.get(i));
								i++;
								while(i<form.size()){
									String[] nextTags = tags.get(i).split(delimiter);
									String nextTag = nextTags[0];
									String nextPos = "S";
									if(nextTags.length > 1)
										nextPos = nextTags[1];
									if(!nextTag.equals(single) && !nextTag.equals(multiStart)
											&& !nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
										newBuffer.append(form.get(i));
										i++;
									}
									else if(nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
										newBuffer.append(form.get(i));
										i++;
										break;
									}
									else{
										break;
									}
								}
								newBuffer.append("\t"+thisPos+"\n");
							}
						}
						outWriter.write(newBuffer.toString()+"\n");
					}
				}
				outWriter.close();
			}
			catch (IOException e){
				System.out.println("IOException: " + e);
			}
		}
	}

	/**
	 * 生成tag候选集
	 * @param tagset
	 * @param tagCandidateFileName
	 */
	public void generateCTBCandidates(String tagset, String tagCandidateFileName){
		setLabel("4");
		System.out.println(OntoNotesPosTaggers.length);
		int tagNumber = OntoNotesPosTaggers.length;
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		for(int i=0; i<tagNumber; i++){
			String	singleTag = single+delimiter+OntoNotesPosTaggers[i];
			String  multiStartTag  = multiStart+delimiter+OntoNotesPosTaggers[i];
			String  multiSecondTag  = multiSecond+delimiter+OntoNotesPosTaggers[i];
			String  multiThirdTag  = multiThird+delimiter+OntoNotesPosTaggers[i];
			String  multiFourthTag  = multiFourth+delimiter+OntoNotesPosTaggers[i];
			String  multiMoreTag  = multiMore+delimiter+OntoNotesPosTaggers[i];
			String  multiEndTag = multiEnd+delimiter+OntoNotesPosTaggers[i];
			if(!newCand.candidates.containsKey(singleTag)){
				newCand.starts.add(singleTag);
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<tagNumber; j++){
					String tmp = single+delimiter+OntoNotesPosTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
					tmp = multiStart+delimiter+OntoNotesPosTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				newCand.candidates.put(singleTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiStartTag)){
				newCand.starts.add(multiStartTag);
				ArrayList<String> newVec= new ArrayList<String>();
				if(!newVec.contains(multiSecondTag))
						newVec.add(multiSecondTag);
				if(!newVec.contains(multiEndTag))
					newVec.add(multiEndTag);
				newCand.candidates.put(multiStartTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiSecondTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				if(!newVec.contains(multiThirdTag))
						newVec.add(multiThirdTag);
				if(!newVec.contains(multiEndTag))
					newVec.add(multiEndTag);
				newCand.candidates.put(multiSecondTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiThirdTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				if(!newVec.contains(multiFourthTag))
						newVec.add(multiFourthTag);
				if(!newVec.contains(multiEndTag))
					newVec.add(multiEndTag);
				newCand.candidates.put(multiThirdTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiFourthTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				if(!newVec.contains(multiMoreTag))
						newVec.add(multiMoreTag);
				if(!newVec.contains(multiEndTag))
					newVec.add(multiEndTag);
				newCand.candidates.put(multiFourthTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiMoreTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				if(!newVec.contains(multiEndTag))
					newVec.add(multiEndTag);
				newCand.candidates.put(multiMoreTag, newVec);
			}
			if(!newCand.candidates.containsKey(multiEndTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<tagNumber; j++){
					String tmp = single+delimiter+OntoNotesPosTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
					tmp = multiStart+delimiter+OntoNotesPosTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				newCand.candidates.put(multiEndTag, newVec);
			}
		}
		newCand.store(tagCandidateFileName);
	}


	/**
	 * 生成一个词中所有字可能的label，根据length和Pos，比如NN：B-NN,E-NN
	 * @since Dec 13, 2011
	 * @param length
	 * @param posTagger
	 * @return
	 */
	public static String[] generateTag(int length, String posTagger){
		String[] tags = new String[length];
		if(length == 1)
			tags[0] = "S"+delimiter+posTagger;
		else if(length>1){
			tags[0] = "B"+delimiter+posTagger;
			for(int i=1; i<length-1; i++){
				tags[i] = "M"+delimiter+posTagger;
			}
			tags[length-1] = "E"+delimiter+posTagger;
		}
		else{
			System.out.println("the input length must be an integer");
			System.exit(-1);
		}
		return tags;
	}

	/**
	 * 生成一个词中所有字可能的label，根据length和Pos，比如NN：B-NN,E-NN
	 * @since Dec 13, 2011
	 * @param length
	 * @param posTagger
	 * @return
	 */
	public static String[] generateAllTag(String[] postags){
		int posNumber = postags.length;
		String[] charTags = new String[posNumber*4];
		int j=0;
		for(int i=0; i<posNumber; i++){
			charTags[j++] = "S" + delimiter + postags[i];
			charTags[j++] = "B" + delimiter + postags[i];
			charTags[j++] = "M" + delimiter + postags[i];
			charTags[j++] = "E" + delimiter + postags[i];
		}
		return charTags;
	}



	public static void main(String[] args) {
		//String segposFileName = "/home/tm/disk/disk1/nnseg/data/ctb5-test-wp";
		//String charFileName = "/home/tm/disk/disk1/nnseg/data/ctb5-test-w-char";
		//Word2Char.word2Character(segposFileName, charFileName, "4", false);
		String data = "ctb5-test";
		String type = "segpos";
		for(int i=1; i<50; i++) {
			String charResult = "/home/tm/disk/disk1/nnsegpos/model."+type+"/" + data + "-random2-n.output.result.final-"+Integer.toString(i);
			String wordResult = "/home/tm/disk/disk1/nnsegpos/model."+type+"/" + data + "-random2-n.output.result.final.word-"+Integer.toString(i);
			Word2Char.character2Word(charResult, wordResult, "4");
		}
		
	}


}
