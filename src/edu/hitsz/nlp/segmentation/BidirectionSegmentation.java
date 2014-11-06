package edu.hitsz.nlp.segmentation;

//A java version of Joy's CMU-LDC Segmenter
//Version: 2.0 (re-implementation of Joy's CMU-LDC Segmenter, adding multi-thread support) 
//Date: Feb. 02, 2009

//Original copyright notes
/*
# $Rev: 5018 $
# $LastChangedDate: 2008-02-23 17:44:00 -0500 (Sat, 23 Feb 2008) $
#
# This program is a perl version of left-right mandarin segmentor
# As LDC segmenter takes a long time to build the DB files which makes the
# the training process last too long time.
#
# For ablation experiments, we do not need to create the DB files because the
# specific frequency dictionary will be used only once for each slice.
#
# The algorithm for this segmenter is to search the longest word at each point
# from both left and right directions, and choose the one with higher frequency 
# product.
#
# By Joy, joy@cs.cmu.edu
# July 4th, 2001
#
# Modified by Joy, Feb 23, 2008
#	After comparing with the C implementation, problem found that using frequency 1 for single characters in the sentence  
#	gives slightly worse results compared to using the frequency from the freqList
#
# Usage:
#
# Perl lrSegmenter.perl [frequencyDictionary] < mandarinFile > segmentedFiles
#
#	if no parameter is given (no frequency dictionary file specified), there should
#	be one called "Mandarin.fre.complete" as default
#
#the mandarinFile is encoded in GB
#
# The format of the dictionary file is this:
# for each line:
# "Frequency\tchineseWord\n"
*/

import java.io.*;
import java.util.*;
import java.lang.*;

public class BidirectionSegmentation {
	private	HashMap<String, Integer> wordFreqList = new HashMap<String, Integer>();
	private	HashMap<Character, Integer> longestWordListStart = new HashMap<Character, Integer>();
	
	private BidirectionSegmentation() {
	}
	
	private static BidirectionSegmentation instance = null;
	
	public static synchronized BidirectionSegmentation getInstance(String dicPath) {
		if (instance == null) {
			instance = new BidirectionSegmentation();
			instance.readDictionary(dicPath);			
		}
		return instance;
	}

	private void readDictionary (String dictFile){
	
		try{
			//open the dictionary file
			BufferedReader reader = new BufferedReader (new InputStreamReader(
					new FileInputStream(dictFile),"GBK"));
			
			//process each line of the dictionary
			String line;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				
				//delete the \n and the \r
				line = line.replaceAll("[\r\n]", "");
				//replace the space with tab
				line = line.replaceAll(" ","\t");
				
				//split the sentence by "\t" into an array of strings (entries)
				String[] entries = line.split("\t");
				
				//debug
				if(entries.length != 2) {
				//	System.err.println("entries.length!= 2" + line);
					continue;
				}
				String thisChnWord = entries[1];
				Integer thisFreq = Integer.valueOf(entries[0]);
				
				//fill the hashtable
				wordFreqList.put(thisChnWord, thisFreq);

				//if thisChnWord is just an ASCII, neglect it
				if(containASCII(thisChnWord)){
				}
				else{
					Character headChar = thisChnWord.charAt(0);
					
					//the length of the Chinese word in character
					int thisLen = thisChnWord.length();
					
					if((longestWordListStart.get(headChar) == null) || 
					(longestWordListStart.get(headChar) < thisLen)){
						longestWordListStart.put(headChar, thisLen);
					}
				}		
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("An IOException has occured");
			System.exit(1);
		}
	}
	
	
	private static boolean containASCII (String string){
		
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if ((c > 0) && (c < 0x7F)) {
				return true;
			}
		}
		return false;
	}
	
	private String segmentACnString(String inputString) {	
		int lenOfString = inputString.length();
		int arcTable[][] = new int[lenOfString][];
		for (int i = 0; i < lenOfString; i++) {
			arcTable[i] = new int[lenOfString];
		}
		
		//step0: initialize the arcTable
		for (int i = 0; i < lenOfString; i++) {
			for (int j = 0; j < lenOfString; j++) {
				if (i == j) {
					String currentChar = inputString.substring(i, i+1);
					if (wordFreqList.get(currentChar) != null) {
						arcTable[i][j] = wordFreqList.get(currentChar);
					}
					else {
						arcTable[i][j] = 1;
					}
				}
				else {
					arcTable[i][j] = -1;
				}
			}
		}
		
		//step1: search for all possible arcs in the input string and 
		//       create an array for them
		for (int currentPos = 0; currentPos < lenOfString; currentPos++) {
			char currentChar = inputString.charAt(currentPos);
		
			int possibleLen = 0;	
			//from this position, try to find all possible words led by this character
			if(longestWordListStart.get(currentChar) == null) {
	        //			System.err.println("no such headWord in dict:" + currentChar);
			}
			else {
				possibleLen = longestWordListStart.get(currentChar);
			}
			
			if( (possibleLen + currentPos) > (lenOfString -1)) {
				possibleLen = lenOfString - currentPos;
			}
			
			while (possibleLen >=1 ) {
				String subString = inputString.substring(currentPos, currentPos + possibleLen);
				if(wordFreqList.get(subString) != null) {
					arcTable[currentPos][currentPos+possibleLen-1] = wordFreqList.get(subString);
				}
				possibleLen --;
			}
		}
		
		//step2: from the arc table, try to find the best path as segmentation at each point use the longest arc
		//Try from two directions for the search: left to right and right to left
		//Using the one with higher product of frequency of the arcs
		
		int[] leftRightSegLabel = new int[lenOfString];
		int[] rightLeftSegLabel = new int[lenOfString];
		
		//from left to right
		double leftToRightFreq = 0;
		
		int thisCharIndex = 0;
		int charIndexEnd = lenOfString - 1;
		
		while (thisCharIndex < lenOfString) {
			int endCharIndex = charIndexEnd;
			
			boolean found = false;
			while((!found) && (endCharIndex >= thisCharIndex)) {
				if(arcTable[thisCharIndex][endCharIndex] != -1) {
					leftToRightFreq += Math.log(arcTable[thisCharIndex][endCharIndex]);
					found = true;
				}
				else {
					endCharIndex --;
				}
			}
			leftRightSegLabel[endCharIndex] = 1;
			thisCharIndex = endCharIndex + 1;
		}
		
		//from right to left
		double rightToLeftFreq = 0;
		thisCharIndex = lenOfString - 1;
		
		while(thisCharIndex >= 0) {
			int startCharIndex = 0;
			
			boolean found = false;
			while ((!found) && (startCharIndex <= thisCharIndex)) {
				if(arcTable[startCharIndex][thisCharIndex] != -1) {
					found = true;
					rightToLeftFreq += Math.log(arcTable[startCharIndex][thisCharIndex]);
				}
				else {
					startCharIndex++;
				}
			}
			
			
			rightLeftSegLabel[startCharIndex] = 1;
			thisCharIndex = startCharIndex - 1;
		}
		
		//Step3: create result
		String result = "";
		if(leftToRightFreq > rightToLeftFreq) {
			for(int p = 0; p < lenOfString; p++) {
				result = result + inputString.substring(p, p+1);
				
				if(leftRightSegLabel[p] == 1) {
					result = result + " ";
				}
			}
		}
		else {
			for(int p = 0; p < lenOfString; p++) {
				if(rightLeftSegLabel[p] == 1) {
					result = result + " ";
				}
				result = result + inputString.substring(p, p+1);
			}
		}
		
		result = result.trim();
		result = " "+result+" ";
		return result;
	}
	
	public String segmentAString(String str)
	{
		//delete \n \r
		String thisSent = str.replaceAll("[\r\n]", "");
		
		String finalResult = "";
		int sentLen = thisSent.length();
		
		String partialChnString="";
		int index = 0;
		
		while(index < sentLen) {
			String thisChar = thisSent.substring(index, index+1);
			
			if(thisChar.charAt(0) >= 0x80) {
				thisChar = thisSent.substring(index, index+1);
				index += 1;
				
				partialChnString = partialChnString + thisChar;
			}
			else {
				index ++;
				if (partialChnString.length() != 0) {
					String partialSegString = segmentACnString(partialChnString);
					finalResult = finalResult + partialSegString;
					
					partialChnString = "";
					partialSegString = "";
				}
				
				finalResult = finalResult + thisChar;
			}
		}
			
			//in case of pure Chinese characters
			if(partialChnString.length() != 0) {
				String partialSegString = segmentACnString(partialChnString);
				finalResult = finalResult + partialSegString;
				
				partialChnString = "";
				partialSegString = "";
			}
			
			finalResult = finalResult.trim();
			finalResult = finalResult.replaceAll("[ ]+", " ");
		
			return finalResult;
	}
	
	public static void main(String args[]) throws IOException {
		if(args.length != 2) {
			System.err.println("Format: Segmenter dictionary inputFile\n");
			System.exit(1);
		}
		//Command line: Segmenter dict inputFile
		BidirectionSegmentation segmenter = BidirectionSegmentation.getInstance(args[0]);
		
		BufferedReader reader = new BufferedReader (new InputStreamReader(
				new FileInputStream(args[1]), "GBK"));
		
		//process each line of the dictionary
		String line;
		
		while ((line = reader.readLine()) != null) {
			String seg = segmenter.segmentAString(line);
			System.out.println(seg);
		}	
	}
	
}

