package edu.hitsz.nlp.lm.nnlm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.language.chinese.ChineseWord;
import edu.hitsz.nlp.segpos.CharPipe;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;

/**
 * 预处理文件，使其支持word2vec 
 * @author Xinxin Li
 * @since Nov 28, 2013
 */
public class Word2VecPre {

	/**
	 * 将文件按字分割
	 * @since Nov 28, 2013
	 * @param inputFileName
	 * @param segEndPunct 是否根据结束标点来分割句子，默认为true
	 * @param keepPunct 是否保留标点符号，默认为true
	 * @param keepNoChinese 是否保留非中文字符，默认为true
	 * @param outFileName
	 * @throws IOException 
	 */
	public static void segCharacter(String inFileName,
			boolean segEndPunct,
			boolean keepPunct,
			boolean keepNoChinese,
			String outFileName) throws IOException {
		
		//
		HashMap<String, Integer> puncts = new HashMap<String, Integer>();
		for(String punct : CTB.punctuations)
			puncts.put(punct, 1);
		
		String endPunct = "[。？！，；：]";
		
		//
		String fileEncoding = FileEncoding.getCharset(inFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
		BufferedWriter writer=new BufferedWriter(new FileWriter(outFileName));
		
		//
        String line = null;
        int count = 0;
        while((line = reader.readLine()) != null) {
        	
        	if(count % 10000 == 0) {
        		System.out.print(count + "...");
        		if(count % 100000 == 0)
        			System.out.println();
        	}
        	count++;
        	
        	line = line.trim();
        	int lineLength = line.length();
        	if(lineLength > 0) {
        		// 是否分割句子
        		String[] subLines = new String[1];
        		if(segEndPunct) {
        			subLines = line.split(endPunct);
        		}
        		else
        			subLines[0] = line;
        			
        		//对于每个句子
        		for(int i=0; i<subLines.length; i++) {
        			String subLine = subLines[i];
        			int length = subLine.length();
        			StringBuffer strbuf = new StringBuffer();
        			for(int j=0; j<length; j++) {
        				String character = subLine.substring(j,j+1);
        				boolean isPunct = puncts.containsKey(character);
        				boolean isChinese = character.matches(ChineseWord.hanziRegex);
        				//如果不保存标点，并且该字为标点
        				if(!keepPunct && isPunct)
        					continue;
        				//如果不保存非中文，并且该字不是中文
        				if(!keepNoChinese && !isChinese)
        					continue;
        				strbuf.append(character + " ");
        			}	
        			writer.write(strbuf.toString().trim() + "\n");
        			
        		}//每个短句子
        	}//长句子存在
        }//每个长句子
        writer.flush();
        writer.close();
        reader.close();		
	}
	
	

	public static void main(String[] args) throws IOException {
		
		String inFileName = "/home/tm/disk/disk1/word2vec/data.Sogou.raw";
		String outFileName = "/home/tm/disk/disk1/word2vec/data.Sogou.character";
		Word2VecPre.segCharacter(inFileName, false, true, true, outFileName);
		
	}
	
}
