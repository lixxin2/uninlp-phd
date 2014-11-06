package edu.hitsz.nlp.pinyin2character;

import java.util.ArrayList;

import edu.hitsz.ml.maxent.jni.MultiMeJNI;

public class Py2CharME {
	public String trainName;
	public String modelPath;
	public String testName;
	public String predictName;
	public String goldName;
	public PyCharReader reader;
	public PyCharWriter writer;
	
	public void train( ) {
		MultiMeJNI models = new MultiMeJNI();
		reader = new PyCharReader(trainName);
		PyCharInstance instance = reader.getNext();
		
		int sentenceNumber = 0;
		
		while(instance != null) {
			
			sentenceNumber++;

			if(sentenceNumber%100 == 0) {
				System.out.println(sentenceNumber + "...");
			}
		
			int length = instance.length;
			
			for(int i=0; i<length; i++) {
				
				ArrayList<String> features = instance.getFeature(i);
				String pinyin = instance.characterYins[i];
				String form = instance.characters[i];
				
				models.begin_add_event(pinyin);
				models.add_event(pinyin, features, form, 1);

			}
			
			instance = reader.getNext();
		}
		
		models.end_add_event_all();
		models.train_all(15, "gis");
		models.save_all(modelPath);
		
	}
	
	public void predict(){
		MultiMeJNI models = new MultiMeJNI();
		models.load_all(modelPath);
		
		writer = new PyCharWriter(predictName);
		
		reader = new PyCharReader(testName);
		PyCharInstance instance = reader.getNextPinyin();
		
		int sentenceNumber = 0;
		
		System.out.println("starting predict");
		
		while(instance != null) {
			
			sentenceNumber++;

			System.out.println(sentenceNumber + "...");
			if(sentenceNumber%100 == 0) {
				System.out.println(sentenceNumber + "...");
			}
		
			int length = instance.length;
			String[] forms = new String[length]; 
			for(int i=0; i<length; i++) {
				
				ArrayList<String> features = instance.getFeature(i);
				String pinyin = instance.characterYins[i];
				
				forms[i] = models.predict(pinyin, features);

			}
			instance.characters = forms;		
			writer.writeInstanceFrom(instance);
			
			instance = reader.getNextPinyin();
			
		}
		writer.finishWriting();
		
	}
	
	
	
	
	
	
	public static void main(String[] args){
		


		Py2CharME pychar = new Py2CharME();
		pychar.trainName = "/home/tm/disk/disk1/pinyin2character/train-UTF-8";
		pychar.modelPath = "/home/tm/disk/disk1/pinyin2character/model";
		pychar.testName = "/home/tm/disk/disk1/pinyin2character/test-UTF-8";
		pychar.predictName = "/home/tm/disk/disk1/pinyin2character/predict-UTF-8";
		pychar.goldName = "/home/tm/disk/disk1/pinyin2character/gold-UTF-8";
		//pychar.train();
		pychar.predict();
		
		
	}
	
	
	

}
