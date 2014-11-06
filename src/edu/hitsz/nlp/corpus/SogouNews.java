package edu.hitsz.nlp.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.sentence.SimpleChineseSegmentation;

/**
 * 处理sogou语料
 * @author Xinxin Li
 * @since Aug 14, 2012
 */
public class SogouNews {

	public static void extractText(String inFileDir, String outFileDir) throws IOException {
		
		FileTree ft = new FileTree(inFileDir);
		ArrayList<String> fileNames = ft.getFileNames();
		
		SimpleChineseSegmentation csc = new SimpleChineseSegmentation();
		
		int count = 0;
		for(String fileName : fileNames) {
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			System.out.println(fileName);
			
			FileWriter newWriter = new FileWriter(outFileDir+"/"+new File(fileName).getName());
			
			String preContext = "";
			
			String line = null;
			while((line = newReader.readLine()) != null){
				if(line.length() > 0) {
					if(line.startsWith("<content>") 
							&& !line.equals("<content></content>")) {
							int length = line.length();
							String context = line.substring(9, length-10);
							if(context.equals(preContext))
								continue;
							preContext = context;
							newWriter.write(csc.getSeg(context)+"\n");												
					}
					else if (line.startsWith("<contenttitle>")
							&& !line.equals("<contenttitle></contenttitle>")) {
							int length = line.length();
							newWriter.write(csc.getSeg(line.substring(14, length-15))+"\n");	
					}					
				}				
			}
			count++;
			//if(count == 1)
			//	break;
			newWriter.close();
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		
		String SogouCADir = "/media/study/corpora/sogou/SogouCA";
		String SogouCSDir = "/media/study/corpora/sogou/SogouCS";
		
		String SogouCAFile = "/media/study/corpora/sogou/SogouCARaw";
		String SogouCSFile = "/media/study/corpora/sogou/SogouCSRaw";
		
		SogouNews.extractText(SogouCADir, SogouCAFile);
		SogouNews.extractText(SogouCSDir, SogouCSFile);
		
		
	}
	
	
	
}
