package edu.hitsz.nlp.chunking;

public class BasicChunk {
	private String word;
	private String tag;
	private int start;
	private int end;

	public BasicChunk(String tag, int start, int end){
		this.tag = tag;
		this.start = start;
		this.end = end;
	}

	public BasicChunk( ){

	}

	public String getWord(){
		return word;
	}

	public String getTag(){
		return tag;
	}

	public int getStart(){
		return start;
	}

	public int getEnd(){
		return end;
	}
}
