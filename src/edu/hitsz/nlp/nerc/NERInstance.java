package edu.hitsz.nlp.nerc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;




public class NERInstance {
	
	static String single = "";
	static String  multiStart  = "";
	static String  multiSecond  = "";
	static String  multiThird  = "";
	static String  multiFourth  = "";
	static String  multiMore  = "";
	static String  multiEnd = "";
	static String other = "0";

	public NERInstance(){
		setLabel("4");
	}

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
	 * convert characters back to word by tagset (Zhao, 2010)
	 * <p> 将字符标示转换为词和对应的标示
	 * @param inFileName
	 * @param outFileName
	 * @param tagset
	 */
	public static String character2Sentence(String inFileName, String outFileName, String nerFileName, String tagset){
		StringBuffer finalSentence = new StringBuffer();
		ConllFile newFile = new ConllFile();
		ArrayList<String> allForms = new ArrayList<String>();
		ArrayList<String> allTags = new ArrayList<String>();
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
							String thisTags = tags.get(i);
							if(thisTags.equals(other)){
								newBuffer.append(form.get(i));
								i++;
								continue;
							}
							else{
								String thisTag = (thisTags.split("-"))[0];
								String thisPos = (thisTags.split("-"))[1];
								if(thisTag.equals(multiStart)){
									String newForm = form.get(i);
									String newTag = thisPos;
									newBuffer.append("<"+thisPos+">"+form.get(i));
									i++;
									while(i<form.size()){
										String nextTags = tags.get(i);

										if(nextTags.equals(other)){
											System.out.println(nextTags);
											newBuffer.append(form.get(i));
											i++;
											break;
										}
										String nextTag = (nextTags.split("-"))[0];
										String nextPos = (nextTags.split("-"))[1];
										if(!nextTag.equals(single) && !nextTag.equals(multiStart)
												&& !nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
											newBuffer.append(form.get(i));
											newForm += form.get(i);
											i++;
										}
										else if(nextTag.equals(multiEnd) && thisPos.equals(nextPos)){
											newBuffer.append(form.get(i));
											newForm += form.get(i);
											i++;
											break;
										}
										else{
											break;
										}
									}
									newBuffer.append("</"+thisPos+">");
									allForms.add(newForm);
									allTags.add(newTag);
								}
								else if(thisTag.equals(single)){
									newBuffer.append("<"+thisPos+">"+form.get(i)+"</"+thisPos+">");
									allForms.add(form.get(i));
									allTags.add(thisPos);
									i++;
								}
							}
						}
						finalSentence.append(newBuffer+"\n");
					}
				}
				outWriter.write(finalSentence.toString()+"\n");
				outWriter.close();
				if(allForms.size()>0){
					StringBuffer newBuffer2 = new StringBuffer();
					FileWriter outWriter2  = new FileWriter(nerFileName);
					for(int i=0; i<allForms.size(); i++){
						if(i<allTags.size()){
							newBuffer2.append(allForms.get(i)+"\t"+allTags.get(i)+"\n");
							//outWriter2.write(allForms.get(i)+"\t"+allTags.get(i)+"\n");
						}
					}
					outWriter2.write(newBuffer2.toString());
					outWriter2.close();
				}
			}
			catch (IOException e){
				System.out.println("IOException: " + e);
			}
		}
		return finalSentence.toString();
	}





	public static void main(String[] args) {
		String dir = "/home/tm/disk/d1/nerc/";
		String trainWordFileName = dir +"train_word";
		String trainCharFileName = dir + "train_char";
		NERTrain newNER = new NERTrain();
		//newNER.trainWord2CharacterWithoutPOS(trainWordFileName, trainCharFileName);

		String trainFeatureFileNameStanford = dir + "train_char_fea";
		String trainFeatureFileNameZhangLe = dir + "train_char_fea_zhang";
		//newNER.extractFeatures(trainCharFileName, trainFeatureFileNameStanford, true, true);
		//newNER.extractFeatures(trainCharFileName, trainFeatureFileNameZhangLe, true, false);

		String trainModelFileName = dir + "train_char_model";
		//newNER.stanfordMaximumEntropyTrain(trainFeatureFileNameStanford, trainModelFileName);

		String testWordFileName = dir + "test_word";
		String testCharFileName = dir + "test_char_all";
		//newNER.testWord2CharacterWithoutPOS(testWordFileName, testCharFileName);

		String testFeatureFileNameStanford = dir + "test_char_fea";
		String testFeatureFileNameZhangLe = dir + "test_char_fea_zhang";
		//newNER.extractFeatures(testCharFileName, testFeatureFileNameStanford, false, true);
		//newNER.extractFeatures(testCharFileName, testFeatureFileNameZhangLe, false, false);

		String tagCandidateFileName = dir + "tagCandidate";
		//newNER.generateCandidateBMESO(tagCandidateFileName);
		String testReslutFileNameZhangLe = dir + "test_result_zhang";
		String testFinalFileNameZhangLe = dir + "test_final_zhang";
		//newNER.kBeamMaximumEntropyZhang(testCharFileName, testReslutFileNameZhangLe, tagCandidateFileName, testFinalFileNameZhangLe);


		String localDir = "/home/tm/disk/d1/nerc/";
		String finalFileName = localDir+"result/1-final";
		//NERTrain.callChineseNER(charFileName, resultFileName, finalFileName);
		String senFileName = localDir + "result/1-sen-1";
		String nerFileName = localDir + "result/1-ner";
		NERInstance.character2Sentence(finalFileName, senFileName, nerFileName, "4");

	}

}

