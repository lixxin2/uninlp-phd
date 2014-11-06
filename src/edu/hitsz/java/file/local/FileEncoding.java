package edu.hitsz.java.file.local;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



/**
 * 判断文本文件编码
 * ANSI：　　　　　　　　无格式定义；
 * Unicode： 　　　　　　前两个字节为FFFE；
 * Unicode big endian：　前两字节为FEFF；　 
 * UTF-8：　 　　　　　　前两字节为EFBB；
 * @author Xinxin Li
 * @since May 15, 2012
 */
public class FileEncoding
{

    /**
     * 判断文件编码类型，UTF-8，GBK等
     * @since Aug 18, 2012
     * @param fileName
     * @return
     */
    public static String getCharset(String fileName)
    {
    
        File file = new File(fileName);
        
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try
        {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
            {
                return charset;
            }
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE)
            {
                charset = "UTF-16LE";
                checked = true;
            }
            else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF)
            {
                charset = "UTF-16BE";
                checked = true;
            }
            else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF)
            {
                charset = "UTF-8";
                checked = true;
            }
            
            bis.reset();   
            if ( !checked ) {   
            //    int len = 0;   
                int loc = 0;   
  
                while ( (read = bis.read()) != -1 ) {   
                    loc++;   
                    if ( read >= 0xF0 ) break;   
                    if ( 0x80 <= read && read <= 0xBF ) // 单独出现BF以下的，也算是GBK   
                    break;   
                    if ( 0xC0 <= read && read <= 0xDF ) {   
                        read = bis.read();   
                        if ( 0x80 <= read && read <= 0xBF ) // 双字节 (0xC0 - 0xDF) (0x80   
                                                                        // - 0xBF),也可能在GB编码内   
                        continue;   
                        else break;   
                    }   
                    else if ( 0xE0 <= read && read <= 0xEF ) {// 也有可能出错，但是几率较小   
                        read = bis.read();   
                        if ( 0x80 <= read && read <= 0xBF ) {   
                            read = bis.read();   
                            if ( 0x80 <= read && read <= 0xBF ) {   
                                charset = "UTF-8";   
                                break;   
                            }   
                            else break;   
                        }   
                        else break;   
                    }   
                }   
                //System.out.println( loc + " " + Integer.toHexString( read ) );   
            }   
  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        return charset;
    }
    
    
    /**
     * 文件转码
     * @since Sep 20, 2012
     * @param inFileName
     * @param outFileName
     * @param outEncoding
     * @throws IOException
     */
    public static void convertFile(String inFileName, String outFileName, String outEncoding) throws IOException {
		String fileEncoding = FileEncoding.getCharset(inFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
        String line = null;
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFileName), outEncoding));
        while((line = reader.readLine()) != null) {
        	writer.write(line+"\n");
        }
        writer.close();
        reader.close();		
	}
    
    
    /**
     * 将文件夹中的所有文件
     * @since Sep 20, 2012
     * @param inDir
     * @param outDir
     * @param outEncoding
     * @throws IOException
     */
    public static void convertDir(String inDir, String outDir, String outEncoding) throws IOException {
    	FileTree newTree = new FileTree();
    	newTree.generateFrom(inDir);
    	ArrayList<String> fileNames = newTree.getFileNames();
    	for(String file : fileNames) {
    		String originalFileName = new File(file).getName();
    		String currentFileName = outDir + File.separator + originalFileName;
    		convertFile(file, currentFileName, outEncoding);
    	}    	
    }
    
    
    public static void test() throws IOException {
    	//String fileName = "/home/tm/disk/disk1/nermusic/test-tag";
       	//String fileEncoding = FileEncoding.getCharset(fileName);
        //BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
        //reader.close();
    	
    	//String inDirName = "/media/study/corpora/treebank/ctb_7/data/utf-8/bracketed-clean";
    	//String outDirName = "/media/study/corpora/treebank/ctb_7/data/utf-8/bracketed-clean-GBK";
    	//FileEncoding.convertDir(inDirName, outDirName, "GBK");
    	
    	String inFileName = "/media/study/corpora/treebank/ctb_7/data/utf-8/dev-dep-GBK";
    	String outFileName = "/media/study/corpora/treebank/ctb_7/data/utf-8/dev-dep-utf8";
    	FileEncoding.convertFile(inFileName, outFileName, "UTF-8");
    	
        
        
        
        
        
    }
    
    
    public static void main(String[] args) throws IOException
    {
        FileEncoding.test();
        
        
    }
}
