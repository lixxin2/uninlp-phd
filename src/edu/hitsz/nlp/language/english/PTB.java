package edu.hitsz.nlp.language.english;

public class PTB {

	
	public static String[] PTBPosTaggers = {};
	
	
	public static String[] punctuations = {
		"#","$","\"","(",")",",",".","?","!",":",";","`","'"};
	
	
	public static String[] endPunctuations = {".","?","!",",",";",":","~","/","//","*"};
	
	
	
	
	public static String letters = "[a-zA-Z.]+";
	
	
	public static void main(String[] args) {
		System.out.println("Mr.a1a".matches(PTB.letters));
	}
	
	
	
}
