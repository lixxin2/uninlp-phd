package edu.hitsz.nlp.struct;

import java.util.ArrayList;

import edu.hitsz.java.file.local.FileTree;

public class Penn2Malt {

	
	static String jarFile = "/media/study/tools/nlp/Parser/Conversion/Penn2Malt_MaltConverter/Penn2Malt.jar";
	static String headFile = "/media/study/tools/nlp/Parser/Conversion/headrules/zhang-chn_headrules.txt";
	
	/**
	 * 转换chinese treebank到dependency structure
	 * @since Sep 21, 2012
	 * @param fileName
	 */
	public static void convertCntbFile(String fileName) {
		
		String cmd = "java -jar " + jarFile + " " + fileName + " " + headFile + " 3 2 cntb";
		System.out.println(cmd);
		
		try {
            Runtime run = Runtime.getRuntime();
            Process p = run.exec(cmd);
            //检查命令是否执行失败。
            try {
                if (p.waitFor()!=0) {
                    if(p.exitValue()==1)//p.exitValue()==0表示正常结束，1：非正常结束
                        System.err.println("命令执行失败!");
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
	
	public static void convertCntbDir(String dirName) {
		FileTree tree = new FileTree();
		tree.generateFrom(dirName);
		ArrayList<String> fileNames = tree.getFileNames();
		
		for(String fileName : fileNames) {
			convertCntbFile(fileName);			
		}		
	}
	
	
	public static void main(String[] args) {
		//Penn2Malt.convertCntbDir("/media/study/corpora/treebank/ctb_7/data/utf-8/bracketed-clean-GBK");
		Penn2Malt.convertCntbDir("/media/study/corpora/treebank/ctb_7/data/utf-8/bracketed-clean-GBK");
		
	}
	
	
	
	
}
