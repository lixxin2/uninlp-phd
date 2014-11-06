package edu.hitsz.nlp.asr.rerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.pinyin2character.PyCharInstance;

/**
 * Julius结果文件的读取器，读取结果的拼音序列，词序列，声学模型和语言模型给每个候选项的赋值
 * @author Xinxin Li
 * @since Sep 21, 2013
 */
public class JuliusResultReader {
	
	public BufferedReader reader;
	public SyllableMap sylMap;
	
	public void startReading(String file) {
		try {
			if(!new File(file).exists()) {
				System.out.println("file: " + file + " doesn't exist");
				System.exit(-1);
			}
			String fileEncoding = FileEncoding.getCharset(file);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileEncoding));
		}
		catch (IOException e) {
			System.out.println(e.getStackTrace());			
		}
	}
	
	public void loadSyllableMap(String fileName) {
		sylMap = new SyllableMap();
		sylMap.readFrom(fileName);
	}
	
	
	/** 
	 * 读取文件，抽取中结果中的音和字
	 * @since Sep 21, 2013
	 * @return
	 * @throws IOException
	 */
	public ArrayList<PyCharInstance> getJuliusResults() throws IOException {
		
		ArrayList<PyCharInstance> items = new ArrayList<PyCharInstance>();
		String sentStart = "sentence";
		String line = null;
		while((line = reader.readLine()) != null
				&& !line.startsWith(sentStart));
		//System.out.println("");
		if(line == null) {
			reader.close();
			return null;
		}
		
		int cnt = 0;		
		while(line != null && line.trim().length() > 0 && line.startsWith(sentStart)) {
			//System.out.println(cnt);
			String sentencen = line;
			String wseqn = reader.readLine();
			String phseqn = reader.readLine();
			String cmscoren = reader.readLine();
			String scoren = reader.readLine();
			//System.out.println(phseqn);
			JuliusResultInstance item = JuliusResultInstance.getInstance(sentencen, wseqn,
					phseqn, cmscoren, scoren, sylMap);
			if(item != null)
				items.add(item);
			line = reader.readLine();
			cnt++;
		}
		if(items.size() == 0) {
			items.add(JuliusResultInstance.emptyInstance());
		}
		return items;				
		
	}
	
}
