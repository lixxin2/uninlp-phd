/**
 * 
 */
package edu.hitsz.nlp.coref;

import java.util.ArrayList;

/**
 * @author tm
 *
 */
public class CoreferenceNode {	
	private int sentenceNumber;
	private int label;
	private int start;
	private int end;
	//CoreferenceNode precedent;
	
	
	
	public CoreferenceNode(){
		sentenceNumber = -1;
		label = -1;
		start = -1;
		end = -1;
		//precedent = new CoreferenceNode();
	}
	
	
	
	
	public void setSentenceNumber(int sentenceNumber){
		this.sentenceNumber = sentenceNumber;
	}	
	public void setLabel(int label){
		this.label = label;
	}
	public void setStart(int start){
		this.start = start;
	}
	public void setEnd(int end){
		this.end = end;
	}
	
	public int getSentenceNumber(){
		return this.sentenceNumber;
	}	
	public int getLabel(){
		return this.label;
	}
	public int getStart(){
		return this.start;
	}
	public int getEnd(){
		return this.end;
	}
	
	
	/**
	 * get ArrayList of coreference node from columns of coreference structure
	 *   (64
	 *     -
	 *     64),
	 * ususally called by train procedure
	 * @param coreference
	 * @param coreferenceColumn
	 * @param sentenceSeq
	 */
	public static void getFromColumn(ArrayList<CoreferenceNode> coreference, ArrayList<String> coreferenceColumn, int sentenceSeq){
		int length = coreferenceColumn.size();
		int i=0;
		while(i<length){
			//
			String[] parts = coreferenceColumn.get(i).split("\\|");
			for(String part : parts){
				if(part.startsWith("(")){
					CoreferenceNode newNode = new CoreferenceNode();
					if(part.endsWith(")")){
						newNode.setSentenceNumber(sentenceSeq);
						newNode.setLabel(Integer.parseInt(part.substring(1,part.length()-1)));
						newNode.setStart(i);
						newNode.setEnd(i);
					}						
					else{
						newNode.setSentenceNumber(sentenceSeq);
						newNode.setLabel(Integer.parseInt(part.substring(1)));
						newNode.setStart(i);						
						int j=i+1;
						boolean match = false;            //to sign the end
						int sign = 1;
						while(j<length){
							String[] sparts = coreferenceColumn.get(j).split("\\|");
							for(String spart : sparts){
								if(spart.endsWith(")"))
									sign--;
								if(spart.startsWith("("))
									sign++;
								if(sign == 0)
									break;
							}
							for(String spart : sparts){
								if( !spart.startsWith("(") &&  spart.endsWith(")") && newNode.getLabel() == Integer.parseInt(spart.substring(0,spart.length()-1))){
									newNode.setEnd(j);
									match=true;
									break;	
								}
							}
							if(match)
								break;
							j++;
						}
						if(!match){
							System.out.println("The coreference column is wrong");
							System.exit(-1);
						}
						if(j >= length)
							newNode.setEnd(length-1);	
					}
					//add newNode to coreference, according to the sequence
					int k=coreference.size();
					if(k>0){
						int m=0;
						for(; m<k; m++){
							if(coreference.get(m).isAfter(newNode)){
								coreference.add(m,newNode);
								break;
							}								
						}
						if(m >= k)
							coreference.add(newNode);
					}
					else
						coreference.add(newNode);
				}
			}
			i++;
		}	
	}
	
	
	/**
	 * determine whether labels of the two coreference nodes are same
	 * @param newNode
	 * @return
	 */
	public boolean isLabelSame(CoreferenceNode newNode){
		if(this.label == newNode.label)
			return true;
		else
			return false;
	}
	
	
	
	/**
	 * determine whether labels of the coreference node is several words before the new one 
	 * @param newNode
	 * @return
	 */
	public boolean isBefore(CoreferenceNode newNode){
		if(this.sentenceNumber < newNode.sentenceNumber || 
				(this.sentenceNumber == newNode.sentenceNumber && this.end < newNode.start))
			return true;
		else
			return false;
	}
	
	
	/**
	 * determine whether labels of the coreference node is several words after the new one 
	 * @param newNode
	 * @return
	 */
	public boolean isAfter(CoreferenceNode newNode){
		if(this.sentenceNumber > newNode.sentenceNumber || 
				(this.sentenceNumber == newNode.sentenceNumber && this.start > newNode.end))
			return true;
		else
			return false;
	}
	
	
	/**
	 * determine whether the two coreference nodes are same
	 * @param newNode
	 * @return
	 */
	public boolean isSame(CoreferenceNode newNode){
		if(this.sentenceNumber == newNode.sentenceNumber && this.label == newNode.label 
				&& this.start == newNode.start && this.end == newNode.end)
			return true;
		else
			return false;
	}
	
	
	/**
	 * determine whether the two coreference nodes are overlapped
	 *                    |______________|              current
	 *                |________|                        new1
	 *                       |_______________|          new2
	 * @param newNode
	 * @return
	 */
	public boolean isOverlap(CoreferenceNode newNode){
		if( this.sentenceNumber == newNode.sentenceNumber
				&& ((this.start <= newNode.end && this.end >= newNode.end && this.start > newNode.start)
						|| (this.start <= newNode.start && this.end >= newNode.start && this.end < newNode.end)))
			return true;
		else
			return false;
	}
	
	/**
	 * determine whether the two coreference nodes are not touched
	 *                    |______________|              current
	 *         |________|                               new1
	 *                                    |_________|   new2
	 * @param newNode
	 * @return
	 */
	public boolean isNotTouched(CoreferenceNode newNode){
		if( this.sentenceNumber != newNode.sentenceNumber
				|| this.start > newNode.end || this.end < newNode.start)
			return true;
		else
			return false;
	}
	
		
	/**
	 * determine whether the this coreference node contains the new one
	 * @param newNode
	 * @return
	 */
	public boolean isContain(CoreferenceNode newNode){
		if( this.sentenceNumber == newNode.sentenceNumber
				&& this.start <=newNode.start && this.end >= newNode.end)
			return true;
		else
			return false;
	}
	
	/**
	 * determine whether the this coreference node is included in the new one
	 * @param newNode
	 * @return
	 */
	public boolean isIncluded(CoreferenceNode newNode){
		if( this.sentenceNumber == newNode.sentenceNumber
				&& this.start >=newNode.start && this.end <= newNode.end)
			return true;
		else
			return false;
	}
	
	/**
	 * determine whether the this coreference node contains or be included the new one
	 * @param newNode
	 * @return
	 */
	public boolean isFatherOrSon(CoreferenceNode newNode)
	{
		if(this.isContain(newNode) || this.isIncluded(newNode))
			return true;
		else 
			return false;
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
