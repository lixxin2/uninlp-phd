/**
 *
 */
package edu.hitsz.nlp.struct;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.struct.CfgTree;


/**
 * @author tm
 *
 */
public class CfgTree{

	public CfgNode head;
	//the variable to determine the position of words in sentence
	public int seqNumber = 0;

	public CfgTree(){
		head = new CfgNode();
	}




	/**
	 * iterative loading tree, like ((S (A x) (C y)))
	 * @param line The sentence in a string
	 * @param i Starting with 0
	 * @param length Number of characters in the sentence
	 * @isTOP 是否设置TOP为顶层短语label，或为空
	 */
	public void load(String line, int i, int length, boolean isTOP){
		this.head  = this.loadNode(line, i, length, isTOP);
	}


	/**
	 * Not direct called function by user
	 * iterative loading tree, like ((S (A x) (C y)))
	 * @param line The sentence in a string
	 * @param i Starting with 0
	 * @param length Number of characters in the sentence
	 * @param isTOP 是否设置TOP为顶层短语label，或为空
	 * @return
	 */
	private CfgNode loadNode(String line, int i, int length, boolean isTOP){
		//define a tree
		CfgNode node=new CfgNode();

		//determine the head
		//cross the blanks before "(" or ")"
		while(i<length&&line.charAt(i)==' ')
			i++;
		if(i<length&&line.charAt(i)=='('){
			node.setBeginCharSeq(i++);
			//cross blanks before phrase category mostly, "(" rarely
			while(i<length&&line.charAt(i)==' ')
				i++;
			//if two '(' is near, like ((, then we should create a top
			if(line.charAt(i)=='(') {
				if(isTOP)
					node.setLabel("TOP");
				else
					node.setLabel("");
			}
			else{
				int j=i;
				while(j<length&&line.charAt(j)!=' '&&line.charAt(j)!='('&&line.charAt(j)!=')'){
					j++;
				}
				node.setLabel(line.substring(i,j));
				i=j;
			}
		}
		//
		//for word, like "x" in ((s (A x))
		else if(i<length&&line.charAt(i)!=' '&&line.charAt(i)!='('&&line.charAt(i)!=')'){
			node.setBeginCharSeq(i);
			int j=i;
			while(j<length&&line.charAt(j)!=' '&&line.charAt(j)!='('&&line.charAt(j)!=')'){
				j++;
			}
			node.setSeq(seqNumber++);
			node.setBeginWordSeq(node.getSeq());
			node.setEndWordSeq(node.getSeq());
			node.setLabel(line.substring(i,j));
			node.setEndCharSeq(j);
			node.setHeadWord(line.substring(i,j));
			while(i<length&&line.charAt(i)==' ')
				i++;
			return node;
		}
		//fine the its tail
		if(line.charAt(i)==')'){
			node.setEndCharSeq(i+1);
			while(i<length&&line.charAt(i)==' ')
				i++;
			return node;
		}

		//iterative
		while(i<length){
			//cross blanks
			while(i<length&&line.charAt(i)==' ')
				i++;
			if(line.charAt(i)==')')
				break;
			//
			CfgNode son=new CfgNode();
			son=loadNode(line,i,length,isTOP);
			assert son!=null;
			son.setFatherNode(node);
			//
			if(node.getSonNode().size()==0){
				node.setBeginWordSeq(son.getBeginWordSeq());
				node.setEndWordSeq(son.getEndWordSeq());
			}
			else
				node.setEndWordSeq(son.getEndWordSeq());
			//
			node.getSonNode().add(son);
			i=son.getEndCharSeq();
		}
		node.setEndCharSeq(i+1);
		return node;
	}

	/**
	 * load tree from strings (ArrayList), like
	 * (S1(FRAG(NP*)
     *   	(NP(NP*
	 *			  *)
     *         (PP*
     *         (NP*
     *       	*)))
     *       	 *))
	 * @param line
	 * @param isTOP 
	 * @return
	 */
	public void load(ArrayList<String> lines, boolean isTOP){
		int linesNumber = lines.size();
		String sentence = "";
		for(int i=0; i<linesNumber; i++){
			//int j = lines.get(i).indexOf("*");
			int lastChar = lines.get(i).length()-1;
			if(lines.get(i).charAt(lastChar) == '*')
				sentence +=  " "+lines.get(i).replaceAll("\\*", " *").trim();
			else
				sentence +=  " "+lines.get(i).trim();
		}
		this.load(sentence, 0, sentence.length(), isTOP);
	}

	/**
	 * 针对Conll2011，2012评测，他的句法中的词和词性是和句法分开的
	 * @since Jun 5, 2012
	 * @param lines
	 * @param poses
	 * @param words
	 */
	public void loadWithPW(ArrayList<String> lines, ArrayList<String> poses, ArrayList<String> words){
		int linesNumber = lines.size();
		if(linesNumber != poses.size() || linesNumber != words.size()){
			System.out.println("the number of parse bits is different from POS, or Words");
			System.exit(0);
		}
		String sentence = "";
		for(int i=0; i<linesNumber; i++){
			//int j = lines.get(i).indexOf("*");
			int lineLength = lines.get(i).length();
			int index = lines.get(i).indexOf("*");
			String tmpString = "";
			if(index>0)
				tmpString += lines.get(i).substring(0,index);
			tmpString += " ("+poses.get(i)+" "+words.get(i)+")";
			if(index<lineLength-1)
				tmpString += lines.get(i).substring(index+1);
			sentence += " "+tmpString;
		}
		this.load(sentence, 0, sentence.length(), true);
	}
	
	
	/** 根据头结点规则找到每个短语的头结点*/
	public void findHead(CfgTreeHead newTreeHead){
		this.head.findHeadIter(newTreeHead);
	}


	/** 去掉包含词性为*NONE*的词，如果其祖宗节点只有一个，则依次递归取消*/
	public void cleanEmptyTerminal() {
		this.head.cleanEmptyTerminal();		
	}
	
	
	/**清理树的label，主要是nonterminal，去掉它的语义，比如把NP-OBJ-NN改为NP*/
	public void cleanNonTerminals() {
		this.head.cleanNonTermianls();		
		
	}
	
	/** 去掉重复的nonterminal, any unary X->X collapsed into one X node*/
	public void collapseUnaryRule() {
		this.head.collapseUnaryRule();
	}
	
	/** 去掉重复的nonterminal, any unary X->Y collapsed into one X#Y node*/
	public void collapseUnaryDiffRule() {
		this.head.collapseUnaryDiffRule();
	}
	
	
	
	
	
	
	/**
	 * find the ancestors of a node
	 * @param seq
	 * @return
	 */
	public ArrayList<CfgNode> findAncestor(int seq){
		if(seq >= seqNumber){
			System.out.println("the input number is larger than the number of words in sentence");
			System.exit(0);
		}
		ArrayList<CfgNode> nodes = new ArrayList<CfgNode>();
		CfgNode node = this.head;
		while(node.getBeginWordSeq()!=seq || node.getEndWordSeq()!=seq){
			nodes.add(node);
			int sonNumber = node.getSonNode().size();
			for(int i=0; i<sonNumber; i++){
				CfgNode son = node.getSonNode().get(i);
				if( son.getBeginWordSeq() <= seq && son.getEndWordSeq() >=seq){
					node = son;
					break;
				}
			}
		}
		nodes.add(node);
		return nodes;
	}

	/**
	 * find phrases with specified category in a tree
	 * @param phraseCategory
	 * @return
	 */
	public ArrayList<CfgNode> findPhrases(String phraseCategory){
		ArrayList<CfgNode> nodes = new ArrayList<CfgNode>();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack
			CfgNode temp = stack.get(stackLength - 1);
			if(temp.getLabel().equals(phraseCategory))
				nodes.add(temp);
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		return nodes;
	}

	/**
	 * find the top phrase whose head is the headWord
	 * @param headWord
	 * @return
	 */
	public CfgNode findTopPhrase(String headWord){
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack, and check whether it satisfies the conditions
			CfgNode temp = stack.get(stackLength - 1);
			if(temp.getHeadWord().equals(headWord))
				return temp;
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		return null;
	}



	/**
	 * find the phrase whose starts from [start], and ends at [end] in the tree.
	 * @param start
	 * @param end
	 * @return
	 */
	public CfgNode findPhrase(int start, int end){
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack, and check whether it satisfies the conditions
			CfgNode temp = stack.get(stackLength - 1);
			if(temp.getBeginWordSeq() == start && temp.getEndWordSeq() == end && !temp.getLabel().equals("TOP"))
				return temp;
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					if(temp.getSonNode().get(i).getBeginWordSeq() <= start && temp.getSonNode().get(i).getEndWordSeq() >= end)
						stack.add(temp.getSonNode().get(i));
			}
		}
		return null;
	}

	public CfgNode findCoFather(int aStart, int aEnd, int bStart, int bEnd){
		CfgNode coFather = null;
		ArrayList<CfgNode> newNode = new ArrayList<CfgNode>();
		newNode.add(this.head);
		while( newNode.size() != 0 ){
			int stackLength = newNode.size();
			//take the last node in stack, and check whether it satisfies the conditions
			CfgNode temp = newNode.get(stackLength - 1);
			if(temp.getBeginWordSeq() <= aStart && temp.getEndWordSeq() >= aEnd
					&& temp.getBeginWordSeq() <= bStart && temp.getEndWordSeq() >= bEnd )
				coFather = temp;
			//remove the last node
			newNode.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					newNode.add(temp.getSonNode().get(i));
			}
		}
		return coFather;
	}
	

	/** is inside a NP */
	public boolean isInNP(int aStart, int aEnd) {
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack
			CfgNode temp = stack.get(stackLength - 1);
			if(temp.getLabel().equals("NP") && (temp.getBeginCharSeq() < aStart || temp.getEndCharSeq() > aEnd))
				return true;
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		return false;
	}



	/**
	 * all phrase types in the tree
	 * @return
	 */
	public ArrayList<String> getAllPhraseTypes(){
		ArrayList<String> newVec = new  ArrayList<String>();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack
			CfgNode temp = stack.get(stackLength - 1);
			//remove the last node
			if(temp.getSonNode().size()>0)
				newVec.add(temp.getLabel());
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		return newVec;
	}

	/**
	 * all head
	 * @return
	 */
	public ArrayList<String> getAllHeadPos(){
		ArrayList<String> newVec = new  ArrayList<String>();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack
			CfgNode temp = stack.get(stackLength - 1);
			//remove the last node
			newVec.add(temp.getHeadPos());
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		return newVec;
	}


	/**
	 * store the part-of-speech tags and words into a file
	 * <p> 将句法树中的词和词性读取出来，按列排列,每句之间空一行
	 * @param outWriter
	 * @throws IOException
	 */
	public void storeWPColumn(FileWriter outWriter) throws IOException{
		StringBuffer sbuf = new StringBuffer();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack, and check whether it satisfies the conditions
			CfgNode temp = stack.get(stackLength - 1);
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			if(temp.isPOS()){
				String pos = temp.getLabel();
				if(pos.contains("-"))
					pos=pos.split("-")[0];
				String word = temp.getSonNode().get(0).getLabel();
				sbuf.append(word+ "\t" + pos+"\n");				
			}
			else {
				int sonNumber = temp.getSonNode().size();
				if(sonNumber>0){
					for(int i = sonNumber-1; i>=0; i--)
						stack.add(temp.getSonNode().get(i));
				}
			}
		}
		outWriter.write(sbuf.toString()+"\n");
		//System.out.println();
	}
	
	/**
	 * 将句法树中的词读取出来，一句话一行，每个词之间用空格间隔(或不间隔)
	 * @param outWriter
	 * @param isSpaced
	 * @throws IOException
	 */
	public void storeWordRow(FileWriter outWriter, boolean isSpaced) throws IOException{
		StringBuffer sbuf = new StringBuffer();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack, and check whether it satisfies the conditions
			CfgNode temp = stack.get(stackLength - 1);
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			if(temp.isPOS()){
				String word = temp.getSonNode().get(0).getLabel();
				if(isSpaced)
					sbuf.append(word+ " ");
				else
					sbuf.append(word);
			}
			else {
				int sonNumber = temp.getSonNode().size();
				if(sonNumber>0){
					for(int i = sonNumber-1; i>=0; i--)
						stack.add(temp.getSonNode().get(i));
				}
			}
		}
		outWriter.write(sbuf.toString().trim() + "\n");
		//System.out.println();
	}

	
	
	
	/**
	 * 提取句法树中的所有推导规则
	 * @since Apr 21, 2012
	 * @param dir
	 * @return
	 */
	public ArrayList<String> findAllRules() {
		ArrayList<String> rules = new ArrayList<String>();
		ArrayList<CfgNode> stack = new ArrayList<CfgNode>();
		stack.add(this.head);
		while( stack.size() != 0 ){
			int stackLength = stack.size();
			//take the last node in stack
			CfgNode temp = stack.get(stackLength - 1);
			//remove the last node
			stack.remove(stackLength - 1);
			//add sons of the last node
			int sonNumber = temp.getSonNode().size();
			if (sonNumber > 0) {
				boolean addRule = true;
				StringBuffer bf = new StringBuffer();
				String father = temp.getLabel();
				bf.append(father);
				if(addRule) {
					for(int i = sonNumber-1; i>=0; i--) {
						String son = temp.getSonNode().get(i).getLabel();						
						bf.append("\t" + son);
					}
					rules.add(bf.toString());
				}
			}
			if(sonNumber>0){
				for(int i = sonNumber-1; i>=0; i--)
					stack.add(temp.getSonNode().get(i));
			}
		}
		
		
		
		return rules;
	}
	
	
	

	/**
	 * store cfg tree
	 * @param newWriter
	 * @throws IOException
	 */
	public void store(FileWriter newWriter) throws IOException{
		this.head.storeIter(newWriter);
		newWriter.write("\n");
	}

	/**
	 * display cfg tree
	 */
	public void output(){
		this.head.output( );
		System.out.println();
	}
	
	public String toString(){
		return this.head.toString();
	}


	/*
	public Object clone(){
		CfgTree o=null;
		try{
			o = (CfgTree)super.clone();
			if(o.sonTree!=null){
				for(int i=0;i<o.sonTree.size();i++)
					o.sonTree.add(this.sonTree.get(i));
			}
		}
		catch(CloneNotSupportedException e){
			e.printStackTrace();
			}
		return o;
		}
	*/


	/**
	 * 从parse文件或文件夹中提取出中文词和词性
	 * extractPosWord("/home/tm/disk/d1/LDC2011T03/ontonotes-release-4.0/data/chinese/annotations","/home/tm/disk/d1/alltrain-wp");
	 */
	private static void extractPosWord(String dirName, String outName){
	 	FileTree newTree = new FileTree();
    	newTree.generateFrom(dirName);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix(".parse");
    	int fileNumber = fileNames.size();
    	if(fileNumber > 0){
    		try{
	    		FileWriter outWriter = new FileWriter(outName);
	    		System.out.println("Has converted ");
	    		int i=0;
	    		for(; i<fileNumber; i++){
	    			if(i%1 == 0)
	    				System.out.println(i+"...");
	    			ConllFile newFile = new ConllFile();
	    			newFile.readFrom(fileNames.get(i), -1);
	    			for(int j=0; j<newFile.getSentenceNumber(); j++){
	    				CfgTree newCfgTree = new CfgTree();
	    				newCfgTree.load(newFile.getSentence(j).getWords(0), true);
	    				newCfgTree.storeWPColumn(outWriter);
	    			}
	    		}
	    		System.out.println(i);
	    		outWriter.close();
    		}
    		catch (IOException e){
    			System.out.println("IOException: " + e);
    		}
    	}
	}





	/**
	 * read cfg trees in files from a directory
	 */
	private static void readFile(){
		
		String dirName = "/home/tm/disk/disk1/conll2012/conll-2012-train-v0/data/files/data/english/annotations";
    	
		FileTree newTree = new FileTree();
    	newTree.generateFrom(dirName);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix(".parse");
    	int fileNumber = fileNames.size();
    	HashMap<String, Integer> ruleStat = new HashMap<String, Integer>();
    	HashMap<String, Integer> wordStat = new HashMap<String, Integer>();
    	if(fileNumber > 0){
    		System.out.println("Has converted ");
    		int i=0;
    		for(; i<fileNumber; i++){
    			if(i%100 == 0)
    				System.out.println(i+"...");
    			ConllFile newFile = new ConllFile();
    			newFile.readFrom(fileNames.get(i), 0);
    			for(int j=0; j<newFile.getSentenceNumber(); j++){
    				CfgTree newCfgTree = new CfgTree();
    				newCfgTree.load(newFile.getSentence(j).getWords(0), true);
    				ArrayList<String> rules = newCfgTree.findAllRules();
    				for(String rule: rules) {
    					if(!rule.matches(".*[a-z].*")) {
	    					if(ruleStat.containsKey(rule)) {
	    						ruleStat.put(rule, ruleStat.get(rule)+1);
	    					}
	    					else {
	    						ruleStat.put(rule, 1);
	    					}
    					}
    					else {
    						if(wordStat.containsKey(rule)) {
    							wordStat.put(rule, wordStat.get(rule)+1);
	    					}
	    					else {
	    						wordStat.put(rule, 1);
	    					}
    					}
    				}
    				//newCfgTree.output();
    			}
    		}
    		System.out.println(i);
    	}
    	
    	String output = "/home/tm/Downloads/rules.txt";
    	try {
	    	FileWriter writer = new FileWriter(new File(output));
	    	Iterator<Entry<String, Integer>> iter = ruleStat.entrySet().iterator();
	    	while(iter.hasNext()) {
	    		Entry<String, Integer> entry = iter.next();
	    		writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
	    	}
	    	writer.write("\n\n\n\n\n");
	    	iter = wordStat.entrySet().iterator();
	    	while(iter.hasNext()) {
	    		Entry<String, Integer> entry = iter.next();
	    		writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
	    	}
	    	writer.close();
	    	
    	}
    	catch (IOException e) {
    		
    	}
    	
	}

	
	
	/**
	 * test the utility of this class
	 */
	private static void testCfgTree(){

		CfgTree newCfgTree = new CfgTree();
		/*
		String line = "((S (A x) (C y)))";
		//line = "(TOP(FRAG(NP *) (NP(NP * *) (PP * (NP * *))) *))";
		line = "(S1(FRAG(NP * ) (NP(NP *   * ) (PP *  (NP *   * )))  * ))";
		newCfgTree.loadCTree(line,0,line.length());
		*/
		ArrayList<String> newSen = new ArrayList<String>();
		newSen.add("(S1(FRAG(NP*)");
		newSen.add("(NP(NP*");
		newSen.add("*)");
		newSen.add("(PP*");
		newSen.add("(NP*");
		newSen.add("*)))");
		newSen.add("*))");

		ArrayList<String> newPos = new ArrayList<String>();
		newPos.add("v1");
		newPos.add("v2");
		newPos.add("v3");
		newPos.add("v4");
		newPos.add("v5");
		newPos.add("v6");
		newPos.add("v7");

		ArrayList<String> newWord = new ArrayList<String>();
		newWord.add("我们");
		newWord.add("是");
		newWord.add("a3");
		newWord.add("a4");
		newWord.add("a5");
		newWord.add("a6");
		newWord.add("a7");

		newCfgTree.loadWithPW(newSen,newPos,newWord);
		//newCfgTree.loadHead("/home/tm/conll/test/headrules.txt");
		newCfgTree.output();
		newCfgTree.getAllPhraseTypes();
		newCfgTree.getAllHeadPos();
		//newCfgTree.findPhrases("NP");

	}




	public static void main(String[] args){
		readFile();
	}
}

