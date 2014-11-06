package edu.hitsz.nlp.corpus.ptb;

public class Words {

	
	public static String[] PTBPosTaggers = {};
	
	
	public static String[] punctuations = {
		"#","$","\"","(",")",",",".","?","!",":",";","`","'"};
	
	
	public static String[] endPunctuations = {".","?","!",",",";",":"};
	
	
	
	
	public static String letters = "[a-zA-Z.]+";
	
	
	public static void main(String[] args) {
		System.out.println("Mr.a1a".matches(Words.letters));
	}
	
	
	
}
