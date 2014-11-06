package edu.hitsz.nlp.subWordSegPOS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import edu.hitsz.nlp.util.Array;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Reader;
import edu.hitsz.nlp.segpos.Writer;

/**
 * 合并不同模型得到的subword序列
 * @author Xinxin Li
 * @since Feb 23, 2013
 */
public class SubWordComb {

	/**
	 * 合并不同模型得到的分词结果，得到其subword序列
	 * @since Feb 23, 2013
	 * @param goldFileName 正确的训练文件(word + pos)，如果没有gold设置其为tagResultFileName
	 * @param tagResultFileName 带有词性的一个模型产生的结果文件， word + pos
	 * @param predictFileNames 其他不同模型的结果文件和gold文件
	 * @param subWordFile 子词的结果文件 
	 * @throws IOException 
	 */
	public void generateSubWords(String goldFileName,
			String tagResultFileName,
			ArrayList<String> predictFileNames,			
			String subWordFile) throws IOException {		

		Options options = new Options();
		//读取所有的实例
		//gold reader
		Reader goldReader = new Reader();
		goldReader.startReading(goldFileName, options);
		Instance goldInstance = goldReader.getNext();
		//tag reader
		Reader tagReader = new Reader();
		tagReader.startReading(tagResultFileName, options);
		Instance tagInstance = tagReader.getNext();
		//other readers
		ArrayList<Reader> readers = new ArrayList<Reader>();
		ArrayList<Instance> instances = new ArrayList<Instance>();
		int fileNumber = predictFileNames.size();
		for(int i=0; i<fileNumber; i++) {
			Reader segposReader = new Reader();
			segposReader.startReading(predictFileNames.get(i), options);
			readers.add(segposReader);
			Instance instance = readers.get(i).getNextRaw();
			if(instance != null)
				instances.add(instance);			
		}	
				
		Writer segposWriter = new Writer();
		segposWriter.startWriting(subWordFile, options);			
		
		int count = 1;
		//如果每个实例都存在
		while(goldInstance != null && tagInstance != null 
				&& instances.size() == fileNumber) {
			
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count % 1000 == 0)
					System.out.println();
			}
			//System.out.println(count);
			count++;
			//gold
			String[] goldTags = goldInstance.tags;
			Object[] goldObjs = getPositions(goldInstance);
			ArrayList<Integer> goldPositions = (ArrayList<Integer>) goldObjs[0];
			String[] goldChars = (String[]) goldObjs[1];
			//tag
			String[] predTags = tagInstance.tags;
			Object[] tagObjs = getPositions(tagInstance);
			ArrayList<Integer> tagPositions = (ArrayList<Integer>) tagObjs[0];
			//			
			instances.add(0,goldInstance);
			instances.add(1,tagInstance);
			ArrayList<Integer> allPositions = getAllPositions(instances);
			
			//分配tag
			int subWordNumber = allPositions.size()-1;
			String[] subWords = new String[subWordNumber];
			String[] subTags = new String[subWordNumber];
			String[] subGoldTags = new String[subWordNumber];
			
			String goldTag = "";
			String tag = "";
			//对于每个词
			for(int i=0; i<subWordNumber; i++) {
				
				int start = allPositions.get(i);
				int end = allPositions.get(i+1);
				subWords[i] = Array.toWord(goldChars, start, end);
				
				//首先检查首字是否在tagPositions中
				int tagIndex = tagPositions.indexOf(start);
				//如果包含词首
				if(tagIndex > -1) {
					tag = predTags[tagIndex];   
					//如果包含词尾
					if(tagPositions.contains(end))
						subTags[i] = tag;
					else
						subTags[i] = "B-"+tag;
				}
				//如果不包含词首
				else
					subTags[i] = "I-"+tag;
				
				//首先检查首字是否在goldPosition中
				int goldIndex = goldPositions.indexOf(start);
				if(goldIndex > -1) {
					goldTag = goldTags[goldIndex]; //					
					if(goldPositions.contains(end))
						subGoldTags[i] = "B-"+goldTag;
					else
						subGoldTags[i] = "B-"+goldTag;
				}
				else
					subGoldTags[i] = "I-"+goldTag;						
			}
			
			String[] allTags = new String[subWordNumber];
			for(int i=0; i<subWordNumber; i++)
				allTags[i] = subTags[i] +"\t" + subGoldTags[i];
						
			Instance newInstance = new Instance(subWords, allTags);
			segposWriter.write(newInstance);
			
			//下一个实例
			goldInstance = goldReader.getNext();
			tagInstance = tagReader.getNext();
			instances.clear();
			for(int i=0; i<fileNumber; i++) {
				Instance instance = readers.get(i).getNextRaw();
				if(instance != null)
					instances.add(instance);
			}			
		}	
		
		segposWriter.finishWriting();
		System.out.println(count);
		
	}
	
	
	/**
	 * 评价所有分类器预测的子词
	 * <li> 计算各个分类器预测相同的子词的正确性，即是否在gold中 
	 * <li> 计算所有分类器子词集合的oracle
	 * <li> 原来词的平均长度，子词的平均长度
	 * @since 2013-5-2
	 * @param goldFileName
	 * @param predictFileNames
	 * @throws IOException 
	 */
	public void evalSubWords(String goldFileName, ArrayList<String> predictFileNames) throws IOException {
		
		Options options = new Options();
		//读取所有的实例
		//gold reader
		Reader goldReader = new Reader();
		goldReader.startReading(goldFileName, options);
		Instance goldInstance = goldReader.getNext();
		//other readers
		ArrayList<Reader> readers = new ArrayList<Reader>();
		ArrayList<Instance> instances = new ArrayList<Instance>();
		int fileNumber = predictFileNames.size();
		for(int i=0; i<fileNumber; i++) {
			Reader segposReader = new Reader();
			segposReader.startReading(predictFileNames.get(i), options);
			readers.add(segposReader);
			Instance instance = readers.get(i).getNextRaw();
			if(instance != null)
				instances.add(instance);			
		}
		
		//预测集中相同的子词，gold和预测集中相同的子词
		int sameSubWordTotalSize = 0;
		int goldSameSubWordTotalSize = 0;
		//统计词长
		int charTotalSize = 0;
		int goldWordTotalSize = 0;
		int subWordTotalSize = 0;
		//
		int goldInSubWordTotalSize = 0;
		int subwordInGoldTotalSize = 0;
		
		int count = 1;
		//如果每个实例都存在
		while(goldInstance != null && instances.size() == fileNumber) {
			
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count % 1000 == 0)
					System.out.println();
			}
			//System.out.println(count);
			count++;
			//golden words
			Object[] goldObjs = getPositions(goldInstance);
			ArrayList<Integer> goldPositions = (ArrayList<Integer>) goldObjs[0];
			String[] goldChars = (String[]) goldObjs[1];
			
			//predicted sub-words
			Object[] firstObjs = getPositions(instances.get(0));
			ArrayList<Integer> firstPositions = (ArrayList<Integer>) firstObjs[0];
			ArrayList<String> subWords = new ArrayList<String>();
			for(int i=0; i<firstPositions.size()-1; i++)
				subWords.add(firstPositions.get(i)+"-"+firstPositions.get(i+1));
			
			//other sub-words, 去找到相同的子词			
			ArrayList<ArrayList<Integer>> allPositions = new ArrayList<ArrayList<Integer>>();
			for(int i=1; i<fileNumber; i++) {
				Object[] objs = getPositions(instances.get(i));
				ArrayList<Integer> positions = (ArrayList<Integer>) objs[0]; 
				allPositions.add(positions);
			}
			
			ArrayList<String> sameSubWords = new ArrayList<String>();
			for(String subWord : subWords) {
				String[] startEnd = subWord.split("-");
				int start = Integer.parseInt(startEnd[0]);
				int end = Integer.parseInt(startEnd[1]);
				boolean same = true;
				for(int i=1; i<fileNumber; i++) {
					ArrayList<Integer> positions = allPositions.get(i-1);
					if(!positions.contains(start) || !positions.contains(end)
							|| positions.indexOf(start)+1 != positions.indexOf(end)) {
						same = false;
						break;
					}						
				}
				if(same)
					sameSubWords.add(subWord);
			}			
			int sameSubWordsSize = sameSubWords.size();
			
			//检测是否在gold中
			ArrayList<String> goldSameSubWords = new ArrayList<String>();
			for(String subWord : sameSubWords) {
				String[] startEnd = subWord.split("-");
				int start = Integer.parseInt(startEnd[0]);
				int end = Integer.parseInt(startEnd[1]);
				boolean same = true;
				if(!goldPositions.contains(start) || !goldPositions.contains(end)
						|| goldPositions.indexOf(start)+1 != goldPositions.indexOf(end)) {
					same = false;
				}			
				if(same)
					goldSameSubWords.add(subWord);
			}	
			int goldSameSubWordsSize = goldSameSubWords.size();
			
			sameSubWordTotalSize += sameSubWordsSize;
			goldSameSubWordTotalSize += goldSameSubWordsSize;
			
			
			//计算sub words的oracle. 我们计算为gold words是否被sub words覆盖
			ArrayList<Integer> predictPositions = getAllPositions(instances);
			for(int i=0; i<goldPositions.size()-1; i++) {
				int start = goldPositions.get(i);
				int end = goldPositions.get(i+1);				
				if(predictPositions.contains(start) && predictPositions.contains(end)) {
					goldInSubWordTotalSize += 1;
					subwordInGoldTotalSize += predictPositions.indexOf(end)-predictPositions.indexOf(start);
				}
			}			
			
			//计算词长
			int charSize = goldChars.length;
			int goldWordSize = goldPositions.size()-1;
			instances.add(goldInstance);
			ArrayList<Integer> allPosition = getAllPositions(instances);
			int subWordSize = allPosition.size()-1;
			
			charTotalSize += charSize;
			goldWordTotalSize += goldWordSize;
			subWordTotalSize += subWordSize;
		
			
			
			//下一个实例
			goldInstance = goldReader.getNext();
			instances.clear();
			for(int i=0; i<fileNumber; i++) {
				Instance instance = readers.get(i).getNextRaw();
				if(instance != null)
					instances.add(instance);
			}			
		}	
		
		System.out.println("same gold     : " + goldSameSubWordTotalSize);
		System.out.println("same          : " + sameSubWordTotalSize);
		System.out.println("same gold/same: " + goldSameSubWordTotalSize/(double)sameSubWordTotalSize);

		System.out.println("goldInSubWordTotalSize: "+goldInSubWordTotalSize + ", goldInSubWordTotalSize/goldWordTotalSize: "+ goldInSubWordTotalSize/(double)goldWordTotalSize);
		System.out.println("subwordInGoldTotalSize: "+subwordInGoldTotalSize + ", subwordInGoldTotalSize/subWordTotalSize: " + subwordInGoldTotalSize/(double)subWordTotalSize);
		
		System.out.println("charTotalSize : " + charTotalSize);
		System.out.println("goldWordTotalSize: " + goldWordTotalSize + ", goldWordTotalSizecharTotalSize： " + charTotalSize/(double)goldWordTotalSize);
		System.out.println("subWordTotalSize: " + subWordTotalSize +", subWordTotalSize/charTotalSize： " + charTotalSize/(double)subWordTotalSize);
	
	}
	
	
	
	/**
	 * 获得当前实例中词的起始位置
	 * @since 2013-5-2
	 * @param instance
	 * @return
	 */
	public Object[] getPositions(Instance instance) {
		
		ArrayList<Integer> positions = new ArrayList<Integer>(); //词的起始和结束位置
		//gold instance
		String[] words = instance.words;
		int length=0;
		positions.add(length);
		//找到正确词的起始和终止位置,保存三个结构：goldPositions, allPositionSet，goldCharList
		for(String word : words) {
			String[] wordChars = Reader.getCharsEntire(word);
			length += wordChars.length;
			positions.add(length);
		}
		String[] chars = new String[length];
		int index=0;
		for(String word : words) {
			String[] wordChars = Reader.getCharsEntire(word);
			for(String wordChar : wordChars)
				chars[index++] = wordChar;
		}
		
		Object[] objs = new Object[2];
		objs[0] = positions;
		objs[1] = chars;
		return objs;
	}
	
	
	/**
	 * 获得所有分词的起始和终止位置
	 * @since 2013-5-2
	 * @param goldInstance
	 * @param tagInstance
	 * @param instances
	 * @return
	 */
	public ArrayList<Integer> getAllPositions(ArrayList<Instance> instances) {
		
		if(instances == null || instances.size() == 0) {
			System.out.println("instances are wrong");
			System.exit(-1);
		}
		//读取所有的词性和分词的位置
		TreeSet<Integer> allPositionSet = new TreeSet<Integer>();    //所有词的起始和结束位置
		
		Instance firstInstance = instances.get(0);
		Object[] objs = getPositions(firstInstance);
		ArrayList<Integer> positions = (ArrayList<Integer>) objs[0];
		int goldLastPosition = positions.get(positions.size()-1);
		allPositionSet.addAll(positions);
		
		int fileNumber = instances.size();
		//other results		
		for(int i=1; i<fileNumber; i++) {                             //在保存其他词的位置
			Instance instance = instances.get(i);
			objs = getPositions(instance);
			positions = (ArrayList<Integer>) objs[0];
			int lastPosition = positions.get(positions.size()-1);
			if(lastPosition != goldLastPosition) {
				System.out.println("char length is different: gold=" + goldLastPosition + ", tag=" + lastPosition);
				System.exit(-1);
			}
			allPositionSet.addAll(positions);
		}		
		
		//将所有的可能的词的位置，包括tagFile, otherFiles
		ArrayList<Integer> allPositions = new ArrayList<Integer>();			
		Iterator<Integer> iter = allPositionSet.iterator();
		while(iter.hasNext()) {
			int position = iter.next();
			allPositions.add(position);
		}
		
		return allPositions;
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		
		String goldFile = "/disk1/subWordSegPos/subWords/subfiles/ctb5-train-parse-wp";
		
		String wordCharResult = "/disk1/subWordSegPos/subWords/subfiles/train-wp-0-test-charresult";
		String wordWordResult = "/disk1/subWordSegPos/subWords/subfiles/train-wp-0-test-wordresult";
		String wordposCharResult = "/disk1/subWordSegPos/subWords/subfiles/train-wp-test-wp-charesult";
		String wordposWordResult = "/disk1/subWordSegPos/subWords/subfiles/train-wp-test-wp-wordresult";
		String wordCrfResult = "/disk1/subWordSegPos/subWords/subfiles/train-wp-test-crf.result.w";
		String wordposCrfResult = "";
		
		ArrayList<String> trainFiles = new ArrayList<String>();
		trainFiles.add(wordposCharResult);
		trainFiles.add(wordCrfResult);
		
		SubWordComb comb = new SubWordComb();
		String trainCharSubWordFile = "/disk1/subWordSegPos/subWords/trainSubWordFile";
		comb.generateSubWords(goldFile, wordposWordResult, trainFiles, trainCharSubWordFile);
			
		String develGold = "/disk1/subWordSegPos/subWords/subfiles/ctb5-dev-parse-wp";
		
		String develwordCharResult = "/disk1/subWordSegPos/subWords/subfiles/dev-charresult-8";
		String develwordWordResult = "/disk1/subWordSegPos/subWords/subfiles/dev-wordresult-6";
		String develwordposCharResult = "/disk1/subWordSegPos/subWords/subfiles/dev-wp-charresult-8";
		String develwordposWordResult = "/disk1/subWordSegPos/subWords/subfiles/ctb5-dev-parse-wp.result-5";
		String develwordCrfResult = "/disk1/subWordSegPos/subWords/subfiles/ctb5-dev-parse-wp-crf.result.w";
		
		ArrayList<String> devFiles = new ArrayList<String>();
		devFiles.add(develwordposCharResult);
		devFiles.add(develwordCrfResult);
		
		String devWordSubWordFile = "/disk1/subWordSegPos/subWords/devSubWordFile";
		comb.generateSubWords(develwordposWordResult, develwordposWordResult, devFiles, devWordSubWordFile);
	
		devFiles.add(develwordposWordResult);
		//comb.evalSubWords(develGold, devFiles);
		
		
	}
	
	
	
	
	
	
	
	
}
