/**
 * 
 */
package edu.hitsz.ml.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * @author tm
 *
 */
public class ClassificationEvaluation {

	private int classNumber;
	private Vector<String> classes;
	private Vector<Integer> goldNumber;
	private Vector<Integer> predictNumber;
	private Vector<Integer> correctNumber;
	private Vector<Double> precision;
	private Vector<Double> recall;
	private Vector<Double> fvalue;
	private int totalNumber;
	private int totalCorrectNumber;
	private double accuracy;
	
	public void initialClass(int number){
		classNumber = 0;
		classes = new Vector<String>();
		goldNumber = new Vector<Integer>();
		predictNumber = new Vector<Integer>();
		correctNumber = new Vector<Integer>();
		precision = new Vector<Double>();
		recall = new Vector<Double>();
		fvalue = new Vector<Double>();
		totalNumber = number;
		totalCorrectNumber = 0;
		accuracy = 0.0;
	}
	
	public void eval(String[] gold, String[] predict){
		int number = gold.length;
		if(number != predict.length){
			System.out.println("The number of gold and predict data is different");
			System.exit(-1);	
		}
		initialClass(number);
		for(int i=0; i<number; i++){
			String goldToken = gold[i];
			String predictToken = predict[i];
			if(!classes.contains(goldToken)){
				classes.add(goldToken);
				goldNumber.add(0);
				predictNumber.add(0);
				correctNumber.add(0);
			}
			if(!classes.contains(predictToken)){
				classes.add(predictToken);
				goldNumber.add(0);
				predictNumber.add(0);
				correctNumber.add(0);
			}
			int goldIndex = classes.indexOf(goldToken);
			int predictIndex = classes.indexOf(predictToken);
			if(goldIndex == predictIndex){
				goldNumber.set(goldIndex, goldNumber.get(goldIndex)+1);
				predictNumber.set(predictIndex, predictNumber.get(predictIndex)+1);
				correctNumber.set(goldIndex, correctNumber.get(goldIndex)+1);
			}
			else{
				goldNumber.set(goldIndex, goldNumber.get(goldIndex)+1);
				predictNumber.set(predictIndex, predictNumber.get(predictIndex)+1);
			}				
		}
		//computer
		classNumber = classes.size();
		for(int i=0; i<classNumber; i++){
			if(predictNumber.get(i) != 0)
				precision.add(correctNumber.get(i)/(double)predictNumber.get(i));
			else
				precision.add(0.0);
			if(goldNumber.get(i) != 0)
				recall.add(correctNumber.get(i)/(double)goldNumber.get(i));
			else
				recall.add(0.0);
			if(predictNumber.get(i) != 0 && goldNumber.get(i) != 0)	
				fvalue.add(2*precision.get(i)*recall.get(i)/(precision.get(i)+recall.get(i)));
			else
				fvalue.add(0.0);
			totalCorrectNumber += correctNumber.get(i);
		}
		if(totalNumber != 0)
			accuracy = totalCorrectNumber/(double)totalNumber;
		else
			accuracy = 0.0;
	}
	
	
	public void store(String evalFileName){
		try{
			FileWriter evalWriter = new FileWriter(evalFileName);
			StringBuffer newBuffer = new StringBuffer();
			newBuffer.append("\nclassNumber\tclass\tgold\tpredict\tcorrect\t"
					+"precision\trecall\t\tfvalue\n");
			for(int i=0; i<classNumber; i++){
				newBuffer.append("\t"+i+"\t"+classes.get(i)+"\t"
						+goldNumber.get(i)+"\t"+predictNumber.get(i)+"\t"+correctNumber.get(i)+"\t"
						+100*precision.get(i)+"\t"+100*recall.get(i)+"\t"+100*fvalue.get(i)+"\n");
			}
			newBuffer.append("\ntotalNumber:\t"+totalNumber);
			newBuffer.append("\ntotalCorrectNumber:\t"+totalCorrectNumber);
			newBuffer.append("\ntotalaccuracy:\t"+100*accuracy);
			evalWriter.write(newBuffer.toString());
			evalWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException" + e);
			System.exit(-1);
		}
	}
	
	public void display(){
		StringBuffer newBuffer = new StringBuffer();
		newBuffer.append("\nclassNumber\tclass\tgold\tpredict\tcorrect\t"
				+"precision\trecall\t\tfvalue\n");
		for(int i=0; i<classNumber; i++){
			newBuffer.append("\t"+i+"\t"+classes.get(i)+"\t"
					+goldNumber.get(i)+"\t"+predictNumber.get(i)+"\t"+correctNumber.get(i)+"\t"
					+100*precision.get(i)+"\t"+100*recall.get(i)+"\t"+100*fvalue.get(i)+"\n");
		}
		newBuffer.append("\ntotalNumber:\t"+totalNumber);
		newBuffer.append("\ntotalCorrectNumber:\t"+totalCorrectNumber);
		newBuffer.append("\ntotalaccuracy:\t"+100*accuracy);
		System.out.println(newBuffer.toString());
	}
	
	public static void test(){
		String[] gold = {"a","a","b","b","c","c","a"};
		String[] predict = {"a","b","b","c","c","a","a"};
		ClassificationEvaluation newEval = new ClassificationEvaluation();
		newEval.eval(gold, predict);
		newEval.display();
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}

}
