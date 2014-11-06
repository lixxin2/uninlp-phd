package edu.hitsz.nlp.sentence;

import java.util.ArrayList;
import java.util.List;


public class Distance {
	
	public int row;
	public int column;
	
	public int dist;          //距离
	public int correct;        //正确的数目
	
	public int insertion;
	public int deletion;
	public int substitution;
	
	public int transpose;
	public String action;
	public Distance prev;
	
	//reference = N=S+D+C
	
	public Distance(int row, int column, int insertion, int deletion, int substitution) {
		this.row = row;
		this.column = column;
		this.insertion = insertion;
		this.deletion = deletion;
		this.substitution = substitution;	
		this.transpose = 0;
		sum();
	}
	
	public Distance(int row, int column, int insertion, int deletion, int substitution, int transpose) {
		this(row, column, insertion, deletion, substitution);
		this.transpose = transpose;		
		sum();
	}
	
	public void sum() {
		dist = insertion+deletion+substitution+transpose;
	}
	
	public void output() {
		System.out.println("dist: " + dist + "\t"
				+ "correct: " + correct + "\t"
				+ "insert: " + insertion + "\t"
				+ "delete: " + deletion + "\t"
				+ "substitution: " + substitution + "\t");
	}
	
	public void update(int insertionAdd, int deletionAdd, int substitutionAdd) {
		this.insertion += insertionAdd;
		this.deletion += deletionAdd;
		this.substitution += substitutionAdd;
	}
	
	/** 获得最佳序列 */
	public ArrayList<Distance> getDistances() {
		ArrayList<Distance> distances = new ArrayList<Distance>();
		Distance dist = this;
		while(dist != null) {
			distances.add(0, dist);
			dist = dist.prev;
		}
		return distances;
	}
}
