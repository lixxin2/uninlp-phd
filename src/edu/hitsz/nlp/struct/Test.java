package edu.hitsz.nlp.struct;

public class Test {

	public static void main(String[] args) {
		
		ConllFile file = new ConllFile();
		file.readFrom("/media/study/corpora/treebank/ctb_7/data/utf-8/test-dep-utf8", -1);
		System.out.println();
		
	}
	
}
