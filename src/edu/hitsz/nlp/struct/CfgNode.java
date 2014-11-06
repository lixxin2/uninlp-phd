package edu.hitsz.nlp.struct;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class CfgNode {
	
	private String head2tail;
	//the current String, phrase category
	private String label;
	//the position of words in ternimal nodes, e.g., the first word is seq 0
	private int seq;
	//the position of the words, phrases in sentence
	private int beginWordSeq;
	private int endWordSeq;
	//the position of characters of words in sentence
	private int beginCharSeq;
	private int endCharSeq;
	//the word chosen as the head of a phrase,  
	private String headWord;
	private String pos;
	private String headPos;
	//for conversion between dependency and cfg tree structure
	private int head=0;

	private CfgNode fatherNode;
	private Vector<CfgNode> sonNode;
	
	public CfgNode(){
		sonNode = new Vector<CfgNode>();
	}
	
	
	public String getLabel(){
		return this.label;
	}	
	public int getBeginWordSeq(){
		return this.beginWordSeq;
	}	
	public int getEndWordSeq(){
		return this.endWordSeq;
	}
	public int getBeginCharSeq(){
		return this.beginCharSeq;
	}	
	public int getEndCharSeq(){
		return this.endCharSeq;
	}
	public int getSeq(){
		return this.seq;
	}
	public String getHeadWord(){
		return this.headWord;
	}
	public String getPos(){
		return this.pos;
	}
	public String getHeadPos(){
		return this.headPos;
	}
	public int getHead(){
		return this.head;
	}
	public CfgNode getFatherNode(){
		return this.fatherNode;
	}
	public Vector<CfgNode> getSonNode(){
		return this.sonNode;
	}
	
	
	public void setLabel(String label){
		this.label = label;
	}	
	public void setBeginWordSeq(int beginWordSeq){
		this.beginWordSeq = beginWordSeq;
	}	
	public void setEndWordSeq(int endWordSeq){
		this.endWordSeq = endWordSeq;
	}
	public void setBeginCharSeq(int beginCharSeq){
		this.beginCharSeq = beginCharSeq;
	}	
	public void setEndCharSeq(int endCharSeq){
		this.endCharSeq = endCharSeq;
	}
	public void setSeq(int seq){
		this.seq = seq;
	}
	public void setHeadWord(String headWord){
		this.headWord = headWord;
	}
	public void setHeadPos(String headPos){
		this.headPos = headPos;
	}
	public void setHead(int head){
		this.head = head;
	}
	public void setFatherNode(CfgNode fatherNode){
		this.fatherNode = fatherNode;
	}
	
	
	
	/**
	 * Store node iteratively and deeply
	 * @param newWriter
	 * @throws IOException
	 */
	public void storeIter(FileWriter newWriter) throws IOException{
		if(sonNode.size()!=0){
			newWriter.write(" ("+label);
			if(sonNode!=null)
				for(int i=0;i<sonNode.size();i++){
					sonNode.get(i).storeIter(newWriter);
				}
			newWriter.write(")");	
		}
		else
			newWriter.write(" "+label);			
	}
	
	
	/**
	 * use CfgTreeHead class to find the head of a phrase
	 * 找到一个短语的头结点
	 * @param newTreeHead
	 */
	public void findHeadIter(CfgTreeHead newTreeHead){
		int sonNumber = this.getSonNode().size();
		if(sonNumber == 0){
			this.setHeadWord(this.getLabel());
			this.setHeadPos(this.pos);
		}
		else if(sonNumber == 1 && this.getSonNode().get(0).getSonNode().size() ==0 ){
			this.setHeadWord(getSonNode().get(0).getHeadWord());
			this.setHeadPos(this.getLabel());	
		}				
		else{
			Vector<String> sonLabel = new Vector<String>();
			for(int i=0; i<sonNumber; i++){
				getSonNode().get(i).findHeadIter(newTreeHead);
				sonLabel.add(getSonNode().get(i).getLabel());
			}
			int i = newTreeHead.findHead(getLabel(),sonLabel);
			this.setHeadWord(getSonNode().get(i).getHeadWord());
			this.setHeadPos(getSonNode().get(i).getHeadPos());
		}
	}
	
	/**
	 * whether this node is word itself
	 * @return
	 */
	public boolean isWord(){
		if(this.sonNode.size()==0)
			return true;
		return false;
	}
	
	/**
	 * whether this node is a part-of-speech tag
	 * @return
	 */
	public boolean isPOS(){
		if(this.sonNode.size() == 1 && this.sonNode.get(0).sonNode.size() == 0)
			return true;
		return false;
	}
	
	//
	public void output(){
		if(sonNode.size()!=0){
			System.out.print("(");
			System.out.print(label);
			System.out.print("-"+seq);
			System.out.print("-"+beginWordSeq);
			System.out.print("-"+endWordSeq);
			System.out.print("-"+headWord);
			System.out.print("-"+headPos);
			//System.out.print("-"+head2tail);
			for(int i=0;i<sonNode.size();i++){
				System.out.print(" ");
				sonNode.get(i).output();
			}
			System.out.print(")");
		}
		else{
			System.out.print(label);
			System.out.print("-"+seq);
			System.out.print("-"+beginWordSeq);
			System.out.print("-"+endWordSeq);
			System.out.print("-"+headWord);
			System.out.print("-"+headPos);
		}
	}	
	
	public String toString() {
		if(this.isWord())
			return this.label;
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("("+this.label+" ");
			for(CfgNode node : this.sonNode)
				sb.append(node);
			sb.append(")");
			return sb.toString();
		}
	}
	
	

	/**
	 * 
	 * 
	 */
	public void compHead2Tail(){
		String tempstring=label+"-"+beginCharSeq+"-"+endCharSeq;
		System.out.println(tempstring);
		head2tail=tempstring;
		if(sonNode.size()!=0){
			for(int i=0;i<sonNode.size();i++){
				String sonstring=sonNode.get(i).label+"_____"+sonNode.get(i).beginCharSeq+"_____"+sonNode.get(i).endCharSeq;
				sonNode.get(i).head2tail=sonstring+"+++++"+head2tail;
				sonNode.get(i).compHead2TailIter();
			}
		}
	}
	
	private void compHead2TailIter(){
		if(sonNode.size()!=0){
			for(int i=0;i<sonNode.size();i++){
				String tempstring=sonNode.get(i).label+"_____"+sonNode.get(i).beginCharSeq+"_____"+sonNode.get(i).endCharSeq;
				sonNode.get(i).head2tail=tempstring+"+++++"+head2tail;
				sonNode.get(i).compHead2TailIter();
			}
		}
	}
	
	
	
	/** 清除里面的空白词*/
	public void cleanEmptyTerminal() {
		//如果
		if(label.equals("-NONE-")) {
			CfgNode node = this;
			while(node.fatherNode != null && node.fatherNode.sonNode.size() == 1)
				node = node.fatherNode;
			if(node.fatherNode.sonNode.size() > 1) {
				CfgNode father = node.fatherNode;
				father.sonNode.remove(node);				
			}			
			else if(node.fatherNode == null) {
				System.out.println("It's a emtpy sentence");
				node = null;
			}			
		}
		//处理子节点
		else if(this.sonNode != null && this.sonNode.size() > 0) {
			int number = this.sonNode.size();
			for(int i=number-1; i>=0; i--)
				this.sonNode.get(i).cleanEmptyTerminal();
		}		
	}
	
	/**清理短语，去掉它的语义，比如把NP-OBJ改为NP*/
	public void cleanNonTermianls() {
		if(this.sonNode != null && this.sonNode.size() > 0) {
			if(label.matches("[A-Z]+[-=].+")) {
				label = label.split("[-=]")[0];
			}
			for(CfgNode node : this.sonNode)
				node.cleanNonTermianls();
		}
	}
	
	/**any unary X->X collapsed into one X node*/
	public void collapseUnaryRule() {
		if(this.sonNode != null) {
			if(this.sonNode.size() == 1 && this.label.equals(this.sonNode.get(0).label)) {
				if(this.sonNode.get(0).sonNode != null)
					this.sonNode = this.sonNode.get(0).sonNode;
			}
		}
		if(this.sonNode != null) {
			if(this.sonNode.size() > 0){
				for(CfgNode node : this.sonNode)
					node.collapseUnaryRule();
			}
		}
	}
	
	/**any unary X->Y collapsed into one X#Y node*/
	public void collapseUnaryDiffRule() {
		if(this.sonNode != null) {
			if(this.sonNode.size() == 1) {
				if(this.label.length() > 0)
					this.label = this.label + '#' + this.sonNode.get(0).label;
				if(this.sonNode.get(0).sonNode != null)
					this.sonNode = this.sonNode.get(0).sonNode;
			}
		}
		if(this.sonNode != null) {
			if(this.sonNode.size() > 0){
				for(CfgNode node : this.sonNode)
					node.collapseUnaryRule();
			}
		}
	}
	
	
	
	
	
	
	
	
}
