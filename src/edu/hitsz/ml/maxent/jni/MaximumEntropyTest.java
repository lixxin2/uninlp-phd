package edu.hitsz.ml.maxent.jni;

import java.util.HashMap;

public class MaximumEntropyTest {

	
	
	public static void METest(){
		
		MaximumEntropyJNI newME = new MaximumEntropyJNI();	
		
		newME.begin_add_event();
		String[] context1= new String[2];
		context1[0] = "a";context1[1] = "a";
		String outcome1 = "A";
		int i = 1;
		newME.add_event(context1, outcome1, i);
		String[] context2= new String[2];
		context2[0] = "b";context2[1] = "b";
		String outcome2 = "B";
		newME.add_event(context2, outcome2, i);
		String[] context3= new String[2];
		context3[0] = "c";context3[1] = "c";
		String outcome3 = "C";
		newME.add_event(context3, outcome3, i);
		
		newME.end_add_event();
		
		newME.train(15,"gis");
		String model = "/home/tm/model_temp";
		
		newME.save(model);

		//
		MaximumEntropyJNI newME2 = new MaximumEntropyJNI();
		newME2.load(model);
		double prob = newME2.eval(context2,"B");
		System.out.println(prob);
		System.out.println(newME2.predict(context1));
		System.out.println(newME2.predict(context2));
		System.out.println(newME2.predict(context3));
		//
		HashMap<String, Double> probs = new HashMap<String, Double>();
		probs = newME2.eval_all(context3, false);
		System.out.println(probs);
		//
		int classNumber = newME2.getClassNumber();
		System.out.println(classNumber);
		String[] outcomes = new String[classNumber];
		double[] probs2 = new double[classNumber];
		newME2.eval_all(context1, outcomes, probs2);
		System.out.println(outcomes[0]+","+outcomes[1]+","+outcomes[2]);
		System.out.println(probs2[0]+","+probs2[1]+","+probs2[2]);
		
	}



	public static void multiMETest(){
		
	
		String[] context1= new String[2];
		context1[0] = "a";context1[1] = "a";
		String outcome1 = "A";
		int i = 1;
		String[] context2= new String[2];
		context2[0] = "b";context2[1] = "b";
		String outcome2 = "B";
		String[] context3= new String[2];
		context3[0] = "c";context3[1] = "c";
		String outcome3 = "C";
		
		String x = "x";
		String y = "y";
		
		String modelPath = "/home/tm/model";
		
		
		MultiMeJNI newME = new MultiMeJNI();	
		newME.print();
		
		newME.begin_add_event(x);
	
		newME.add_event(x, context1, outcome1, i);
		
		newME.begin_add_event(y);
		
		newME.add_event(y, context3, outcome3, i);
	
		newME.add_event(x, context2, outcome2, i);
		newME.add_event(x, context3, outcome3, i);
		newME.add_event(y, context2, outcome2, i);
		
		newME.end_add_event_all();
		
		newME.train_all(15,"gis");		
		
		newME.save_all(modelPath);
		
		MultiMeJNI newME2 = new MultiMeJNI();
		newME2.load_all(modelPath);
		int classNumber = newME2.getClassNumber(y);
		System.out.println(classNumber);
		String[] outcomes = new String[classNumber];
		double[] probs2 = new double[classNumber];
		newME2.eval_all(y, context1, outcomes, probs2);
		System.out.println(outcomes[0]+","+outcomes[1]);
		System.out.println(probs2[0]+","+probs2[1]);
		
		
	
		
	}
		

	public static void main(String[] args) {
		METest();
		multiMETest();
	}
	
	
}
