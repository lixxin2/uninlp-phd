package edu.hitsz.nlp.transDep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.algorithm.hash.HashCodeUtil;

/**
 * 最基层的一个结果，包括一个词和其依存关系
 * @author Xinxin Li
 * @since Feb 25, 2013
 */
public class DepItem implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int index;
	public String word;
	public String postag;
	public int head;
	public String deprel;
	//左子节点，越大越左
	List<DepItem> leftSons;
	//右子节点，越大越右
	List<DepItem> rightSons;
	//
	public int bgn;
	public int end;
	
		
	public DepItem(int index, String word, String postag) {
		this.index = index;
		this.word = word;
		this.postag = postag;
		head = -1;
		deprel = null;
		leftSons = new ArrayList<DepItem>();
		rightSons = new ArrayList<DepItem>();
		bgn = index;
		end = index;
	}
	
	public DepItem(int index, String word, String postag, int head) {
		this(index, word, postag);
		this.head = head;
	}	
	
	public DepItem(int index, String word, String postag, int head, String deprel) {
		this(index, word, postag, head);
		this.deprel = deprel;
	}	
	
	public DepItem(DepItem item) {
		this(item.index, item.word, item.postag, item.head, item.deprel);
		leftSons.addAll(item.leftSons);
		rightSons.addAll(item.rightSons);
		bgn = item.bgn;
		end = item.end;	
	}
	
	
	@Override
	public String toString() {
		
		if(this == null) return "";
		StringBuffer sbuf = new StringBuffer();
		sbuf.append( index + "/"
				+ (word != null ? word : "--") + "/"
				+ (postag != null ? postag : "--")+ "/" 
				+ head + "/"
				+ (deprel != null ? deprel : "--") + "/"
				+ bgn + "-" + end);
		
		int leftSonSize = leftSons.size();
		int rightSonSize = rightSons.size();
		if(leftSonSize > 0 || rightSonSize > 0) {
			sbuf.append(" {");
			for(DepItem sonItem : leftSons)
				sbuf.append(sonItem + ";");
			sbuf.append("}");
			sbuf.append("{");
			for(DepItem sonItem : rightSons)
				sbuf.append(sonItem + ";");
			sbuf.append("}");
		}
				
		return sbuf.toString();		
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj == null || !(obj instanceof DepItem)) return false;
		DepItem item = (DepItem)obj;
		if(item.index != index
				|| !item.word.equals(word) || !item.postag.equals(postag) 
				|| item.head != head 
				|| !(item.deprel== null && deprel == null) && item.deprel.equals(deprel))
			return false;
		if(!item.leftSons.equals(leftSons) || !item.rightSons.equals(rightSons))
			return false;
		if(item.bgn != bgn || item.end != end) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hashCode = HashCodeUtil.SEED;
	    hashCode = HashCodeUtil.hash(hashCode, index);
	    hashCode = HashCodeUtil.hash(hashCode, word);
	    hashCode = HashCodeUtil.hash(hashCode, postag);
	    hashCode = HashCodeUtil.hash(hashCode, head);
	    hashCode = HashCodeUtil.hash(hashCode, deprel);
	    hashCode = HashCodeUtil.hash(hashCode, leftSons);
	    hashCode = HashCodeUtil.hash(hashCode, rightSons);
	    hashCode = HashCodeUtil.hash(hashCode, bgn);
	    hashCode = HashCodeUtil.hash(hashCode, end);
        return hashCode;
	}

}
