package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import edu.hitsz.nlp.mst.Util;

public class PyCharWriter {
	
	private BufferedWriter writer;
	
	public PyCharWriter() {
		
	}
	
	public PyCharWriter(String file){
		startWriting(file);
	}
	
	public void startWriting (String file) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		}
		catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeInstance(PyCharInstance instance) {
		try {
			writer.write(join(instance.wordYins, " ") + "\n");
			writer.write(join(instance.words) + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeInstanceWords(PyCharInstance instance) {
		try {
			writer.write(join(instance.words) + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeInstanceColumn(PyCharInstance instance) {
		try {
			String[] characterYins = instance.characterYins;
			String[] characters = instance.characters;
			for(int i=0; i<instance.length; i++)
				writer.write(characterYins[i]+"\t"+characters[i]+"\n");
			writer.write("\n");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeInstanceMert(PyCharInstance instance, int seq, ArrayList<Double> weights) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(seq); sb.append(" ||| ");
			String[] words = instance.words;
			for(int i=0; i<words.length; i++)
				sb.append(words[i] + " ");
			sb.append("|||");
			for(int i=0; i<weights.size(); i++)
				sb.append(" " + weights.get(i));
			sb.append("\n");
			writer.write(sb.toString());
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeInstanceFrom(PyCharInstance instance) {
		try {
			writer.write(join(instance.characters) + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void finishWriting () {
		try {
			//writer.flush();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
    /**
     * 用一个字符连接字符串数组，返回一个字符串
     * @param a
     * @param sep
     * @return
     */
    public static String join (String[] a, String sep) {
		StringBuffer sb = new StringBuffer();
		sb.append(a[0]);
		for (int i=1; i<a.length; i++)
		    sb.append(sep).append(a[i]);
		return sb.toString();
    }
    
    public static String join (String[] a) {
		StringBuffer sb = new StringBuffer();
		sb.append(a[0]);
		for (int i=1; i<a.length; i++)
		    sb.append(a[i]);
		return sb.toString();
    }
	
}
