package edu.hitsz.nlp.segposnn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.lm.nnlm.Word2VecPre;
import edu.hitsz.nlp.segpos.CharPipe;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;

public class DatasetPrep {
	
	/**
	 * 检测segpos文件中的所有字或词都在wordvecDict中
	 * @since Nov 29, 2013
	 * @param characterFileName
	 * @param segposFileName
	 * @throws IOException 
	 */
	public static void checkMissingWords(String wordvecDictFileName, String segposFileName) throws IOException {
		
		HashMap<String, String> wordVec= new HashMap<String, String>();
		
		//read word vec
		String fileEncoding = FileEncoding.getCharset(wordvecDictFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordvecDictFileName), fileEncoding));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.trim().length() > 0) {
				String[] parts = line.trim().split("\\s+");
				wordVec.put(parts[0], parts[1]);
			}
		}		
		reader.close();
		
		//
		HashMap<String, Integer> maps = new HashMap<String, Integer>();
		Options options = new Options(new String[0]);
		CharPipe pipe = new CharPipe(options);
		Instance inst = null;
		pipe.initInputFile(segposFileName);
		int count = 0;
		while((inst = pipe.nextInstance()) != null) {
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count % 1000 == 0)
					System.out.println();
			}
			count++;
			String[] words = inst.words;
			for(String word : words) {
				if(!wordVec.containsKey(word)) {
					System.out.println(word);	
					if(maps.containsKey(word))
						maps.put(word, maps.get(word)+1);
					else
						maps.put(word, 1);
				}
			}
		}
		for(Entry<String, Integer> entry : maps.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
			
		
	}
	
	
	
	public static void convertMissingWords(
			String wordvecDictFileName,
			String inFilename, 
			String outFilename) throws IOException {
		
		//
		HashMap<String, String> wordVec= new HashMap<String, String>();		
		//read word vec
		String fileEncoding = FileEncoding.getCharset(wordvecDictFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordvecDictFileName), fileEncoding));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.trim().length() > 0) {
				String[] parts = line.trim().split("\\s+");
				wordVec.put(parts[0], parts[1]);
			}
		}		
		reader.close();
		
		//
		
		
		
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		String wordvecDictFileName = "/home/tm/disk/disk1/nnseg/cctvpdgmd.character.bin.skipgram.size100.windows5.neg0.sample3.vocab";
		String segposFileName = "/home/tm/disk/disk1/nnseg/data/ctb5-test-w-char";
		DatasetPrep.checkMissingWords(wordvecDictFileName, segposFileName);
		
	}
	

}
