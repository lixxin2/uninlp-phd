package edu.hitsz.nlp.segmentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 分词文件
 * @author Xinxin Li
 * Dec 3, 2011
 */
public class SegmentFile extends ArrayList<SegmentSentence>{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	int sentenceNumber=0;

	/**
	 * 读取分词类型的文件，格式如下：
	 * 人们  常  说  生活  是  一  部  教科书。
	 *
	 * @param trainFileName
	 * @param encodeType
	 */
	public void readTrainLine(String trainFileName, int size, String encodeType){
		if(size <= 0)
			size=Integer.MAX_VALUE;
		File file = new File(trainFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed:");
			String tempString=null;
			ArrayList<String> tempSentence=new ArrayList<String>();
			System.out.print("Have Read ");
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if(!tempString.trim().equals("")){
					tempSentence.add(tempString.trim());
					//
					if (tempString!=null){
						//System.out.println("Have Read the "+sentenceNumber+"th Sentence;");
						if(sentenceNumber%10000==0)
							System.out.print(sentenceNumber+"...\n");
						else if(sentenceNumber%1000==0)
							System.out.print(sentenceNumber+"...");
						if(sentenceNumber<size){
							//System.out.println("Have Read "+sentenceNumber+"th Sentences.");
							SegmentSentence sentence=new SegmentSentence();
							sentence.processTrainLine(tempSentence);
							//sentence.outputTrainSentence();
							this.add(sentence);
							sentenceNumber++;
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}
			}
			System.out.println(sentenceNumber+" Sentences is done.");
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	/**
	 * 读取分词类型的文件，转化为每个字一个词格式如下：
	 * 人们常说生活是一部教科书。
	 *
	 * @param trainFileName
	 * @param encodeType
	 */
	public void readRaw(String trainFileName, int size, String encodeType){
		if(size <= 0)
			size=Integer.MAX_VALUE;
		File file = new File(trainFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed:");
			String tempString=null;
			ArrayList<String> tempSentence=new ArrayList<String>();
			System.out.print("Have Read ");
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if(!tempString.trim().equals("")){
					tempSentence.add(tempString.trim());
					//
					if (tempString!=null){
						//System.out.println("Have Read the "+sentenceNumber+"th Sentence;");
						if(sentenceNumber%10000==0)
							System.out.print(sentenceNumber+"...\n");
						else if(sentenceNumber%1000==0)
							System.out.print(sentenceNumber+"...");
						if(sentenceNumber<size){
							//System.out.println("Have Read "+sentenceNumber+"th Sentences.");
							SegmentSentence sentence=new SegmentSentence();
							sentence.processRaw(tempSentence);
							//sentence.outputTrainSentence();
							this.add(sentence);
							sentenceNumber++;
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}
			}
			System.out.println(sentenceNumber+" Sentences is done.");
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


}
