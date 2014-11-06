/**
 *
 */
package edu.hitsz.nlp.eisnerdep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



/**
 * �����������洢�Ͷ�ȡѵ������е�ͳ����Ϣ�����Ƶ�ʵȣ���(Eisner, 1996)
 * @author lee
 *
 */

public class StatMap {
	public HashMap<String,Integer> tempSonMap;
	public HashMap<String,Integer> tempParentMap;
	public HashMap<String,String> sonParentMap;
	public HashMap<String,Integer> parentDivMap;
	public HashMap<String,String> parentStatMap;
	//HashMap<String,Double> weightMap;

	public StatMap(){
		tempSonMap=new HashMap<String,Integer>();
		tempParentMap=new HashMap<String,Integer>();
		sonParentMap=new HashMap<String,String>();
		parentDivMap=new HashMap<String,Integer>();
		parentStatMap=new HashMap<String,String>();
		//weightMap=new HashMap<String,Double>();
	}


	/**
	 * ��ѵ���ļ��õ�����ļ��ĸ��ӽڵ�ͳ��
	 * @param outPath ��·��
	 * @param size ������ӵĸ���0��ȫ��������ļ���n��n������
	 */
	public void compLexLink(String outPath,String fileName,int size){
		Conll09File trainfile=new Conll09File();
		trainfile.readTrainFile(outPath,fileName,size);
		//�ݹ����о���
		int sentenceNumber=trainfile.sentenceNumber;
		for(int i=0;i<sentenceNumber;i++){
			//�ҵ�ÿ���ʵ��ӽڵ�
			if(i%1000==0)
				System.out.println("Statistical relative frequency of "+i+" Sentences have been done");
			int sentenceLength=trainfile.totalSentence.get(i).sentenceLength;
			for(int j=0;j<sentenceLength;j++){
				//��־λ��ʾ�Ƿ�Ϊ��ߵ�һ���ӽڵ�
				int leftSignal=0;
				//�Ӹô�j������
				String curTag=trainfile.totalSentence.get(i).pos[j];
				String curTagL=curTag+"-LEFT";
				String curTagR=curTag+"-RIGHT";
				String curForm=trainfile.totalSentence.get(i).form[j];
				String curWord=curForm+"+"+curTag;
				String leftStart="LEFTSTART";
				String leftStop="LEFTSTOP";
				String preTag=null;
				String preTagCurTag=null;
				String preTagCurWord=null;
				String leftTag=null;
				String leftForm=null;
				String leftTagPreTagCurWord=null;
				String leftTagPreTagCurTag=null;
				String leftWordPreTagCurWord=null;
				String leftWordPreTagCurTag=null;
				String leftWord=null;
				//�жϵ�ǰ�ڵ�ĸ��ڵ��Ƿ�ΪROOT
				if(trainfile.totalSentence.get(i).isFatherRoot(j)){
					//�洢P(t|Root)��tempSonMap,tmpParentMap,sonParentHash
					String rootForm=trainfile.totalSentence.get(i).form[sentenceLength];
					String curTagRootForm=curTag+"+"+rootForm;
					addMap(curTagRootForm,rootForm);
					addParentStatMap(rootForm, "ROOT");
					//�洢P(w|t,Root)��tempSonMap,tmpParentMap,sonParentHash
					String curWordRootForm=curWord+"+"+rootForm;
					addMap(curWordRootForm,curTagRootForm);
					addParentStatMap(curTagRootForm,"t-l,ROOT");
				}
				curWord=curForm+"+"+curTagL;
				for(int k=j;k>=0;k--){
					//�жϴ�k�Ƿ�Ϊ��j���ӽڵ�
					if(trainfile.totalSentence.get(i).isFather(j, k)){
						//������ڵ�j���ӽڵ�
						if(leftSignal==0){
							//�洢P(LeftStart|w,t)��tempSonMap,tmpParentMap,sonParentHash
							String leftStartCurWord=leftStart+"+"+curWord;
							addMap(leftStartCurWord,curWord);
							addParentStatMap(curWord,"w,t-l");
							//�洢P(LeftStart|t)��tempSonMap,tmpParentMap,sonParentHash
							String leftStartCurTag=leftStart+"+"+curTagL;
							addMap(leftStartCurTag,curTagL);
							addParentStatMap(curTagL,"t-l");
							preTag=leftStart;
							preTagCurTag=preTag+"+"+curTagL;
							preTagCurWord=preTag+"+"+curWord;
							//�洢P(tl|LeftStart,w,t)��tempSonMap,tmpParentMap,sonParentHash
							leftTag=trainfile.totalSentence.get(i).pos[k];
							leftForm=trainfile.totalSentence.get(i).form[k];
							leftTagPreTagCurWord=leftTag+"+"+preTagCurWord;
							addMap(leftTagPreTagCurWord,preTagCurWord);
							addParentStatMap(preTagCurWord,"LEFTSTART,w,t-l");
							//�洢P(tl|LeftStart,t)��tempSonMap,tmpParentMap,sonParentHash
							leftTagPreTagCurTag=leftTag+"+"+preTagCurTag;
							addMap(leftTagPreTagCurTag,preTagCurTag);
							addParentStatMap(preTagCurTag,"LEFTSTART,t-l");
							//�洢P(wl|tl,LeftStart,w,t)��tempSonMap,tmpParentMap,sonParentHash
							leftWordPreTagCurWord=leftForm+"+"+leftTagPreTagCurWord;
							addMap(leftWordPreTagCurWord,leftTagPreTagCurWord);
							addParentStatMap(leftTagPreTagCurWord,"tl,LEFTSTART,w,t-l");
							//�洢P(wl|tl,LeftStart,t)��tempSonMap,tmpParentMap,sonParentHash
							leftWordPreTagCurTag=leftForm+"+"+leftTagPreTagCurTag;
							addMap(leftWordPreTagCurTag,leftTagPreTagCurTag);
							addParentStatMap(leftTagPreTagCurTag,"tl,LEFTSTART,t-l");
							//�洢P(wl|tl)��tempSonMap,tmpParentMap,sonParentHash
							leftWord=leftForm+"+"+leftTag;
							addMap(leftWord,leftTag);
							addParentStatMap(leftTag,"tl");
							leftSignal=1;
						}
						else if(leftSignal==1){
							preTag=leftTag;
							preTagCurTag=preTag+"+"+curTagL;
							preTagCurWord=preTag+"+"+curWord;
							//�洢P(tl|tl,w,t)��tempSonMap,tmpParentMap,sonParentHash
							leftTag=trainfile.totalSentence.get(i).pos[k];
							leftForm=trainfile.totalSentence.get(i).form[k];
							leftTagPreTagCurWord=leftTag+"+"+preTagCurWord;
							addMap(leftTagPreTagCurWord,preTagCurWord);
							addParentStatMap(preTagCurWord,"tl,w,t-l");
							//�洢P(tl|tl,t)��tempSonMap,tmpParentMap,sonParentHash
							leftTagPreTagCurTag=leftTag+"+"+preTagCurTag;
							addMap(leftTagPreTagCurTag,preTagCurTag);
							addParentStatMap(preTagCurTag,"tl,t-l");
							//�洢P(wl|tl,tl,w,t)��tempSonMap,tmpParentMap,sonParentHash
							leftWordPreTagCurWord=leftForm+"+"+leftTagPreTagCurWord;
							addMap(leftWordPreTagCurWord,leftTagPreTagCurWord);
							addParentStatMap(leftTagPreTagCurWord,"tl,tl,w,t-l");
							//�洢P(wl|tl,tl,t)��tempSonMap,tmpParentMap,sonParentHash
							leftWordPreTagCurTag=leftForm+"+"+leftTagPreTagCurTag;
							addMap(leftWordPreTagCurTag,leftTagPreTagCurTag);
							addParentStatMap(leftTagPreTagCurTag,"tl,tl,t-l");
							//�洢P(wl|tl)��tempSonMap,tmpParentMap,sonParentHash
							leftWord=leftForm+"+"+leftTag;
							addMap(leftWord,leftTag);
							addParentStatMap(leftTag,"tl");
						}
					}
				}
				if(leftSignal==1){
					preTag=leftTag;
					preTagCurTag=preTag+"+"+curTagL;
					preTagCurWord=preTag+"+"+curWord;
					String leftStopPreTagCurWord=leftStop+"+"+preTagCurWord;
					String leftStopPreTagCurTag=leftStop+"+"+preTagCurTag;
					//�洢P(LeftStop|tl,w,t)��tempSonMap,tmpParentMap,sonParentHash
					addMap(leftStopPreTagCurWord,preTagCurWord);
					addParentStatMap(preTagCurWord,"tl,w,t-l");
					//�洢P(LeftStop|tl,t)��tempSonMap,tmpParentMap,sonParentHash
					addMap(leftStopPreTagCurTag,preTagCurTag);
					addParentStatMap(preTagCurTag,"tl,t-l");
				}
				int rightSignal=0;
				String rightStart="RIGHTSTART";
				String rightStop="RIGHTSTOP";
				String rightTag=null;
				String rightForm=null;
				String rightTagPreTagCurWord=null;
				String rightTagPreTagCurTag=null;
				String rightWordPreTagCurWord=null;
				String rightWordPreTagCurTag=null;
				String rightWord=null;
				curWord=curForm+"+"+curTagR;
				for(int k=j;k<sentenceLength;k++){
					//�жϴ�k�Ƿ�Ϊ��j���ӽڵ�
					if(trainfile.totalSentence.get(i).isFather(j, k)){
						//������ڵ�j���ӽڵ�
						if(rightSignal==0){
							//�洢P(rightStart|w,t)��tempSonMap,tmpParentMap,sonParentHash
							String rightStartCurWord=rightStart+"+"+curWord;
							addMap(rightStartCurWord,curWord);
							addParentStatMap(curWord,"w,t-r");
							//�洢P(rightStart|t)��tempSonMap,tmpParentMap,sonParentHash
							String rightStartCurTag=rightStart+"+"+curTagR;
							addMap(rightStartCurTag,curTagR);
							addParentStatMap(curTagR,"t-r");
							preTag=rightStart;
							preTagCurTag=preTag+"+"+curTagR;
							preTagCurWord=preTag+"+"+curWord;
							//�洢P(tl|rightStart,w,t)��tempSonMap,tmpParentMap,sonParentHash
							rightTag=trainfile.totalSentence.get(i).pos[k];
							rightForm=trainfile.totalSentence.get(i).form[k];
							rightTagPreTagCurWord=rightTag+"+"+preTagCurWord;
							addMap(rightTagPreTagCurWord,preTagCurWord);
							addParentStatMap(preTagCurWord,"RIGHTSTART,w,t-r");
							//�洢P(tl|rightStart,t)��tempSonMap,tmpParentMap,sonParentHash
							rightTagPreTagCurTag=rightTag+"+"+preTagCurTag;
							addMap(rightTagPreTagCurTag,preTagCurTag);
							addParentStatMap(preTagCurTag,"RIGHTSTART,t-r");
							//�洢P(wl|tr,rightStart,w,t)��tempSonMap,tmpParentMap,sonParentHash
							rightWordPreTagCurWord=rightForm+"+"+rightTagPreTagCurWord;
							addMap(rightWordPreTagCurWord,rightTagPreTagCurWord);
							addParentStatMap(rightTagPreTagCurWord,"tr,RIGHTSTART,w,t-r");
							//�洢P(wl|tr,rightStart,t)��tempSonMap,tmpParentMap,sonParentHash
							rightWordPreTagCurTag=rightForm+"+"+rightTagPreTagCurTag;
							addMap(rightWordPreTagCurTag,rightTagPreTagCurTag);
							addParentStatMap(rightTagPreTagCurTag,"tr,RIGHTSTART,t-r");
							//�洢P(wl|tr)��tempSonMap,tmpParentMap,sonParentHash
							rightWord=rightForm+"+"+rightTag;
							addMap(rightWord,rightTag);
							addParentStatMap(rightTag,"tr");
							rightSignal=1;
						}
						else if(rightSignal==1){
							preTag=rightTag;
							preTagCurTag=preTag+"+"+curTagR;
							preTagCurWord=preTag+"+"+curWord;
							//�洢P(tl|tr,w,t)��tempSonMap,tmpParentMap,sonParentHash
							rightTag=trainfile.totalSentence.get(i).pos[k];
							rightForm=trainfile.totalSentence.get(i).form[k];
							rightTagPreTagCurWord=rightTag+"+"+preTagCurWord;
							addMap(rightTagPreTagCurWord,preTagCurWord);
							addParentStatMap(preTagCurWord,"tr,w,t-r");
							//�洢P(tl|tr,t)��tempSonMap,tmpParentMap,sonParentHash
							rightTagPreTagCurTag=rightTag+"+"+preTagCurTag;
							addMap(rightTagPreTagCurTag,preTagCurTag);
							addParentStatMap(preTagCurTag,"tr,t-r");
							//�洢P(wl|tr,tr,w,t)��tempSonMap,tmpParentMap,sonParentHash
							rightWordPreTagCurWord=rightForm+"+"+rightTagPreTagCurWord;
							addMap(rightWordPreTagCurWord,rightTagPreTagCurWord);
							addParentStatMap(rightTagPreTagCurWord,"tr,tr,w,t-r");
							//�洢P(wl|tr,tr,t)��tempSonMap,tmpParentMap,sonParentHash
							rightWordPreTagCurTag=rightForm+"+"+rightTagPreTagCurTag;
							addMap(rightWordPreTagCurTag,rightTagPreTagCurTag);
							addParentStatMap(rightTagPreTagCurTag,"tr,tr,t-r");
							//�洢P(wl|tr)��tempSonMap,tmpParentMap,sonParentHash
							rightWord=rightForm+"+"+rightTag;
							addMap(rightWord,rightTag);
							addParentStatMap(rightTag,"tr");
						}
					}
				}
				if(rightSignal==1){
					preTag=rightTag;
					preTagCurTag=preTag+"+"+curTagR;
					preTagCurWord=preTag+"+"+curWord;
					String rightStopPreTagCurWord=rightStop+"+"+preTagCurWord;
					String rightStopPreTagCurTag=rightStop+"+"+preTagCurTag;
					//�洢P(rightStop|tr,w,t)��tempSonMap,tmpParentMap,sonParentHash
					addMap(rightStopPreTagCurWord,preTagCurWord);
					addParentStatMap(preTagCurWord,"tr,w,t-r");
					//�洢P(rightStop|tr,t)��tempSonMap,tmpParentMap,sonParentHash
					addMap(rightStopPreTagCurTag,preTagCurTag);
					addParentStatMap(preTagCurTag,"tr,t-r");
				}
			}//�������
		}//����ļ������о���
		//�õ�����ʽ�Ҳ��ֵ�diversity,��˼��ͬһ���Ҳ��ֳ��ֵ�Ƶ��

		int tempNumber;
		Iterator<Entry<String,String>> iter=parentStatMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String,String> entry = iter.next();
		    String parent = entry.getValue();
			if(parentDivMap.get(parent)==null)
				parentDivMap.put(parent, 1);
			else{
				tempNumber=parentDivMap.get(parent);
				parentDivMap.remove(parent);
				tempNumber++;
				parentDivMap.put(parent,tempNumber);
			}
		}

		/*
		//�õ�����ʽ�Ҳ��ֵ�diversity,��˼��ͬһ���Ҳ��ֶ�Ӧ���󲿷ֵ���Ŀ
		int tempNumber;
		Iterator iter=sonParentMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String parent = entry.getValue().toString();
			if(parentDivMap.get(parent)==null)
				parentDivMap.put(parent, 1);
			else{
				tempNumber=parentDivMap.get(parent);
				parentDivMap.remove(parent);
				tempNumber++;
				parentDivMap.put(parent,tempNumber);
			}
		}
		*/
		/*
		//�õ����е�Ƶ��
		Iterator iter=sonParentMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String key = entry.getKey().toString();
		    String val = entry.getValue().toString();
		    double ratio=Double.parseDouble(tempSonMap.get(key).toString())/Double.valueOf(tempParentMap.get(val).toString());
		    weightMap.put(key, ratio);
		}
		*/
	}

	/**
	 * �ݹ�������ӣ��õ�ÿ�����ӣ�ͷ�ڵ���β�ڵ㣩��Ȩֵ���洢��Map�ļ�
	 * ���õ���Eisner 1996 Model 3������relative frequency
	 * �����ʻ���Ϣ
	 */
	public void storeLexLink(String outPath){
		//���Map���ļ�
		Iterator iter;
		//���Map���ļ�
		String sonFile=outPath+"sonStat.txt";
		String parentFile=outPath+"parentStat.txt";
		String sonParentFile=outPath+"sonParentStat.txt";
		String parentDivFile=outPath+"parentDivStat.txt";
		//String weightFile=outPath+inPath+"weightStat.txt";
		String tempString=null;
		try{
			FileWriter sonFileWriter=new FileWriter(sonFile);
			iter=tempSonMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    sonFileWriter.write(tempString);
			}
			sonFileWriter.close();
			System.out.println("sonStatFile has been stored");
			FileWriter parentFileWriter=new FileWriter(parentFile);
			iter=tempParentMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    parentFileWriter.write(tempString);
			}
			parentFileWriter.close();
			System.out.println("parentStatFile has been stored");
			/*
			FileWriter sonParentFileWriter=new FileWriter(sonParentFile);
			iter=sonParentMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    sonParentFileWriter.write(tempString);
			}
			sonParentFileWriter.close();
			System.out.println("sonParentFile has been stored");
			*/
			FileWriter parentDivWriter=new FileWriter(parentDivFile);
			iter=parentDivMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    parentDivWriter.write(tempString);
			}
			parentDivWriter.close();
			System.out.println("parentDivFile has been stored");
			/*
			FileWriter sonFreqWriter=new FileWriter(weightFile);
			iter=weightMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    sonFreqWriter.write(tempString);
			}
			sonFreqWriter.close();
			*/
		}catch (Exception e) {
            e.printStackTrace();
		}
		System.out.println("Relative Frequency finished.");

	}

	/**
	 * ��ͳ���ļ��ж�ȡͳ����Ϣ��HashMap��
	 * @param outPath
	 */
	public void readLexLink(String outPath){
		String tempString=null;
		BufferedReader reader = null;
		try{
			//���SonMap�ļ���Map
			String sonFile=outPath+"sonStat.txt";
			reader = new BufferedReader(new FileReader(new File(sonFile)));
			try{
				while ((tempString = reader.readLine()) != null){
					String[] word=tempString.split("\t");
					String son=word[0];
					int sonStat=Integer.parseInt(word[1]);
					tempSonMap.put(son,sonStat);
				}
				try{
					reader.close();
					}catch (IOException e) {
						e.printStackTrace();
				}
				System.out.println("read tempSonMap successed");
			}catch (FileNotFoundException e) {
				System.err.println(e);
			}
			//���ParentMap�ļ���Map
			String parentFile=outPath+"parentStat.txt";
			reader = new BufferedReader(new FileReader(new File(parentFile)));
			try{
				while ((tempString = reader.readLine()) != null){
					String[] word=tempString.split("\t");
					String parent=word[0];
					int parentStat=Integer.parseInt(word[1]);
					tempParentMap.put(parent,parentStat);
				}
				try{
					reader.close();
					}catch (IOException e) {
						e.printStackTrace();
				}
				System.out.println("read tempParentMap successed");
			}catch (FileNotFoundException e) {
				System.err.println(e);
			}
			/*
			//���sonParentMap�ļ���Map
			String sonParentFile=outPath+inPath+"sonParentStat.txt";
			reader = new BufferedReader(new FileReader(new File(sonParentFile)));
			try{
				while ((tempString = reader.readLine()) != null){
					String[] word=tempString.split("\t");
					String son=word[0];
					String parent=word[1];
					sonParentMap.put(son,parent);
				}
				try{
					reader.close();
					}catch (IOException e) {
						e.printStackTrace();
				}
				System.out.println("read sonParentMap successed");
			}catch (FileNotFoundException e) {
				System.err.println(e);
			}
			*/
			//���parentDivMap�ļ���Map
			String parentDivFile=outPath+"parentDivStat.txt";
			reader = new BufferedReader(new FileReader(new File(parentDivFile)));
			try{
				while ((tempString = reader.readLine()) != null){
					String[] word=tempString.split("\t");
					String parent=word[0];
					int parentDiv=Integer.parseInt(word[1]);
					parentDivMap.put(parent,parentDiv);
				}
				try{
					reader.close();
					}catch (IOException e) {
						e.printStackTrace();
				}
			}catch (FileNotFoundException e) {
				System.err.println(e);
			}
			System.out.println("read parentDivMap successed");
		}catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	//����Map������ȷ��
	public void outputMap(){
		Iterator iter;
		/*
		System.out.println("\n\n\nThis is tempSonMap.");
		iter=tempSonMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
		    Object val = entry.getValue();
		    System.out.println(key+"\t"+val);
		}
		System.out.println("\n\n\nThis is tempParentMap.");
		iter=tempParentMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
		    Object val = entry.getValue();
		    System.out.println(key+"\t"+val);
		}
		System.out.println("\n\n\nThis is sonParentMap.");
		iter=sonParentMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
		    Object val = entry.getValue();
		    System.out.println(key+"\t"+val);
		}
		System.out.println("\n\n\nThis is parentDivMap.");
		*/
		iter=parentDivMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
		    Object val = entry.getValue();
		    System.out.println(key+"\t"+val);
		}
		System.out.println();
	}



	/**
	 * ���ӽڵ��ַ����ڵ��ַ����ӽڵ��Ӧ��ϵ�ֱ�洢��tempSonMap,tempParentMap,sonParentMap
	 * @param temSon
	 * @param temParent
	 */
	void addMap(String temSon, String temParent){
		int tempNumber;
		if(tempSonMap.get(temSon)==null)
			tempSonMap.put(temSon, 1);
		else{
			tempNumber=tempSonMap.get(temSon);
			tempSonMap.remove(temSon);
			tempNumber++;
			tempSonMap.put(temSon,tempNumber);
		}
		if(tempParentMap.get(temParent)==null)
			tempParentMap.put(temParent, 1);
		else{
			tempNumber=tempParentMap.get(temParent);
			tempParentMap.remove(temParent);
			tempNumber++;
			tempParentMap.put(temParent,tempNumber);
		}
		//if(sonParentMap.get(temSon)==null)
			//sonParentMap.put(temSon,temParent);
	}

	/**
	 * ���ӽڵ��ַ����ӽڵ��Ӧ��ϵ�ֱ�洢��tempSonMap,sonParentMap
	 * @param temSon
	 * @param temParent
	 */
	void addMapNoParent(String temSon, String temParent){
		int tempNumber;
		if(tempSonMap.get(temSon)==null)
			tempSonMap.put(temSon, 1);
		else{
			tempNumber=tempSonMap.get(temSon);
			tempSonMap.remove(temSon);
			tempNumber++;
			tempSonMap.put(temSon,tempNumber);
		}
		if(sonParentMap.get(temSon)==null)
			sonParentMap.put(temSon,temParent);
	}

	void addParentStatMap(String str, String stat){
		if(parentStatMap.get(str)==null)
			parentStatMap.put(str,stat);
	}

	/**
	 * �õ��ַ���tempSonMap��Ӧ�Ĵ���ѵ��������ֵĴ���
	 * @param son
	 * @return
	 */
	int getSonNum(String son){
		int num;
		if(tempSonMap.get(son)!=null){
			num=tempSonMap.get(son);
			return num;
		}
		return 0;
	}

	/**
	 * �õ��ַ���tempParentMap��Ӧ�Ĵ���ѵ��������ֵĴ���
	 * @param parent
	 * @return
	 */
	int getParentNum(String parent){
		int num;
		if(tempParentMap.get(parent)!=null){
			num=tempParentMap.get(parent);
			return num;
		}
		return 0;
	}

	/**
	 * �õ��ַ���tempParentMap��Ӧ�Ĵ���ѵ��������ֵĴ���
	 * @param parent
	 * @return
	 */
	int getParentDiv(String parent){
		int num;
		if(parentDivMap.get(parent)!=null){
			num=parentDivMap.get(parent);
			return num;
		}
		return 0;
	}


	static void test(){
		StatMap newMap=new StatMap();
		String path="E:\\codespace\\workspace\\laparser\\";
		String trainName="CoNLL2009-ST-English-train.txt";
		String trainNameP="CoNLL2009-ST-English-train-preprocess.txt";
		newMap.compLexLink(path, trainName, 0);
		newMap.storeLexLink(path);
	}

	public static void main(String[] args){
		//test();
	}



}