package edu.hitsz.nlp.segpos;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Writer {
	
	protected BufferedWriter writer;
	private String outputFileType;
	private boolean writeReverse;
	
	public void startWriting (String file, Options options) throws IOException {
		 writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		 outputFileType = options.outputFileType;
		 writeReverse = options.writeReverse;
	}

	public void finishWriting () throws IOException {
		//writer.flush();
		writer.close();
	}
	
	public void write(Instance instance) throws IOException{
		
		if(writeReverse)
			instance = instance.reverse();
		
		if(outputFileType.equals("column"))
			writeColumn(instance);
		else if(outputFileType.equals("wRow"))
			writeWRow(instance);
		else if(outputFileType.equals("wpRow"))
			writeWPRow(instance);
		else if(outputFileType.equals("wpTwoRows"))
			writeWPTwoRows(instance);
		else {
			System.out.println("no such outputFileType. Please check it in Options class");
			System.exit(1);
		}
	}
	
	/**按照CoNLL格式输出，词和词性都是按列排列*/
	public void writeColumn(Instance instance) throws IOException{
		int length = instance.words.length;
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<length; i++) {
			buf.append(instance.words[i]+"\t"+instance.tags[i]+"\n");
		}		
		buf.append("\n");
		writer.write(buf.toString());
	}
	
	/**输出所有词在一行*/
	public void writeWRow(Instance instance) throws IOException{
		int length = instance.words.length;
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<length-1; i++) {
			buf.append(instance.words[i]+" ");
		}		
		buf.append(instance.words[length-1]);
		buf.append("\n");
		writer.write(buf.toString());
	}
	
	/**输出所有词和词性在一行*/
	public void writeWPRow(Instance instance) throws IOException{
		
		String[] words = instance.words;
		String[] tags = instance.tags;		
		int length = words.length;
		
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<length-1; i++) {
			buf.append(words[i]+"_"+tags[i]+" ");
		}		
		buf.append(words[length-1]+"_"+tags[length-1]);
		buf.append("\n");
		writer.write(buf.toString());
	}
	
	/**输出所有词和词性在一行*/
	public void writeWPTwoRows(Instance instance) throws IOException{

		String[] words = instance.words;
		String[] tags = instance.tags;		
		int length = words.length;
		
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<length-1; i++) {
			buf.append(words[i]+" ");
		}		
		buf.append(words[length-1]+"\n");
		for(int i=0; i<length-1; i++) {
			buf.append(tags[i]+" ");
		}		
		buf.append(tags[length-1]+"\n");
		buf.append("\n");
		writer.write(buf.toString());
	}
	
	public void writeWPLattice(Lattice lattice, int cnt) throws IOException {
		lattice.writeWP(writer, cnt);		
	}
	

	
}
