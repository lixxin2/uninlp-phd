package edu.hitsz.java.file.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyExample {
	

    

    
    
    /** 
     * 测试RandomAccessFile
     * <p> andomAccessFile的工作方式是，把DataInputStream和DataOutputStream粘起来，
     * 再加上它自己的一些方法，
     * 比如定位用的getFilePointer( )，在文件里移动用的seek( )，以及判断文件大小的length( )。
     * @since Jun 19, 2013
     * @param inFileName
     * @param outFileName
     */
    public static void randomAccessFileCopy(String inFileName, String outFileName) {
    	
    	try {
	    	RandomAccessFile read = new RandomAccessFile(inFileName,"r");   
	        RandomAccessFile writer = new RandomAccessFile(outFileName,"rw");   
	        byte[] b = new byte[4096];
	        int count;
	        while((count=read.read(b))!=-1){  
	        	
	                if(count!=b.length)
	                {
	                        byte[] t=new byte[4096];
	                        for(int i=0;i<count;++i)
	                                t=b;
	                        writer.write(t);
	                }
	                else writer.write(b);   
	        }   
	        writer.close();   
	        read.close();
    	}
    	catch (IOException e) {
    		
    	}	        
    	
    }
    
    /** 测试FileInputStream，FileOutputSteam
     * 这是一个最基本的读写文件的方式，这种读写一般是读取一个字节，或者定义长度的字节个数，
     * 如果为了节约时间的话，建议用缓冲区读写，这样速度会快一点。
     * @since Jun 19, 2013
     * @param inFileName
     * @param outFileName
     */
    public static void fileStreamCopy(String inFileName, String outFileName) {
    	
    	try {
    		
    		InputStream in = new BufferedInputStream(
    				new FileInputStream(new File(inFileName)));
    		OutputStream out = new BufferedOutputStream(
    				new FileOutputStream(new File(outFileName)));
    		
	        int count;
	        byte[] b=new byte[4096];
	        while((count=in.read(b))!=-1)
	        {
	                if(count!=b.length)
	        {
	                byte[] t=new byte[4096];
	                for(int i=0;i<count;++i)
	                        t=b;
	                out.write(t); //out.write(b, 0, count);
	        }
	                else out.write(b);
	        }
	        in.close();
	        out.flush();
	        out.close();
    	}
    	catch (IOException e) {
    		
    	}
    }
    
    /**  
     * 测试BufferedReader，BufferedWriter 
     * <p> 读行操作readLine()
     * <p> bw.writer(line); bw.write("\n); 比 bw.write(line+"\n"); 快很多
     * @since Oct 21, 2013
     * @param inFileName
     * @param outFileName
     */
    public static void bufferedCopy(String inFileName, String outFileName) {
    	
    	try {
    		
	    	BufferedReader br = new BufferedReader(new FileReader(inFileName));
	        BufferedWriter bw=new BufferedWriter(new FileWriter(outFileName));
	        String line = null;
	        
	        while((line = br.readLine())!=null)
	        {
	                bw.write(line);
	                bw.write("\n");
	        }
	        
	        bw.flush();
	        bw.close();
	        br.close();
    	}
    	catch (IOException e) {
    		
    	}    	
    }
    
    
    
    public static void fileChannelCopy(String inFileName, String outFileName) {
    	
    	try {
    		
    		FileChannel read = new RandomAccessFile(inFileName,"r").getChannel();   
            FileChannel writer = new RandomAccessFile(outFileName,"rw").getChannel();   
            long i = 0;   
            long size = read.size()/30;
            //System.out.println(read.size());
            ByteBuffer bb,cc = null;   
            while(i < read.size() && (read.size()-i) > size)
            {   
                bb = read.map(FileChannel.MapMode.READ_ONLY, i, size);   
                cc = writer.map(FileChannel.MapMode.READ_WRITE, i, size);   
                cc.put(bb);   
                i+=size;   
                bb.clear();   
                cc.clear();   
            }   
            bb = read.map(FileChannel.MapMode.READ_ONLY, i, read.size()-i);
            cc = writer.map(FileChannel.MapMode.READ_WRITE, i, read.size()-i);
            cc.put(bb);   
            bb.clear();   
            cc.clear();   
            read.close();   
            writer.close();
    	}
    	catch (IOException e) {
    		
    	}
    }
    

    /** 单线程复制文件 */
    public static void nioTransferCopy(String source, String target) {  
    	
        FileChannel in = null;  
        FileChannel out = null;  
        FileInputStream inStream = null;  
        FileOutputStream outStream = null;  
        
        try {
            inStream = new FileInputStream(new File(source));  
            outStream = new FileOutputStream(new File(target));  
            in = inStream.getChannel();  
            out = outStream.getChannel();  
            in.transferTo(0, in.size(), out);  

        	inStream.close();
        	in.close();
        	outStream.close();
        	out.close();
        } 
        catch (IOException e) {  
            e.printStackTrace();  
        } 
        
    }
    
 
    

    
    
    public static void speedTest() {
    	long startTime = System.currentTimeMillis();
    	String inFile = "/home/tm/Downloads/gmw.ictclas.seg"; //394MB text file
    	String outFile = "/home/tm/Documents/gmw.ictclas.seg";
    	//bufferedCopy(inFile, outFile);                            //7000
    	//randomAccessFileCopy(inFile, outFile);                    //1066
    	//fileStreamCopy(inFile, outFile);                          //3893
    	//fileChannelCopy(inFile, outFile);                         //1649
    	nioTransferCopy(inFile, outFile);							//4722
    	long endTime = System.currentTimeMillis();                  
    	System.out.println(endTime - startTime);
    }
    
    
    public static void main(String[] args) {
    	
    	speedTest();
    	
    	
    	
    }
    
    

}
