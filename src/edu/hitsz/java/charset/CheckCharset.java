package edu.hitsz.java.charset;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Set;

public class CheckCharset {

	public static void main(String[] args) {
		
		Set<String> charsetNames = Charset.availableCharsets().keySet();
		String[] charsetss = new String[charsetNames.size()];
		String[] charsets = charsetNames.toArray(charsetss);
		for(String charset : charsets)
			System.out.println(charset);		
		
		System.out.println(charsetNames.contains("utf-8"));  
		System.out.println(charsetNames.contains("utf-16"));  
		System.out.println(charsetNames.contains("gb2312"));  
		  
		System.out.println(Charset.isSupported("utf-8")); 
		
		System.out.println(Charset.defaultCharset());  
		
		byte[] bytes = "啊".getBytes();
		byte[] bytesDf = "啊".getBytes(Charset.defaultCharset());
		String s = new String("啊".getBytes());
		try {
			byte[] bytesDf2 = "啊".getBytes("gb2312");
			String sGB = new String("啊".getBytes(), "GB18030");

			System.out.println();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String ling = "\u3007";
		System.out.println(ling);
	}
	
}
