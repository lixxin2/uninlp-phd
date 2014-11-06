/**
 *
 */
package edu.hitsz.nlp.coref;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.stemming.PorterStemmer;
import edu.hitsz.nlp.struct.CfgNode;
import edu.hitsz.nlp.struct.CfgTreeHead;
import edu.hitsz.util.StringMatch;

/**
 * @author tm
 *
 */
public class Conll11Document {

	private String documentName;
	private String partName;
	private String partNumber;
	private int sentenceNumber;
	private ArrayList<Conll11Sentence> totalSentence;
	private ArrayList<CoreferenceNode> totalCoreference;

	public Conll11Document(){
		sentenceNumber = 0;
		totalSentence = new ArrayList<Conll11Sentence>();
		totalCoreference = new ArrayList<CoreferenceNode>();
	}


	public String getDocumentName(){
		return this.documentName;
	}
	public void setDocumentName(String documentName){
		this.documentName = documentName;
	}
	public String getPartName(){
		return this.partName;
	}
	public void setPartName(String partName){
		this.partName = partName;
	}
	public String getPartNumber(){
		return this.partNumber;
	}
	public void setPartNumber(String partNumber){
		this.partNumber = partNumber;
	}
	public int getSentenceNumber(){
		return this.sentenceNumber;
	}
	public void setSentenceNumber(int sentenceNumber){
		this.sentenceNumber = sentenceNumber;
	}
	public ArrayList<Conll11Sentence> getTotalSentence(){
		return this.totalSentence;
	}
	public Conll11Sentence getSentence(int i){
		return this.totalSentence.get(i);
	}
	public ArrayList<CoreferenceNode> getTotalCoreference(){
		return this.totalCoreference;
	}

	public void addSentence(Conll11Sentence newSentence){
		this.totalSentence.add(newSentence);
		this.sentenceNumber++;
	}




	/**
	 * Preprocess coreferences in document,
	 *     basically copying coreference in sentences to docucument,
	 *     and placing them in order in a ArrayList.
	 * Call by readTrainFile and readTestFile methods in Conll11File class
	 */
	public void preProcessCoreference(){
		if(sentenceNumber < 0){
			System.out.println("The number of sentences in document "+documentName+" is 0");
			System.exit(0);
		}
		for(int i=0; i<sentenceNumber; i++){
			int curNumber = this.totalSentence.get(i).getCoreferenceSize();
			if(curNumber > 0)
				totalCoreference.addAll(this.totalSentence.get(i).getCoreference());
		}
	}


	/**
	 * select preceding coreference which is closest
	 * @param probs
	 */
	public void postProcessCoreferenceClosest(ArrayList<ArrayList<Double>> probs){
		int probsNumber = probs.size();
		if(probsNumber != totalCoreference.size()){
			System.out.println("probsNumber"+probsNumber);
			System.out.println("totalCoreference.size:"+totalCoreference.size());
			System.out.println("the coreferenc number is not equal");
			System.exit(1);
		}
		for(int i=0; i<probsNumber; i++)
			this.totalCoreference.get(i).setLabel(-1);
		int label = 1;
		Double limited = 0.5;
		if(probsNumber > 1){
			for(int i=1; i< probsNumber; i++){
				for(int j=i-1; j>=0; j--){
					if(this.totalCoreference.get(j).isNotTouched(this.totalCoreference.get(i))
							&& probs.get(i).get(j) > limited){
						if(this.totalCoreference.get(i).getLabel() == -1){
							if(this.totalCoreference.get(j).getLabel() == -1){
								this.totalCoreference.get(j).setLabel(label);
								this.totalCoreference.get(i).setLabel(label++);
							}
							else
								this.totalCoreference.get(i).setLabel(this.totalCoreference.get(j).getLabel());
						}
						else{
							if(this.totalCoreference.get(j).getLabel() == -1)
								this.totalCoreference.get(j).setLabel(this.totalCoreference.get(i).getLabel());
						}
						break;
					}
				}
			}
		}
		removeExtraCoreference();
		setCoreferenceLabel();
	}


	/**
	 * select preceding coreference which has highest probability
	 * @param probs
	 */
	public void postProcessCoreferenceHighest(ArrayList<ArrayList<Double>> probs){
		int probsNumber = probs.size();
		if(probsNumber != totalCoreference.size()){
			System.out.println("probsNumber"+probsNumber);
			System.out.println("totalCoreference.size:"+totalCoreference.size());
			System.out.println("the coreferenc number is not equal");
			System.exit(1);
		}
		for(int i=0; i<probsNumber; i++)
			this.totalCoreference.get(i).setLabel(-1);
		int label = 1;
		//0.38 for gold mention,
		Double limited = 0.50;
		if(probsNumber > 1){
			for(int i=1; i< probsNumber; i++){
				int sig = -1;
				double max = limited;
				for(int j=i-1; j>=0; j--){
					if(this.totalCoreference.get(j).isNotTouched(this.totalCoreference.get(i))
							&& probs.get(i).get(j) > max){
							max = probs.get(i).get(j);
							sig = j;
					}
				}
				if(sig > -1){
					int j=sig;
					if(this.totalCoreference.get(i).getLabel() == -1){
						if(this.totalCoreference.get(j).getLabel() == -1){
							this.totalCoreference.get(j).setLabel(label);
							this.totalCoreference.get(i).setLabel(label++);
						}
						else
							this.totalCoreference.get(i).setLabel(this.totalCoreference.get(j).getLabel());
					}
					else{
						if(this.totalCoreference.get(j).getLabel() == -1)
							this.totalCoreference.get(j).setLabel(this.totalCoreference.get(i).getLabel());
					}
				}
			}
		}
		removeExtraCoreference();
		setCoreferenceLabel();
	}


	public void postProcessCoreferenceAllPrecedent(ArrayList<ArrayList<Double>> probs){
		int probsNumber = probs.size();
		if(probsNumber != totalCoreference.size()){
			System.out.println("probsNumber"+probsNumber);
			System.out.println("totalCoreference.size:"+totalCoreference.size());
			System.out.println("the coreferenc number is not equal");
			System.exit(1);
		}
		for(int i=0; i<probsNumber; i++)
			this.totalCoreference.get(i).setLabel(-1);
		int label = 1;
		Double limited = 0.50;
		if(probsNumber > 1){
			for(int i=1; i< probsNumber; i++){
				for(int j=i-1; j>=0; j--){
					if(this.totalCoreference.get(j).isNotTouched(this.totalCoreference.get(i))
							&& probs.get(i).get(j) > limited){
						if(this.totalCoreference.get(i).getLabel() == -1){
							if(this.totalCoreference.get(j).getLabel() == -1){
								this.totalCoreference.get(j).setLabel(label);
								this.totalCoreference.get(i).setLabel(label++);
							}
							else
								this.totalCoreference.get(i).setLabel(this.totalCoreference.get(j).getLabel());
						}
						else{
							if(this.totalCoreference.get(j).getLabel() == -1)
								this.totalCoreference.get(j).setLabel(this.totalCoreference.get(i).getLabel());
						}
					}
				}
			}
		}
		//postProcessCoreferenceStringMatch();
		removeExtraCoreference();
		setCoreferenceLabel();
	}


	/**
	 * post processing:
	 * set the coreference node which is not annotated, same label as the node
	 * which is string match with previous un-annotated node.
	 * Experiments shows that is increase the recall but decrease the precision
	 */
	public void postProcessCoreferenceStringMatch(){
		int corefNumber = this.totalCoreference.size();
		for(int i=0; i<corefNumber; i++){
			CoreferenceNode curNode = this.totalCoreference.get(i);
			if(curNode.getLabel() == -1
					&& !(curNode.getStart()==curNode.getEnd()
							&& this.getSentence(curNode.getSentenceNumber()).getPOS(curNode.getStart()).startsWith("PRP"))){
				String curString ="";
				for(int j=curNode.getStart(); j<=curNode.getEnd(); j++)
					curString += "+"+this.getSentence(curNode.getSentenceNumber()).getForm(j).toLowerCase();
				int sig = 0;
				for( int j=i-1; j>=0; j--){
					CoreferenceNode otherNode = this.totalCoreference.get(j);
					if(otherNode.getLabel() != -1
							&& !(otherNode.getStart()==otherNode.getEnd()
									&& this.getSentence(otherNode.getSentenceNumber()).getPOS(otherNode.getStart()).startsWith("PRP"))){
						String otherString = "";
						for(int k=otherNode.getStart(); k<=otherNode.getEnd(); k++)
							otherString += "+"+this.getSentence(otherNode.getSentenceNumber()).getForm(k).toLowerCase();
						if(curString.equals(otherString)){
							curNode.setLabel(otherNode.getLabel());
							sig =1;
							break;
						}
					}
				}
				if (sig ==0 ){
					for( int j=i+1; j<corefNumber; j++){
						CoreferenceNode otherNode = this.totalCoreference.get(j);
						if(otherNode.getLabel() != -1
								&& !(otherNode.getStart()==otherNode.getEnd()
										&& this.getSentence(otherNode.getSentenceNumber()).getPOS(otherNode.getStart()).startsWith("PRP"))){
							String otherString = "";
							for(int k=otherNode.getStart(); k<=otherNode.getEnd(); k++)
								otherString += "+"+this.getSentence(otherNode.getSentenceNumber()).getForm(k).toLowerCase();
							if(curString.equals(otherString)){
								curNode.setLabel(otherNode.getLabel());
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Convert the coreference probs m*m into lp_solver lp format, and store in a file
	 * @param probs
	 * @param lpName
	 */
	public void storeCoreferenceProbLpsolverlp(ArrayList<ArrayList<Double>> probs, String lpName){
		int probNumber = probs.size();
		if(probNumber != this.totalCoreference.size()) {
			System.out.println("corefNumber is different from probNumber");
			System.exit(-1);
		}
		try{
			FileWriter outFileWriter=new FileWriter(lpName);
			StringBuffer function = new StringBuffer();
			StringBuffer constraints = new StringBuffer();
			StringBuffer variables = new StringBuffer();
			for(int i=1; i<probNumber; i++){
				for(int j=0; j<i; j++){
					double prob = -1 * Math.log(probs.get(i).get(j));
					double vProb = -1 * Math.log(1-probs.get(i).get(j));
					//Ci,j * Xi,j + CCi,j * (1-Xi,j)
					//in lp format:
					//function.append(prob+" "+"x"+i+"x"+j+" "+"+"+vProb+" "+"-"+vProb+" "+"x"+i+"x"+j+" ");
					//(Ci,j - CCi,j) * Xi,j + CCi,j
					double tmp = prob-vProb;
					if(tmp > 0.0)
						function.append("+"+" "+Double.toString(tmp)+" x"+i+"x"+j+" ");
					else
						function.append("-"+" "+Double.toString(tmp).substring(1)+" x"+i+"x"+j+" ");
					variables.append(","+" "+"x"+i+"x"+j);
				}
			}
			outFileWriter.write("min: "+function.toString().substring(2).trim()+";\n");
			System.out.println("corefNumber"+ probNumber);
			for(int i=0; i<probNumber; i++){
				System.out.print(i+"...");
				for(int j=i+1; j<probNumber; j++){
					for(int k=j+1; k<probNumber; k++){
						//Xi,k >= Xi,j + Xj,k - 1
						outFileWriter.write("x"+i+"x"+k+" - "+"x"+i+"x"+j+" - "+"x"+j+"x"+k+" >= -1;"+"\n"
							+ "x"+i+"x"+j+" - "+"x"+i+"x"+k+" - "+"x"+j+"x"+k+" >= -1;"+"\n"
							+ "x"+j+"x"+k+" - "+"x"+i+"x"+j+" - "+"x"+i+"x"+k+" >= -1;"+"\n");
					}
				}
			}

			outFileWriter.write("bin "+variables.toString().substring(2)+";\n");
			outFileWriter.close();
			System.out.println("\nStore lp file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	/**
	 * Convert the coreference probs m*m into Cplex lp format, and store in a file
	 * @param probs
	 * @param lpDir
	 */
	public void storeCoreferenceProbCplexlp(ArrayList<ArrayList<Double>> probs, String lpName){
		int probNumber = probs.size();
		if(probNumber != this.totalCoreference.size()) {
			System.out.println("corefNumber is different from probNumber");
			System.exit(-1);
		}
		try{
			FileWriter outFileWriter=new FileWriter(lpName);
			StringBuffer function = new StringBuffer();
			StringBuffer constraints = new StringBuffer();
			StringBuffer variables = new StringBuffer();
			for(int i=1; i<probNumber; i++){
				for(int j=0; j<i; j++){
					//Ci,j and CCi,j
					double prob = -1 * Math.log(probs.get(i).get(j));
					double vProb = -1 * Math.log(1-probs.get(i).get(j));
					//Ci,j * Xi,j + CCi,j * (1-Xi,j)
					//in lp format:
					//function.append(prob+" "+"x"+i+"x"+j+" "+"+"+vProb+" "+"-"+vProb+" "+"x"+i+"x"+j+" ");
					//(Ci,j - CCi,j) * Xi,j + CCi,j
					double tmp = prob-vProb;
					if(tmp > 0.0)
						function.append("+"+" "+Double.toString(tmp)+" x"+i+"x"+j+" ");
					else
						function.append("-"+" "+Double.toString(tmp).substring(1)+" x"+i+"x"+j+" ");
					variables.append("x"+i+"x"+j+"\n");
				}
			}
			outFileWriter.write("Minimize\nvalue: ");
			outFileWriter.write(function.toString().substring(2).trim()+"\n\n");
			outFileWriter.write("Subject To\n");
			System.out.println("corefNumber"+ probNumber);
			int st = 1;
			int nearDistance = 10000; //only output the node whose distance less than 100, set infinity as all
			double lowProb = 0.08;   //cancel the node whose probability less than 0.1
			boolean setCons = false; // add xij+xik < 1
			boolean setZero = true; //add x(i,j)=0 if p(i,j)<p*;
			for(int i=0; i<probNumber; i++){
				System.out.print(i+"...");
				/*
				for(int j=i+1; j<probNumber; j++){
					for(int k=j+1; k<probNumber; k++){
					*/
				int jMax = Math.min(probNumber, i+nearDistance);
				for(int j=i+1; j<jMax; j++){
					int kMax = Math.min(probNumber, j+nearDistance);
					for(int k=j+1; k<jMax; k++){
						//Xi,k >= Xi,j + Xj,k - 1
						String xij = "x"+j+"x"+i;
						String xjk = "x"+k+"x"+j;
						String xik = "x"+k+"x"+i;
						if(probs.get(j).get(i)>lowProb){
							if( probs.get(k).get(i)>lowProb) {
								if(probs.get(k).get(j)>lowProb) {
									outFileWriter.write("c"+(st++)+": "+xik+" - "+xij+" - "+xjk+" >= -1\n"
											+"c"+(st++)+": " +xij+" - "+xik+" - "+xjk+" >= -1\n"
											+"c"+(st++)+": " +xjk+" - "+xij+" - "+xik+" >= -1\n");
								}
								else{
									if(setCons) outFileWriter.write("c"+(st++)+": "+xij+" + "+xik+" =< 1\n");
									if(setZero) outFileWriter.write("c"+(st++)+": "+xjk+" = 0\n");
								}
							}
							else{
								if(probs.get(k).get(j)>lowProb){
									if(setCons) outFileWriter.write("c"+(st++)+": "+xij+" + "+xjk+" =< 1\n");
									if(setZero) outFileWriter.write("c"+(st++)+": "+xik+" = 0\n");
								}
							}
						}
						else{
							if( probs.get(k).get(i)>lowProb) {
								if(probs.get(k).get(j)>lowProb) {
									if(setCons) outFileWriter.write("c"+(st++)+": "+xik+" + "+xjk+" =< 1\n");
									if(setZero) outFileWriter.write("c"+(st++)+": "+xij+" = 0\n");
								}
							}
							else {
							}
						}
					}
				}
			}
			outFileWriter.write("\nbinary\n");
			outFileWriter.write(variables.toString());
			outFileWriter.write("End\n");
			outFileWriter.close();
			System.out.println("\nStore cplex lp file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}



	/**
	 * load head for cfg tree, according to CfgTreeHead class, which read headrules file for specified languages
	 * @param newTreeHead
	 */
	public void findHead(CfgTreeHead newTreeHead){
		for(int i=0; i<this.sentenceNumber; i++){
			this.totalSentence.get(i).findHead(newTreeHead);
		}
	}

	/**
	 * generate training sample for coreference resolution (Soon, 2001)
	 * @param newWriter
	 * @throws IOException
	 */
	public void extractEnglishTrainFeatures(ArrayList<ArrayList<String>> features, ArrayList<String> outcomes, Gender newGender, WordNet newNet){
		int corefNumber = this.totalCoreference.size();
		for(int i=1; i<corefNumber; i++){
			CoreferenceNode curNode = this.totalCoreference.get(i);
			int sig = -1;
			//find its coreferent node
			for(int j=i-1; j>=0; j--){
				CoreferenceNode corefNode = this.totalCoreference.get(j);
				if(corefNode.isLabelSame(curNode)){
					features.add(this.extractEnglishFeatures(curNode, corefNode, newGender, newNet));
					outcomes.add("IDENT");
					sig = j;
					break;
				}
			}
			//find these nodes between the node and its coreferent node
			int k = sig + 1;
			if( k > 0 && k < i){
				for(; k<i; k++){
					CoreferenceNode middleNode = this.totalCoreference.get(k);
					if(!middleNode.isLabelSame(curNode)){
						features.add(this.extractEnglishFeatures(curNode, middleNode, newGender, newNet));
						outcomes.add("DIFF");
					}
				}
			}
		}
		if(features.size() != outcomes.size()){
			System.out.println("the feature number is defferent from the outcomes");
			System.exit(-1);
		}
	}









	/**
	 * extract features for test
	 * @param features
	 * @param newGender
	 * @param newNet
	 * @throws IOException
	 */
	public void extractEnglishTestFeatures(Gender newGender, WordNet newNet, FileWriter outFileWriter) throws IOException{
		int corefNumber = this.totalCoreference.size();
		for(int i=0; i<corefNumber; i++)
			this.totalCoreference.get(i).setLabel(-1);
		if(corefNumber > 1){
			for(int i=1; i<corefNumber; i++){
				System.out.print(i+",");
				ArrayList<ArrayList<String>> features = new ArrayList<ArrayList<String>>();
				CoreferenceNode curNode = this.totalCoreference.get(i);
				for(int j=0; j<i; j++){
					//System.out.println(j);
					CoreferenceNode corefNode = this.totalCoreference.get(j);
					features.add(this.extractEnglishFeatures(curNode, corefNode, newGender, newNet));
				}
				int testNumber = features.size();
				if(testNumber > 0 ){
					for(int j=0; j<testNumber; j++){
						int featureNumber = features.get(j).size();
						if(featureNumber > 0){
							String tmp = "";
							for(int k=0; k<featureNumber; k++)
								tmp += " "+ features.get(j).get(k);
							outFileWriter.write(tmp.trim()+"\n");
						}
					}
				}
			}
		}
	}



	/**
	 * remove the unlabeled coreferences, duplicated coreferences which are overlaped, and coreferenceColumn for every sentence
	 *
	 */
	public void removeExtraCoreference(){
		//remove the unlabeled coreferences
		int coreferenceNumber = this.totalCoreference.size();
		if(coreferenceNumber > 1){
			for(int i=coreferenceNumber-1; i>=0; i--){
				if(this.totalCoreference.get(i).getLabel() == -1)
					this.totalCoreference.remove(i);
			}
		}
		//remove the duplicated
		coreferenceNumber = this.totalCoreference.size();
		if(coreferenceNumber > 1){
			for(int i=0; i<coreferenceNumber-1; i++){
				for(int j=i+1; j<coreferenceNumber; j++){
					if(this.totalCoreference.get(i).getSentenceNumber() == this.totalCoreference.get(j).getSentenceNumber()
							&& this.totalCoreference.get(i).getLabel() == this.totalCoreference.get(j).getLabel()){
						if(this.totalCoreference.get(i).isContain(this.totalCoreference.get(j))){
							this.totalCoreference.remove(j);
							j--;
							coreferenceNumber = this.totalCoreference.size();
						}
						else if(this.totalCoreference.get(i).isIncluded(this.totalCoreference.get(j))){
							this.totalCoreference.remove(i);
							i--;
							coreferenceNumber = this.totalCoreference.size();
							break;
						}
					}
				}
			}
		}
	}


	/**
	 * set coreference annotation, according to the labels of coreference relations
	 *
	 */
	public void setCoreferenceLabel(){
		for(int i=0; i<this.totalSentence.size();i++)
			this.getSentence(i).removeCoreferenceColumn();
		int coreferenceNumber = this.totalCoreference.size();
		for(int i=0; i<coreferenceNumber; i++){
			int sentenceNumber = this.totalCoreference.get(i).getSentenceNumber();
			this.totalSentence.get(sentenceNumber).setCoreference(this.totalCoreference.get(i));
		}
	}


	public void compTestCoreference(){
		//put labels to coreferences
		int label = 0;
		// clear coreferences in sentence()
		for(int i=0; i<sentenceNumber; i++)
			this.totalSentence.get(i).clearCoreference();
		// put coreferences in document to sentences
		int coreferenceNumber = this.totalCoreference.size();
		if( coreferenceNumber > 0 ){
			for(int i=0; i<coreferenceNumber; i++){
				CoreferenceNode newNode = this.totalCoreference.get(i);
				if(newNode.getSentenceNumber() > -1){
					this.totalSentence.get(newNode.getSentenceNumber()).addCoreference(newNode);
				}
			}
		}
		// deal with coreference into coreferenceColumns
		if( sentenceNumber > 0 ){
			for(int i=0; i<sentenceNumber; i++ ){
				this.totalSentence.get(i).postProcessCoreference();
			}
		}
	}


	
	
	/**
	 * compute the feature ArrayList between current node and its coreferent node
	 * from (Soon, 2001),
	 * @param curNode
	 * @param corefNode
	 * @return
	 */
	public ArrayList<String> extractEnglishFeatures(CoreferenceNode curNode, CoreferenceNode corefNode, Gender newGender, WordNet newNet){
		//curNode
		ArrayList<String> newFeature = new ArrayList<String>();
		int curStart = curNode.getStart();
		int curEnd = curNode.getEnd();
		int curSentenceNumber = curNode.getSentenceNumber();
		Conll11Sentence curSentence = this.getSentence(curSentenceNumber);
		int curSentenceLength = curSentence.getSentenceLength();
		ArrayList<String> curWordsArrayList= new ArrayList<String>();
		for(int i=curStart; i<=curEnd; i++){
			curWordsArrayList.add(curSentence.getForm(i).toLowerCase());
		}
		CfgNode curCfgNode = curSentence.getParse().findPhrase(curStart, curEnd);
		String curHeadWord = curSentence.getForm(curEnd).toLowerCase();
		if(curCfgNode != null)
			curHeadWord = curCfgNode.getHeadWord().toLowerCase();

		//corefNode
		int corefStart = corefNode.getStart();
		int corefEnd = corefNode.getEnd();
		int corefSentenceNumber = corefNode.getSentenceNumber();
		Conll11Sentence corefSentence = this.getSentence(corefSentenceNumber);
		int corefSentenceLength = corefSentence.getSentenceLength();
		ArrayList<String> corefWordsArrayList= new ArrayList<String>();
		for(int i=corefStart; i<=corefEnd; i++){
			corefWordsArrayList.add(corefSentence.getForm(i).toLowerCase());
		}
		CfgNode corefCfgNode = corefSentence.getParse().findPhrase(corefStart, corefEnd);
		String corefHeadWord = corefSentence.getForm(corefEnd).toLowerCase();
		if(corefCfgNode != null)
			corefHeadWord = corefCfgNode.getHeadWord().toLowerCase();


		int featureNumber=1;
		/***************Distance Feature and position**************************************/
		//1 Distance Feature (DIST)
		int dist = curSentenceNumber - corefSentenceNumber;
		newFeature.add("1:"+Integer.toString(dist));
		featureNumber++;
		
		
		//2 whether in the same sentence		
		if(curSentenceNumber == corefSentenceNumber)
			newFeature.add("2:t");
		else
			newFeature.add("2:f");
		featureNumber++;
		//2a:consecutive sentences
		if(curSentenceNumber == corefSentenceNumber -1)
			newFeature.add("2a:t");
		else
			newFeature.add("2a:f");
		//2b:less than 3 sentences
		if(curSentenceNumber > corefSentenceNumber - 4)
			newFeature.add("2b:t");
		else
			newFeature.add("2b:f");
			
		//2d:Distance between mi and mj in phrases:
		if(curSentenceNumber == corefSentenceNumber) {
			if(curStart -1 == curEnd)
				newFeature.add("2d:0");
			else if(curStart - 3 < curEnd)
				newFeature.add("2d:1");
		}
		else
			newFeature.add("2d:L3");
		
		//3 if the NPs form a predicate nominal construction: NN is(are) NNP
		ArrayList<String> copula = new ArrayList<String>();
		copula.add("are");copula.add("is");
		boolean predNominalSig = false;
		if(curSentenceNumber == corefSentenceNumber && corefEnd == curStart - 2
				&& copula.contains(curSentence.getForm(curStart-1).toLowerCase())){
			newFeature.add("3:t");
			predNominalSig = true;
		}
		else
			newFeature.add("3:f");
		featureNumber++;
		
		//4, 5,6  left word and right word, POS of curCoreference
		if(curStart > 0)
			newFeature.add("4:"+curSentence.getForm(curStart-1));
		else
			newFeature.add("4:none");
		featureNumber++;
		
		if(curStart > 0)
			newFeature.add("5:"+curSentence.getPOS(curStart-1));
		else
			newFeature.add("5:none");
		featureNumber++;
		if(curEnd < curSentenceLength - 1)
			newFeature.add("6:"+curSentence.getPOS(curEnd+1));
		else
			newFeature.add("6:"+"none");
		featureNumber++;
		
		
		//I/J IN QUOTES: mi/j is in quotes or inside a NP or a sentence in quotes.
		boolean curInQuote = false;
		if(curStart > 0 && curEnd < curSentenceLength -1) {
			boolean leftInQuotes = false;
			for(int i=curStart-1; i>=0; i--)
				if(curSentence.getForm(i).equals("\"") || curSentence.getForm(i).equals("\'"))
					leftInQuotes = true;
			boolean rightInQuotes = false;
			for(int i=curEnd+1; i<curSentenceLength; i++)
				if(curSentence.getForm(i).equals("\"") || curSentence.getForm(i).equals("\'"))
					rightInQuotes = true;
			if(leftInQuotes && rightInQuotes)
				curInQuote = true;
		}
		newFeature.add("6a:"+curInQuote);
		
		boolean corefInQuote = false;
		if(corefStart > 0 && corefEnd < corefSentenceLength -1) {
			boolean leftInQuotes = false;
			for(int i=corefStart-1; i>=0; i--)
				if(corefSentence.getForm(i).equals("\"") || corefSentence.getForm(i).equals("\'"))
					leftInQuotes = true;
			boolean rightInQuotes = false;
			for(int i=corefEnd+1; i<corefSentenceLength; i++)
				if(corefSentence.getForm(i).equals("\"") || corefSentence.getForm(i).equals("\'"))
					rightInQuotes = true;
			if(leftInQuotes && rightInQuotes)
				corefInQuote = true;
		}
		newFeature.add("6b:"+corefInQuote);
		
		//I/J FIRST: mi/j is the first mention in the sentence.

		if(curStart == 0)
			newFeature.add("6c:t");
		else
			newFeature.add("6c:f");
		if(corefStart == 0)
			newFeature.add("6d:t");
		else
			newFeature.add("6d:f");
		

		
		/******************Morphological features***************************/
		/***********Lexical feature,String Match feature********************************************/
		//7 String match feature from (Soon, 2001)
		String cleanCurWords="";
		for(int i=curStart; i<=curEnd; i++){
			if(!curSentence.getPOS(i).equals("DT"))
				cleanCurWords+=curSentence.getForm(i).toLowerCase()+"+";
		}
		String cleanCorefWords="";
		for(int i=corefStart; i<=corefEnd; i++){
			if(!corefSentence.getPOS(i).equals("DT"))
				cleanCorefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		}
		if(cleanCurWords.equals(cleanCorefWords))
			newFeature.add("7:t");
		else
			newFeature.add("7:f");
		featureNumber++;		
		//8 exact string match
		String curWords="";
		for(int i=curStart; i<=curEnd; i++)
			curWords+=curSentence.getForm(i).toLowerCase()+"+";
		curWords = curWords.trim();
		String corefWords="";
		for(int i=corefStart; i<=corefEnd; i++)
			corefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		corefWords = corefWords.trim();
		
		if(curWords.equals(corefWords))
			newFeature.add("8:t");
		else
			newFeature.add("8:f");
		featureNumber++;

		//9 Both are pronouns and their strings match
		String npCurWords="";
		for(int i=curStart; i<=curEnd; i++){
			if(curSentence.getPOS(i).startsWith("PRP"))
				npCurWords+=curSentence.getForm(i).toLowerCase()+"+";
		}
		String npCorefWords="";
		for(int i=corefStart; i<=corefEnd; i++){
			if(corefSentence.getPOS(i).startsWith("PRP"))
				npCorefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		}
		if(npCurWords.equals(npCorefWords))
			newFeature.add("9:t");
		else
			newFeature.add("9:f");
		featureNumber++;

		//10, 11,12,Both-Proper-Names Feature (PROPER_NAME):
		ArrayList<String> notProperNamePos = new ArrayList<String>();
		notProperNamePos.add("CC");notProperNamePos.add("IN");notProperNamePos.add("TO");
		notProperNamePos.add("DT");notProperNamePos.add("HYPH");notProperNamePos.add(",");
		boolean curProperName = true;
		for(int i=curStart; i<=curEnd; i++)
			if(!notProperNamePos.contains(curSentence.getPOS(i))
					&& !Character.isUpperCase(curSentence.getForm(i).charAt(0))){
				curProperName = false;
				break;
			}
		boolean corefProperName = true;
		for(int i=corefStart; i<=corefEnd; i++)
			if(!notProperNamePos.contains(corefSentence.getPOS(i))
					&& !Character.isUpperCase(corefSentence.getForm(i).charAt(0))){
				corefProperName = false;
				break;
			}
		
		if(curProperName)
			newFeature.add("10:"+"t");
		else
			newFeature.add("10:"+"f");
		featureNumber++;
		if(corefProperName)
			newFeature.add("11:"+"t");
		else
			newFeature.add("11:"+"f");
		featureNumber++;
		if(curProperName && corefProperName)
			newFeature.add("12:"+"b");
		else if (curProperName || corefProperName)
			newFeature.add("12:"+"o");
		else
			newFeature.add("12:"+"n");
		featureNumber++;
		
		
		//13 Apposition: m1 , m2 found
		boolean apposSig = false;
		if(curSentenceNumber == corefSentenceNumber && corefEnd == curStart -2
				&& curStart > 0 && curSentence.getForm(curStart-1).equals(",")
				&& (curProperName || corefProperName))
			newFeature.add("13:t");
		else
			newFeature.add("13:f");
		featureNumber++;
		
		String curHeadPos = curSentence.getPOS(curEnd);
		if(curCfgNode != null)
			curHeadPos = curCfgNode.getHeadPos();
		String corefHeadPos = corefSentence.getPOS(corefEnd);
		if(corefCfgNode != null)
			corefHeadPos = corefCfgNode.getHeadPos();
		
		
		//14 if both NPs are pronominal and are the same string
		if(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP")
				&& curSentence.getForm(curStart).toLowerCase().equals(corefSentence.getForm(corefStart).toLowerCase()))
			newFeature.add("14:t");
		else
			newFeature.add("14:f");
		featureNumber++;
		
		//15 if both NPs are proper names and are the same string; else I.
		if(curProperName && corefProperName && curWords.equals(corefWords))
			newFeature.add("15:t");
		else
			newFeature.add("15:f");
		featureNumber++;
		

		
		
		//16 if both NPs are non-pronominal and are the same string; else I.
		if(!(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP"))
				&& curWords.equals(corefWords))
			newFeature.add("16:"+"t");
		else
			newFeature.add("16:"+"f");
		featureNumber++;
		//17 This feature is essentially the same as soon str, but restricts string matching to non-pronominal NPs.
		if(!(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP"))
				&& cleanCurWords.equals(cleanCorefWords))
			newFeature.add("17:"+"t");
		else
			newFeature.add("17"+"f");
		featureNumber++;
		
		//18 Head match: headi == headj, head means the head noun phrase of a mention

		if(curHeadWord.equals(corefHeadWord))
			newFeature.add("18:"+"t");
		else
			newFeature.add("18:"+"f");
		featureNumber++;
		
		//19 Substring: headi substring of headj
		if(corefWords.contains(curWords))
			newFeature.add("19:"+"t");
		else
			newFeature.add("19:"+"f");
		featureNumber++;
		//20 head synonym

		if(curHeadWord.equals(corefHeadWord)
			|| (newNet.getSynonyms(curHeadWord, curHeadPos) != null && newNet.getSynonyms(curHeadWord, curHeadPos).contains(corefHeadWord))
			|| (newNet.getSynonyms(corefHeadWord, corefHeadPos) !=null && newNet.getSynonyms(corefHeadWord, corefHeadPos).contains(curHeadWord)))
			newFeature.add("20:t");
		else
			newFeature.add("20:t");
		featureNumber++;
		
		//21 Extent match: extenti == extentj, extent is the largest noun phrase headed by the head noun phrase.
		CfgNode extentCfgNode = curCfgNode;
		if(extentCfgNode != null && extentCfgNode.getFatherNode()!=null &&extentCfgNode.getFatherNode().getLabel().equals("NP"))
			extentCfgNode = extentCfgNode.getFatherNode();
		String extentCurNodeWord="+";
		if(extentCfgNode != null)
			for(int i=extentCfgNode.getBeginWordSeq(); i<=extentCfgNode.getEndWordSeq(); i++)
				extentCurNodeWord+=curSentence.getForm(i).toLowerCase()+"+";
		extentCfgNode = corefCfgNode;
		if(extentCfgNode != null && extentCfgNode.getFatherNode()!=null && extentCfgNode.getFatherNode().getLabel().equals("NP"))
				extentCfgNode = extentCfgNode.getFatherNode();
		String extentCorefNodeWord="+";
		if(extentCfgNode != null)
			for(int i=extentCfgNode.getBeginWordSeq(); i<=extentCfgNode.getEndWordSeq(); i++)
				extentCorefNodeWord+=corefSentence.getForm(i).toLowerCase()+"+";
		if(extentCurNodeWord!="+" && extentCurNodeWord.equals(extentCorefNodeWord))
			newFeature.add("21:t");
		else
			newFeature.add("21:f");
		featureNumber++;
		
		//22 miminum edit distance
		int strMED = StringMatch.LevenshteinDistance(curWords, corefWords);
		newFeature.add("22:"+strMED);
		featureNumber++;
		
		//23 MED for clean words
		strMED = StringMatch.LevenshteinDistance(cleanCurWords, cleanCorefWords);
		newFeature.add("23:"+strMED);
		featureNumber++;
		//24 MED for NP words
		strMED = StringMatch.LevenshteinDistance(npCurWords, npCorefWords);
		newFeature.add("24:"+strMED);
		featureNumber++;
		
		//25 ante med:  minimum edit distance to anaphor(Strube, 2002)
		int ante_med = (int) (100*(curWords.length()-strMED)/(double)curWords.length());
		newFeature.add("25:"+ante_med);
		featureNumber++;
		//26 ana med: minimum edit distance to antecedent
		int ana_med = (int) (100*(corefWords.length()-strMED)/(double)corefWords.length());
		newFeature.add("26:"+ana_med);
		featureNumber++;
		


		/**********Grammatical Features*******************************************/
		
		//27 i-Pronoun Feature (I_PRONOUN)
		boolean iPronounSig = false;
		if(corefStart == corefEnd
				&& corefSentence.getPOS(corefStart).startsWith("PRP")){
			newFeature.add("27:t");
			iPronounSig = true;
		}
		else{
			newFeature.add("27:f");
		}
		featureNumber++;
		//28 j-Pronoun Feature (J_PRONOUN)
		boolean jPronounSig = false;
		if(curStart == curEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")){
			newFeature.add("28:t");
			jPronounSig = true;
		}
		else{
			newFeature.add("28:f");
		}
		featureNumber++;
		
		
		//29 both i and j are pronouns
		if(iPronounSig && jPronounSig)
			newFeature.add(featureNumber+":"+"t");
		else if (iPronounSig || jPronounSig)
			newFeature.add("29:o");
		else
			newFeature.add("29:n");
		featureNumber++;

		//30 j Definite Noun Phrase Feature (DEF_NP)(J)
		if(curSentence.getForm(curStart).toLowerCase().equals("the"))
			newFeature.add("30:t");
		else
			newFeature.add("30:f");
		featureNumber++;
		
		//31 i Demonstrative Noun Phrase Feature (DEM_NP)
		String corefFirstWord = corefSentence.getForm(corefStart).toLowerCase();
		if(corefFirstWord.equals("this") || corefFirstWord.equals("that")
				|| corefFirstWord.equals("these") || corefFirstWord.equals("those"))
			newFeature.add("31:t");
		else
			newFeature.add("31:f");
		featureNumber++;

		//32 j Demonstrative Noun Phrase Feature (DEM_NP)
		String curFirstWord = curSentence.getForm(curStart).toLowerCase();
		if(curFirstWord.equals("this") || curFirstWord.equals("that")
				|| curFirstWord.equals("these") || curFirstWord.equals("those"))
			newFeature.add("32:t");
		else
			newFeature.add("32:f");
		featureNumber++;
		
		//33 if NP is an indefinite and not appositive; else C.
		if(!apposSig && !curSentence.getPOS(curStart).toLowerCase().equals("the"))
			newFeature.add("33:"+"t");
		else
			newFeature.add("33:f");
		featureNumber++;
		//34 Y i if NP is an embedded noun; else N.
		boolean corefEmbedNoun = false;
		ArrayList<CfgNode> CorefNPNodes = corefSentence.getParse().findPhrases("NP");
		if(CorefNPNodes != null){
			for(int i=0; i< CorefNPNodes.size(); i++)
				if((CorefNPNodes.get(i).getBeginWordSeq() < corefStart && CorefNPNodes.get(i).getEndWordSeq() >= corefEnd)
						||(CorefNPNodes.get(i).getBeginWordSeq() <= corefStart && CorefNPNodes.get(i).getEndWordSeq() > corefEnd)){
					corefEmbedNoun = true;break;
				}
		}
		if(corefEmbedNoun)
			newFeature.add("34:"+"t");
		else
			newFeature.add("34:"+"f");
		featureNumber++;
		
		//35 j if NP is an embedded noun; else N.
		boolean curEmbedNoun = false;
		ArrayList<CfgNode> curNPNodes = curSentence.getParse().findPhrases("NP");
		if(curNPNodes != null){
			for(int i=0; i< curNPNodes.size(); i++)
				if((curNPNodes.get(i).getBeginWordSeq() < curStart && curNPNodes.get(i).getEndWordSeq() >= curEnd)
						||(curNPNodes.get(i).getBeginWordSeq() <= curStart && curNPNodes.get(i).getEndWordSeq() > curEnd)){
					curEmbedNoun = true;break;
				}
		}
		if(curEmbedNoun)
			newFeature.add("35:t");
		else
			newFeature.add("35:f");
		featureNumber++;
	
		
        //36 Number Agreement Feature (NUMBER):

		int curNumber = newGender.isPlural(curWordsArrayList, curHeadWord);
		int corefNumber = newGender.isPlural(corefWordsArrayList, corefHeadWord);
		if( curNumber == 0|| corefNumber ==0)
			newFeature.add("36:u");
		else if (curNumber == corefNumber)
			newFeature.add("36:t");
		else
			newFeature.add("36:f");
		featureNumber++;
		//37 Gender Match; We determine the gender (male, female, or neuter) of the two phrases, and report whether they match (true, false, or unknown).
		String curGender = newGender.getGender(curWordsArrayList, curHeadWord);
		String corefGender = newGender.getGender(corefWordsArrayList, corefHeadWord);
		if(curGender.equals("none") || corefGender.equals("none"))
			newFeature.add("37:u");
		else if(curGender.equals(corefGender))
			newFeature.add("37:t");
		else
			newFeature.add("37:f");
		featureNumber++;
		
		//381 AGREEMENT: both Gender and number of mentions match: y,n,u
		if( curNumber == 0|| corefNumber ==0 || curGender.equals("none") || corefGender.equals("none"))
			newFeature.add("38:u");
		else if (curNumber == corefNumber && curGender.equals(corefGender))
			newFeature.add("38:t");
		else
			newFeature.add("38:f");
		featureNumber++;
		//39 head Pos
		newFeature.add("39:"+curHeadPos+"+"+corefHeadPos);
		featureNumber++;
		
		//40
		if(curHeadPos.equals(corefHeadPos))
			newFeature.add("40:t");
		else
			newFeature.add("40:f");
		featureNumber++;
		
		//41 whether i is subject
		int iSubjectSig = 0;
		String iSubjectVerb = "";
		if(corefEnd+1 < corefSentenceLength){
			for(int i = corefEnd+1; i< corefSentenceLength; i++){
				if(corefSentence.getPOS(i).startsWith("VB")){
					iSubjectSig = 1;
					iSubjectVerb = corefSentence.getForm(i);
					break;
				}
			}
		}
		if(iSubjectSig == 1)
			newFeature.add("41:t");
		else
			newFeature.add("41:f");
		featureNumber++;
		//42 whether j is subject
		int jSubjectSig = 0;
		String jSubjectVerb = "";
		if(curEnd+1< curSentenceLength){
			for(int i = curEnd+1; i<curSentenceLength; i++){
				if(curSentence.getPOS(i).contains("VB")){
					jSubjectSig = 1;
					jSubjectVerb = curSentence.getForm(i).toLowerCase();
					break;
				}
			}
		}
		if(jSubjectSig == 1)
			newFeature.add("42:t");
		else
			newFeature.add("42:f");
		featureNumber++;
		//if the NPs match in animacy; else I.
		//43 Animacy
		boolean curAnimacy = false;
		ArrayList<String> newPos = new ArrayList<String>();
		newPos.add("he");newPos.add("she");newPos.add("him");newPos.add("her");
		newPos.add("his");newPos.add("hers");newPos.add("himself");newPos.add("herself");
		newPos.add("'s");newPos.add("'");
		for(int i=0; i<curWordsArrayList.size(); i++){
			if(newPos.contains(curWordsArrayList.get(i))){
				curAnimacy = true;
				break;
			}
		}
		if(curAnimacy == false && curHeadPos.startsWith("N")){
			PorterStemmer newStemmer = new PorterStemmer();
			newStemmer.add(curHeadWord);
			ArrayList<String> hypernyms = newNet.getHypernyms(newStemmer.toString(), "NOUN", 0);
			if(hypernyms != null && (hypernyms.contains("person")|| hypernyms.contains("relation")
					|| hypernyms.contains("cognition")|| hypernyms.contains("communication")
					|| hypernyms.contains("emotion") || hypernyms.contains("social")))
				curAnimacy = true;
		}
		if(curAnimacy == false && jSubjectSig ==1){
			PorterStemmer newStemmer = new PorterStemmer();
			newStemmer.add(jSubjectVerb);
			ArrayList<String> vHypernyms = newNet.getHypernyms(newStemmer.toString(),"VB", 0);
			if(vHypernyms != null && (vHypernyms.contains("person")|| vHypernyms.contains("relation")
					|| vHypernyms.contains("cognition")|| vHypernyms.contains("communication")
					|| vHypernyms.contains("emotion") || vHypernyms.contains("social")))
				curAnimacy = true;
		}
		boolean corefAnimacy = false;
		for(int i=0; i<corefWordsArrayList.size(); i++){
			if(newPos.contains(corefWordsArrayList.get(i))){
				corefAnimacy = true;
				break;
			}
		}
		if(corefAnimacy == false && corefHeadPos.startsWith("N")){
			PorterStemmer newStemmer = new PorterStemmer();
			newStemmer.add(corefHeadWord);
			ArrayList<String> hypernyms = newNet.getHypernyms(newStemmer.toString(), "NOUN",3);
			if(hypernyms != null && (hypernyms.contains("person")|| hypernyms.contains("relation")
					|| hypernyms.contains("cognition")|| hypernyms.contains("communication")
					|| hypernyms.contains("emotion") || hypernyms.contains("social")))
				corefAnimacy = true;
		}
		if(corefAnimacy == false && iSubjectSig ==1){
			PorterStemmer newStemmer = new PorterStemmer();
			newStemmer.add(iSubjectVerb);
			ArrayList<String> vHypernyms = newNet.getHypernyms(newStemmer.toString(),"VB",3);
			if(vHypernyms != null && (vHypernyms.contains("person")|| vHypernyms.contains("relation")
					|| vHypernyms.contains("cognition")|| vHypernyms.contains("communication")
					|| vHypernyms.contains("emotion") || vHypernyms.contains("social")))
				corefAnimacy = true;
		}
		if(curAnimacy == corefAnimacy)
			newFeature.add("43:t");
		else
			newFeature.add("43:f");
		featureNumber++;
		//44 named entity pairs:for each mention: name → its type, noun → NOUN , pronoun → its spelling
		String curNamedEntity = curSentence.getNamedEntity(curStart, curEnd);
		String corefNamedEntity = corefSentence.getNamedEntity(corefStart, corefEnd);
		
		String tmp = "";
		if(!curNamedEntity.equals("none"))
			tmp += curNamedEntity;
		else
			tmp +=curHeadPos;
		if(!corefNamedEntity.equals("none"))
			tmp += "+"+corefNamedEntity;
		else
			tmp +="+"+corefHeadPos;
		newFeature.add("44:"+tmp);
		featureNumber++;

		//45 how much NN* and PRP* are same
		int cleanSameWordsNumber = 0;
		for(int i=curStart; i<=curEnd; i++){
			for(int j=corefStart; j<=corefEnd; j++){
				if((curSentence.getPOS(i).startsWith("NN") || curSentence.getPOS(i).startsWith("PRP"))
						&& (corefSentence.getPOS(j).startsWith("NN") || corefSentence.getPOS(j).startsWith("PRP"))
						&& curSentence.getForm(i).toLowerCase().equals(corefSentence.getForm(j).toLowerCase()))
					cleanSameWordsNumber++;
			}
		}
		newFeature.add("45:"+cleanSameWordsNumber);
		featureNumber++;
		
		/**********************Syntaictic features****************************/
		
		//46 C-command ccmd(m1 , m2 ) (Luo, 2007)
		boolean ccmd = false;
		if(curSentenceNumber == corefSentenceNumber
				&& curCfgNode != null && corefCfgNode != null
				&& curCfgNode.getFatherNode() != null && corefCfgNode.getFatherNode()!=null){
			CfgNode corefFatherNode = corefCfgNode.getFatherNode();
			if(corefFatherNode.getEndWordSeq() >= curEnd)
				ccmd = true;
		}
		
		if(ccmd)
			newFeature.add("46:"+"t");
		else
			newFeature.add("46:"+"f");
		featureNumber++;
		
		//47 c-command path T (m1 , m2 )
		ArrayList<String> ccmdPath = new ArrayList<String>();
		if(ccmd){
			CfgNode corefFatherNode = corefCfgNode.getFatherNode();
			ArrayList<CfgNode> curAncestorNodes = curSentence.getParse().findAncestor(curEnd);
			if(curAncestorNodes!=null){
				int ancestorNumber = curAncestorNodes.size();
				int i=0;
				for(; i<ancestorNumber; i++){
					if(curAncestorNodes.get(i).getBeginWordSeq() >= corefFatherNode.getBeginWordSeq()
							&& curAncestorNodes.get(i).getEndWordSeq() <= corefFatherNode.getEndWordSeq())
						break;
				}
				if(i<ancestorNumber)
					for(;i<ancestorNumber;i++)
						ccmdPath.add(curAncestorNodes.get(i).getLabel());
			}
		}		
		int ccmdPathNumber = ccmdPath.size();		
		if(ccmdPathNumber > 0){
			String ccmdPathLabels = "";
			for(int i=0; i<ccmdPathNumber; i++)
				ccmdPathLabels += "+"+ccmdPath.get(i);
			newFeature.add("47:"+ccmdPathLabels);
		}
		else
			newFeature.add("47:n");
		featureNumber++;
		
		//48 N P count(m1 , m2 )
		if(ccmdPathNumber > 1){
			int ccmdPathNPs = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).equals("NP"))
					ccmdPathNPs++;
			newFeature.add("48:"+ccmdPathNPs);
		}
		else
			newFeature.add("48:"+"n");
		featureNumber++;
		//49 V P count(m1 , m2 )
		if(ccmdPathNumber > 1){
			int ccmdPathVPs = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).equals("VP"))
					ccmdPathVPs++;
			newFeature.add("49:"+ccmdPathVPs);
		}
		else
			newFeature.add("49:"+"n");
		featureNumber++;
		
		//50 S count(m1 , m2 ):
		if(ccmdPathNumber > 1){
			int ccmdPathClauses = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).startsWith("S"))
					ccmdPathClauses++;
			newFeature.add("50:"+ccmdPathClauses);
		}
		else
			newFeature.add("50:n");
		featureNumber++;
		
		//51 same head(m1 , m2 )
		String curFatherHeadWord = "none";
		String curFatherHeadPos = "none";
		String curGrandFatherHeadWord = "none";
		String curGrandFatherHeadPos = "none";
		String corefFatherHeadWord = "none";
		String corefFatherHeadPos = "none";
		String corefGrandFatherHeadWord = "none";
		String corefGrandFatherHeadPos = "none";
		if(curSentenceNumber == corefSentenceNumber
				&& curCfgNode != null && corefCfgNode != null ){
			ArrayList<CfgNode> curAncestorNodes = curSentence.getParse().findAncestor(curEnd);
			ArrayList<CfgNode> corefAncestorNodes = corefSentence.getParse().findAncestor(corefEnd);
			if(curAncestorNodes != null && curAncestorNodes.size()>0){
				int i = curAncestorNodes.size()-1;
				for(; i >=0 ; i--){
					if(!curAncestorNodes.get(i).getHeadWord().equals(curHeadWord)){
						curFatherHeadWord = curAncestorNodes.get(i).getHeadWord();
						curFatherHeadPos = curAncestorNodes.get(i).getHeadPos();
						break;
					}
				}
				if(i >= 0)
					for(; i>=0; i--){
						if(!curAncestorNodes.get(i).getHeadWord().equals(curFatherHeadWord)){
							curGrandFatherHeadWord = curAncestorNodes.get(i).getHeadWord();
							curGrandFatherHeadPos = curAncestorNodes.get(i).getHeadPos();
							break;
						}
					}
			}
			if(corefAncestorNodes != null && corefAncestorNodes.size() >0){
				int i= corefAncestorNodes.size() - 1;
				for(; i >=0 ; i--){
					if(!corefAncestorNodes.get(i).getHeadWord().equals(corefHeadWord)){
						corefFatherHeadWord = corefAncestorNodes.get(i).getHeadWord();
						corefFatherHeadPos = corefAncestorNodes.get(i).getHeadPos();
						break;
					}
				}
				if(i >= 0){
					for(; i>=0; i--){
						if(!corefAncestorNodes.get(i).getHeadWord().equals(corefFatherHeadWord)){
							corefGrandFatherHeadWord = corefAncestorNodes.get(i).getHeadWord();
							corefGrandFatherHeadPos = corefAncestorNodes.get(i).getHeadPos();
							break;
						}
					}
				}
			}
		}
		boolean commonFather = false;
		if(!curFatherHeadWord.equals("none") && !corefFatherHeadWord.equals("none")
			&& curFatherHeadWord.equals(corefFatherHeadWord)){
			commonFather = true;
			newFeature.add("51:"+curFatherHeadWord);
		}
		else
			newFeature.add("51:"+"none");
		featureNumber++;
		//52 same P OS(m1 , m2 )
		if(commonFather)
			newFeature.add("52:"+curFatherHeadPos);
		else
			newFeature.add("52:"+"none");
		featureNumber++;
		//53 mod(m1 , m2 )
		if(!corefFatherHeadWord.equals("none") && corefFatherHeadWord.equals(curHeadWord))
			newFeature.add("53:"+"t");
		else
			newFeature.add("53:"+"f");
		featureNumber++;
		//54 mod( m2, m1 )
		if(!curFatherHeadWord.equals("none") && curFatherHeadWord.equals(corefHeadWord))
			newFeature.add("54:"+"t");
		else
			newFeature.add("54:"+"f");
		featureNumber++;
		//55 same head2(m1 , m2 ):  h(h(m1 )) = h(m2 )
		boolean iGrandSig = false;
		if( !corefGrandFatherHeadWord.equals("none") && !curFatherHeadWord.equals("none")
				&& corefGrandFatherHeadWord.equals(curFatherHeadWord)){
			iGrandSig = true;
			newFeature.add("55:"+curFatherHeadWord);
		}
		else
			newFeature.add("55:"+"none");
		featureNumber++;
		//56 pos2(m1 , m2 )
		if(iGrandSig)
			newFeature.add("56:"+curFatherHeadPos);
		else
			newFeature.add("56:"+"none");
		featureNumber++;
		//57 same head2(m2 , m1 ): h(m1 ) = h( h(m2 ))
		boolean jGrandSig = false;
		if(!curFatherHeadWord.equals("none") && !corefGrandFatherHeadWord.equals("none")
				&& curGrandFatherHeadWord.equals(corefFatherHeadWord)){
			jGrandSig = true;
			newFeature.add("57:"+corefFatherHeadWord);
		}
		else
			newFeature.add("57:"+"none");
		featureNumber++;
		//58 pos2(m2,m1)
		if(jGrandSig)
			newFeature.add("58:"+corefFatherHeadPos);
		else
			newFeature.add("58:"+"none");
		featureNumber++;
		//59 same head22(m1 , m2 ):
		if(!curGrandFatherHeadWord.equals("none") && !corefGrandFatherHeadWord.equals("none")
				&& curGrandFatherHeadWord.equals(corefGrandFatherHeadWord)){
			newFeature.add("59:"+corefGrandFatherHeadWord);
		}
		else
			newFeature.add("59:"+"none");
		featureNumber++;
		//60 same parent(m1 , m2 ), MAXIMALNP: Both mentions have the same NP parent or they are nested: y,n
		if(curSentenceNumber == corefSentenceNumber){
			CfgNode coFather = corefSentence.getParse().findCoFather(curStart, curEnd, corefStart, corefEnd);
			if(coFather != null && coFather.getLabel().equals("NP"))
				newFeature.add("60:t");
			else
				if((curStart <= corefStart && curEnd >= corefEnd)
						|| (curStart >= corefStart && curEnd <= corefEnd))
					newFeature.add("60:t");
				else
					newFeature.add("60:f");
		}
		else
			newFeature.add("60:f");
		featureNumber++;
		

		/***********Semantic features*********************************************/

		//61 WordNet//8.Semantic Class Agreement Feature (SEMCLASS)
		ArrayList<String> namedEntityClass = new ArrayList<String>();
		namedEntityClass.add("TIME");namedEntityClass.add("LAW");namedEntityClass.add("GPE");
		namedEntityClass.add("NORP");namedEntityClass.add("LANGUAGE");namedEntityClass.add("PERCENT");
		namedEntityClass.add("FAC");namedEntityClass.add("PRODUCT");namedEntityClass.add("ORDINAL");
		namedEntityClass.add("LOC");namedEntityClass.add("PERSON");namedEntityClass.add("WORK_OF_ART");
		namedEntityClass.add("MONEY");namedEntityClass.add("EVENT");namedEntityClass.add("DATE");
		namedEntityClass.add("QUANTITY");namedEntityClass.add("ORG");namedEntityClass.add("CARDINAL");
		//replace SC in wordnet to NE (Ng, 2007)
		ArrayList<String> orgWordNets = new ArrayList<String>();
		orgWordNets.add("social_group");
		ArrayList<String> facWordNets = new ArrayList<String>();
		facWordNets.add("establishment");facWordNets.add("construction");facWordNets.add("building");
		facWordNets.add("facility");facWordNets.add("workplace");
		ArrayList<String> gpeWordNets = new ArrayList<String>();
		gpeWordNets.add("country");gpeWordNets.add("province");gpeWordNets.add("government");
		gpeWordNets.add("town");gpeWordNets.add("city");gpeWordNets.add("administration");
		gpeWordNets.add("society");gpeWordNets.add("island");gpeWordNets.add("community");
		ArrayList<String> locWordNets = new ArrayList<String>();
		locWordNets.add("dry_land");locWordNets.add("region");locWordNets.add("landmass");
		locWordNets.add("body_of_water");locWordNets.add("geographical_area");locWordNets.add("geological_formation");
		ArrayList<String> curSemanticClass = newNet.getHypernyms(curHeadWord, curHeadPos,3);
		int curSemanticClassNumber = 0;
		if(curSemanticClass != null)
			curSemanticClassNumber = curSemanticClass.size();
		else
			curSemanticClass = new ArrayList<String>();
		if(curSemanticClassNumber > 0){
			for(int i=0; i<curSemanticClassNumber; i++){
				if(orgWordNets.contains(curSemanticClass.get(i)))
					curSemanticClass.add("org");
				else if(facWordNets.contains(curSemanticClass.get(i)))
					curSemanticClass.add("fac");
				else if(gpeWordNets.contains(curSemanticClass.get(i)))
					curSemanticClass.add("gpe");
				else if(locWordNets.contains(curSemanticClass.get(i)))
					curSemanticClass.add("loc");
			}
		}
		if(!curNamedEntity.equals("none")){
			if(namedEntityClass.contains(curNamedEntity)){
				curSemanticClass.add(curNamedEntity.toLowerCase());
			}
		}
		ArrayList<String> corefSemanticClass = newNet.getHypernyms(corefHeadWord, corefHeadPos,3);
		int corefSemanticClassNumber = 0;
		if(corefSemanticClass != null)
			corefSemanticClassNumber = corefSemanticClass.size();
		else
			corefSemanticClass = new ArrayList<String>();
		if(corefSemanticClassNumber>0){
			for(int i=0; i<corefSemanticClassNumber; i++){
				if(orgWordNets.contains(corefSemanticClass.get(i)))
					corefSemanticClass.add("org");
				else if(facWordNets.contains(corefSemanticClass.get(i)))
					corefSemanticClass.add("fac");
				else if(gpeWordNets.contains(corefSemanticClass.get(i)))
					corefSemanticClass.add("gpe");
				else if(locWordNets.contains(corefSemanticClass.get(i)))
					corefSemanticClass.add("loc");
			}
		}
		if(!corefNamedEntity.equals("none")){
			if(namedEntityClass.contains(corefNamedEntity))
				corefSemanticClass.add(corefNamedEntity.toLowerCase());
		}
		boolean semanticClassAgree = false;
		curSemanticClassNumber = curSemanticClass.size();
		corefSemanticClassNumber = corefSemanticClass.size();
		if(curSemanticClassNumber>0 && corefSemanticClassNumber>0){
			for(int i=0; i<curSemanticClassNumber; i++)
				for(int j=0; j<corefSemanticClassNumber; j++)
					if(curSemanticClass.get(i).equals(corefSemanticClass.get(j))){
						semanticClassAgree = true;
						break;
					}
		}
		if(semanticClassAgree)
			newFeature.add("61:t");
		else
			newFeature.add("61:f");
		featureNumber++;
		//62 alias: extract the head of
		//If both NPs are dates, we first normalize them
		//by a date tagger (Mani and Wilson, 2000) and then check whether the two refer
		//to the same date. If both are persons (e.g., John Smith and Smith), we pick the
		//last word of each NP and check whether the two words are the same. If both are
		//organizations (e.g., General Electric and GE), we check whether one is a potential
		//acronym of the other. The feature value is:
		boolean alias = false;
		if(curSemanticClassNumber>0 && corefSemanticClassNumber>0
				&&(curSemanticClass.contains("date") || curSemanticClass.contains("time"))
				&& (corefSemanticClass.contains("date") || corefSemanticClass.contains("time"))){
			for(int i=0; i<curWordsArrayList.size();i++)
				for(int j=0; j<corefWordsArrayList.size(); j++)
					if(curWordsArrayList.get(i).equals(corefWordsArrayList.get(j))){
						alias = true;
						break;
					}
		}
		if(alias == false && curSemanticClassNumber>0 && corefSemanticClassNumber>0
				&& curSemanticClass.contains("person") && corefSemanticClass.contains("person"))
			if(curWordsArrayList.get(curWordsArrayList.size()-1).equals(corefWordsArrayList.get(corefWordsArrayList.size()-1)))
				alias = true;
		if(alias == false && curSemanticClassNumber>0 && corefSemanticClassNumber>0
				&& (curSemanticClass.contains("org") || curSemanticClass.contains("group"))
				&& (corefSemanticClass.contains("org") || corefSemanticClass.contains("group"))){
					String curWordsAlias="";
					for(int i=curStart; i<=curEnd; i++)
						curWordsAlias+=curSentence.getForm(i).toLowerCase().substring(0,1);
					if(corefWords.toLowerCase().startsWith(curWordsAlias))
						alias = true;
		}
		if(alias == true)
			newFeature.add("62:t");
		else
			newFeature.add("62:f");
		featureNumber++;



		//semantic similarity




		//63,64 semantic role labeling (Ponzetto, 2006)
		//governing verb and its grammatical role
		
		ArrayList<String> curPredicates =curSentence.getSemantic().findPredicate(curStart, curEnd);
		ArrayList<String> corefPredicates = corefSentence.getSemantic().findPredicate(corefStart, corefEnd);
		String coPred = "none";
		if(curPredicates.size() != 0 || corefPredicates.size() != 0){
			for(int i=0; i<curPredicates.size(); i++)
				for(int j=0; j<corefPredicates.size(); j++)
					if(curPredicates.get(i).equals(corefPredicates.get(j))){
						coPred = curPredicates.get(i);
						break;
					}
		}
		if(!coPred.equals("none")) {
			String curRole = curSentence.getSemantic().findRole(curStart, curEnd, coPred);
			String corefRole = corefSentence.getSemantic().findRole(corefStart, corefEnd, coPred);
			if(curRole.equals(corefRole))
				newFeature.add("63:t");
			else
				newFeature.add("63:f");
			if(curRole.equals(corefRole) && curPredicates.equals(corefPredicates))
				newFeature.add("64:t");
			else
				newFeature.add("64:f");			
		}
		else {
			newFeature.add("63:f");
			newFeature.add("64:f");		
		}


		return newFeature;

	}
	
	

	
	
	public void extractChineseTrainFeatures(ArrayList<ArrayList<String>> features, ArrayList<String> outcomes){
		int corefNumber = this.totalCoreference.size();
		for(int i=1; i<corefNumber; i++){
			CoreferenceNode curNode = this.totalCoreference.get(i);
			int sig = -1;
			//find its coreferent node
			for(int j=i-1; j>=0; j--){
				CoreferenceNode corefNode = this.totalCoreference.get(j);
				if(corefNode.isLabelSame(curNode)){
					features.add(this.extractChineseFeatures(curNode, corefNode));
					outcomes.add("IDENT");
					sig = j;
					break;
				}
			}
			//find these nodes between the node and its coreferent node
			int k = sig + 1;
			if( k > 0 && k < i){
				for(; k<i; k++){
					CoreferenceNode middleNode = this.totalCoreference.get(k);
					if(!middleNode.isLabelSame(curNode)){
						features.add(this.extractChineseFeatures(curNode, middleNode));
						outcomes.add("DIFF");
					}
				}
			}
		}
		if(features.size() != outcomes.size()){
			System.out.println("the feature number is defferent from the outcomes");
			System.exit(-1);
		}
	}
	
	
	public void extractChineseTestFeatures(FileWriter outFileWriter) throws IOException{
		int corefNumber = this.totalCoreference.size();
		for(int i=0; i<corefNumber; i++)
			this.totalCoreference.get(i).setLabel(-1);
		if(corefNumber > 1){
			for(int i=1; i<corefNumber; i++){
				System.out.print(i+",");
				ArrayList<ArrayList<String>> features = new ArrayList<ArrayList<String>>();
				CoreferenceNode curNode = this.totalCoreference.get(i);
				for(int j=0; j<i; j++){
					//System.out.println(j);
					CoreferenceNode corefNode = this.totalCoreference.get(j);
					features.add(this.extractChineseFeatures(curNode, corefNode));
				}
				int testNumber = features.size();
				if(testNumber > 0 ){
					for(int j=0; j<testNumber; j++){
						int featureNumber = features.get(j).size();
						if(featureNumber > 0){
							String tmp = "";
							for(int k=0; k<featureNumber; k++)
								tmp += " "+ features.get(j).get(k);
							outFileWriter.write(tmp.trim()+"\n");
						}
					}
				}
			}
		}
	}
	
	
	
	
	
	
	public ArrayList<String> extractChineseFeatures(CoreferenceNode curNode, CoreferenceNode corefNode)  {
		
		//curNode
		ArrayList<String> newFeature = new ArrayList<String>();
		int curStart = curNode.getStart();
		int curEnd = curNode.getEnd();
		int curSentenceNumber = curNode.getSentenceNumber();
		Conll11Sentence curSentence = this.getSentence(curSentenceNumber);
		int curSentenceLength = curSentence.getSentenceLength();
		ArrayList<String> curWordsArrayList= new ArrayList<String>();
		for(int i=curStart; i<=curEnd; i++){
			curWordsArrayList.add(curSentence.getForm(i).toLowerCase());
		}
		CfgNode curCfgNode = curSentence.getParse().findPhrase(curStart, curEnd);
		String curHeadWord = curSentence.getForm(curEnd).toLowerCase();
		if(curCfgNode != null)
			curHeadWord = curCfgNode.getHeadWord().toLowerCase();

		//corefNode
		int corefStart = corefNode.getStart();
		int corefEnd = corefNode.getEnd();
		int corefSentenceNumber = corefNode.getSentenceNumber();
		Conll11Sentence corefSentence = this.getSentence(corefSentenceNumber);
		int corefSentenceLength = corefSentence.getSentenceLength();
		ArrayList<String> corefWordsArrayList= new ArrayList<String>();
		for(int i=corefStart; i<=corefEnd; i++){
			corefWordsArrayList.add(corefSentence.getForm(i).toLowerCase());
		}
		CfgNode corefCfgNode = corefSentence.getParse().findPhrase(corefStart, corefEnd);
		String corefHeadWord = corefSentence.getForm(corefEnd).toLowerCase();
		if(corefCfgNode != null)
			corefHeadWord = corefCfgNode.getHeadWord().toLowerCase();


		int featureNumber=1;
		/***************Distance Feature and position**************************************/
		//1 Distance Feature (DIST)
		int dist = curSentenceNumber - corefSentenceNumber;
		newFeature.add("1:"+Integer.toString(dist));
		featureNumber++;
		
		
		//2 whether in the same sentence		
		if(curSentenceNumber == corefSentenceNumber)
			newFeature.add("2:t");
		else
			newFeature.add("2:f");
		featureNumber++;
		//2a:consecutive sentences
		if(curSentenceNumber == corefSentenceNumber -1)
			newFeature.add("2a:t");
		else
			newFeature.add("2a:f");
		//2b:less than 3 sentences
		if(curSentenceNumber > corefSentenceNumber - 4)
			newFeature.add("2b:t");
		else
			newFeature.add("2b:f");
			
		//2d:Distance between mi and mj in phrases:
		if(curSentenceNumber == corefSentenceNumber) {
			if(curStart -1 == curEnd)
				newFeature.add("2d:0");
			else if(curStart - 3 < curEnd)
				newFeature.add("2d:1");
		}
		else
			newFeature.add("2d:L3");
		
		//3 if the NPs form a predicate nominal construction: NN is(are) NNP
		ArrayList<String> copula = new ArrayList<String>();
		copula.add("are");copula.add("is");
		boolean predNominalSig = false;
		if(curSentenceNumber == corefSentenceNumber && corefEnd == curStart - 2
				&& copula.contains(curSentence.getForm(curStart-1).toLowerCase())){
			newFeature.add("3:t");
			predNominalSig = true;
		}
		else
			newFeature.add("3:f");
		featureNumber++;
		
		//4, 5,6  left word and right word, POS of curCoreference
		if(curStart > 0)
			newFeature.add("4:"+curSentence.getForm(curStart-1));
		else
			newFeature.add("4:none");
		featureNumber++;
		
		if(curStart > 0)
			newFeature.add("5:"+curSentence.getPOS(curStart-1));
		else
			newFeature.add("5:none");
		featureNumber++;
		if(curEnd < curSentenceLength - 1)
			newFeature.add("6:"+curSentence.getPOS(curEnd+1));
		else
			newFeature.add("6:"+"none");
		featureNumber++;
		
		
		//I/J IN QUOTES: mi/j is in quotes or inside a NP or a sentence in quotes.
		boolean curInQuote = false;
		if(curStart > 0 && curEnd < curSentenceLength -1) {
			boolean leftInQuotes = false;
			for(int i=curStart-1; i>=0; i--)
				if(curSentence.getForm(i).equals("\"") || curSentence.getForm(i).equals("\'"))
					leftInQuotes = true;
			boolean rightInQuotes = false;
			for(int i=curEnd+1; i<curSentenceLength; i++)
				if(curSentence.getForm(i).equals("\"") || curSentence.getForm(i).equals("\'"))
					rightInQuotes = true;
			if(leftInQuotes && rightInQuotes)
				curInQuote = true;
		}
		newFeature.add("6a:"+curInQuote);
		
		boolean corefInQuote = false;
		if(corefStart > 0 && corefEnd < corefSentenceLength -1) {
			boolean leftInQuotes = false;
			for(int i=corefStart-1; i>=0; i--)
				if(corefSentence.getForm(i).equals("\"") || corefSentence.getForm(i).equals("\'"))
					leftInQuotes = true;
			boolean rightInQuotes = false;
			for(int i=corefEnd+1; i<corefSentenceLength; i++)
				if(corefSentence.getForm(i).equals("\"") || corefSentence.getForm(i).equals("\'"))
					rightInQuotes = true;
			if(leftInQuotes && rightInQuotes)
				corefInQuote = true;
		}
		newFeature.add("6b:"+corefInQuote);
		
		//I/J FIRST: mi/j is the first mention in the sentence.

		if(curStart == 0)
			newFeature.add("6c:t");
		else
			newFeature.add("6c:f");
		if(corefStart == 0)
			newFeature.add("6d:t");
		else
			newFeature.add("6d:f");
		

		
		/******************Morphological features***************************/
		/***********Lexical feature,String Match feature********************************************/
		//7 String match feature from (Soon, 2001)
		String cleanCurWords="";
		for(int i=curStart; i<=curEnd; i++){
			if(!curSentence.getPOS(i).equals("DT"))
				cleanCurWords+=curSentence.getForm(i).toLowerCase()+"+";
		}
		String cleanCorefWords="";
		for(int i=corefStart; i<=corefEnd; i++){
			if(!corefSentence.getPOS(i).equals("DT"))
				cleanCorefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		}
		if(cleanCurWords.equals(cleanCorefWords))
			newFeature.add("7:t");
		else
			newFeature.add("7:f");
		featureNumber++;		
		//8 exact string match
		String curWords="";
		for(int i=curStart; i<=curEnd; i++)
			curWords+=curSentence.getForm(i).toLowerCase()+"+";
		curWords = curWords.trim();
		String corefWords="";
		for(int i=corefStart; i<=corefEnd; i++)
			corefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		corefWords = corefWords.trim();
		
		if(curWords.equals(corefWords))
			newFeature.add("8:t");
		else
			newFeature.add("8:f");
		featureNumber++;

		//9 Both are pronouns and their strings match
		String npCurWords="";
		for(int i=curStart; i<=curEnd; i++){
			if(curSentence.getPOS(i).startsWith("PRP"))
				npCurWords+=curSentence.getForm(i).toLowerCase()+"+";
		}
		String npCorefWords="";
		for(int i=corefStart; i<=corefEnd; i++){
			if(corefSentence.getPOS(i).startsWith("PRP"))
				npCorefWords+=corefSentence.getForm(i).toLowerCase()+"+";
		}
		if(npCurWords.equals(npCorefWords))
			newFeature.add("9:t");
		else
			newFeature.add("9:f");
		featureNumber++;

		//10, 11,12,Both-Proper-Names Feature (PROPER_NAME):
		ArrayList<String> notProperNamePos = new ArrayList<String>();
		notProperNamePos.add("CC");notProperNamePos.add("IN");notProperNamePos.add("TO");
		notProperNamePos.add("DT");notProperNamePos.add("HYPH");notProperNamePos.add(",");
		boolean curProperName = true;
		for(int i=curStart; i<=curEnd; i++)
			if(!notProperNamePos.contains(curSentence.getPOS(i))
					&& !Character.isUpperCase(curSentence.getForm(i).charAt(0))){
				curProperName = false;
				break;
			}
		boolean corefProperName = true;
		for(int i=corefStart; i<=corefEnd; i++)
			if(!notProperNamePos.contains(corefSentence.getPOS(i))
					&& !Character.isUpperCase(corefSentence.getForm(i).charAt(0))){
				corefProperName = false;
				break;
			}
		
		if(curProperName)
			newFeature.add("10:"+"t");
		else
			newFeature.add("10:"+"f");
		featureNumber++;
		if(corefProperName)
			newFeature.add("11:"+"t");
		else
			newFeature.add("11:"+"f");
		featureNumber++;
		if(curProperName && corefProperName)
			newFeature.add("12:"+"b");
		else if (curProperName || corefProperName)
			newFeature.add("12:"+"o");
		else
			newFeature.add("12:"+"n");
		featureNumber++;
		
		
		//13 Apposition: m1 , m2 found
		boolean apposSig = false;
		if(curSentenceNumber == corefSentenceNumber && corefEnd == curStart -2
				&& curStart > 0 && curSentence.getForm(curStart-1).equals(",")
				&& (curProperName || corefProperName))
			newFeature.add("13:t");
		else
			newFeature.add("13:f");
		featureNumber++;
		
		String curHeadPos = curSentence.getPOS(curEnd);
		if(curCfgNode != null)
			curHeadPos = curCfgNode.getHeadPos();
		String corefHeadPos = corefSentence.getPOS(corefEnd);
		if(corefCfgNode != null)
			corefHeadPos = corefCfgNode.getHeadPos();
		
		
		//14 if both NPs are pronominal and are the same string
		if(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP")
				&& curSentence.getForm(curStart).toLowerCase().equals(corefSentence.getForm(corefStart).toLowerCase()))
			newFeature.add("14:t");
		else
			newFeature.add("14:f");
		featureNumber++;
		
		//15 if both NPs are proper names and are the same string; else I.
		if(curProperName && corefProperName && curWords.equals(corefWords))
			newFeature.add("15:t");
		else
			newFeature.add("15:f");
		featureNumber++;
		

		
		
		//16 if both NPs are non-pronominal and are the same string; else I.
		if(!(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP"))
				&& curWords.equals(corefWords))
			newFeature.add("16:"+"t");
		else
			newFeature.add("16:"+"f");
		featureNumber++;
		//17 This feature is essentially the same as soon str, but restricts string matching to non-pronominal NPs.
		if(!(curStart==curEnd && corefStart == corefEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")
				&& corefSentence.getPOS(corefStart).startsWith("PRP"))
				&& cleanCurWords.equals(cleanCorefWords))
			newFeature.add("17:"+"t");
		else
			newFeature.add("17"+"f");
		featureNumber++;
		
		//18 Head match: headi == headj, head means the head noun phrase of a mention

		if(curHeadWord.equals(corefHeadWord))
			newFeature.add("18:"+"t");
		else
			newFeature.add("18:"+"f");
		featureNumber++;
		
		//19 Substring: headi substring of headj
		if(corefWords.contains(curWords))
			newFeature.add("19:"+"t");
		else
			newFeature.add("19:"+"f");
		featureNumber++;
		//20 head synonym

		if(curHeadWord.equals(corefHeadWord))
			newFeature.add("20:t");
		else
			newFeature.add("20:t");
		featureNumber++;
		
		//21 Extent match: extenti == extentj, extent is the largest noun phrase headed by the head noun phrase.
		CfgNode extentCfgNode = curCfgNode;
		if(extentCfgNode != null && extentCfgNode.getFatherNode()!=null &&extentCfgNode.getFatherNode().getLabel().equals("NP"))
			extentCfgNode = extentCfgNode.getFatherNode();
		String extentCurNodeWord="+";
		if(extentCfgNode != null)
			for(int i=extentCfgNode.getBeginWordSeq(); i<=extentCfgNode.getEndWordSeq(); i++)
				extentCurNodeWord+=curSentence.getForm(i).toLowerCase()+"+";
		extentCfgNode = corefCfgNode;
		if(extentCfgNode != null && extentCfgNode.getFatherNode()!=null && extentCfgNode.getFatherNode().getLabel().equals("NP"))
				extentCfgNode = extentCfgNode.getFatherNode();
		String extentCorefNodeWord="+";
		if(extentCfgNode != null)
			for(int i=extentCfgNode.getBeginWordSeq(); i<=extentCfgNode.getEndWordSeq(); i++)
				extentCorefNodeWord+=corefSentence.getForm(i).toLowerCase()+"+";
		if(extentCurNodeWord!="+" && extentCurNodeWord.equals(extentCorefNodeWord))
			newFeature.add("21:t");
		else
			newFeature.add("21:f");
		featureNumber++;
		
		//22 miminum edit distance
		int strMED = StringMatch.LevenshteinDistance(curWords, corefWords);
		newFeature.add("22:"+strMED);
		featureNumber++;
		
		//23 MED for clean words
		strMED = StringMatch.LevenshteinDistance(cleanCurWords, cleanCorefWords);
		newFeature.add("23:"+strMED);
		featureNumber++;
		//24 MED for NP words
		strMED = StringMatch.LevenshteinDistance(npCurWords, npCorefWords);
		newFeature.add("24:"+strMED);
		featureNumber++;
		
		//25 ante med:  minimum edit distance to anaphor(Strube, 2002)
		int ante_med = (int) (100*(curWords.length()-strMED)/(double)curWords.length());
		newFeature.add("25:"+ante_med);
		featureNumber++;
		//26 ana med: minimum edit distance to antecedent
		int ana_med = (int) (100*(corefWords.length()-strMED)/(double)corefWords.length());
		newFeature.add("26:"+ana_med);
		featureNumber++;
		


		/**********Grammatical Features*******************************************/
		
		//27 i-Pronoun Feature (I_PRONOUN)
		boolean iPronounSig = false;
		if(corefStart == corefEnd
				&& corefSentence.getPOS(corefStart).startsWith("PRP")){
			newFeature.add("27:t");
			iPronounSig = true;
		}
		else{
			newFeature.add("27:f");
		}
		featureNumber++;
		//28 j-Pronoun Feature (J_PRONOUN)
		boolean jPronounSig = false;
		if(curStart == curEnd
				&& curSentence.getPOS(curStart).startsWith("PRP")){
			newFeature.add("28:t");
			jPronounSig = true;
		}
		else{
			newFeature.add("28:f");
		}
		featureNumber++;
		
		
		//29 both i and j are pronouns
		if(iPronounSig && jPronounSig)
			newFeature.add(featureNumber+":"+"t");
		else if (iPronounSig || jPronounSig)
			newFeature.add("29:o");
		else
			newFeature.add("29:n");
		featureNumber++;

		//30 j Definite Noun Phrase Feature (DEF_NP)(J)
		if(curSentence.getForm(curStart).toLowerCase().equals("the"))
			newFeature.add("30:t");
		else
			newFeature.add("30:f");
		featureNumber++;
		
		//31 i Demonstrative Noun Phrase Feature (DEM_NP)
		String corefFirstWord = corefSentence.getForm(corefStart).toLowerCase();
		if(corefFirstWord.equals("this") || corefFirstWord.equals("that")
				|| corefFirstWord.equals("these") || corefFirstWord.equals("those"))
			newFeature.add("31:t");
		else
			newFeature.add("31:f");
		featureNumber++;

		//32 j Demonstrative Noun Phrase Feature (DEM_NP)
		String curFirstWord = curSentence.getForm(curStart).toLowerCase();
		if(curFirstWord.equals("this") || curFirstWord.equals("that")
				|| curFirstWord.equals("these") || curFirstWord.equals("those"))
			newFeature.add("32:t");
		else
			newFeature.add("32:f");
		featureNumber++;
		
		//33 if NP is an indefinite and not appositive; else C.
		if(!apposSig && !curSentence.getPOS(curStart).toLowerCase().equals("the"))
			newFeature.add("33:"+"t");
		else
			newFeature.add("33:f");
		featureNumber++;
		//34 Y i if NP is an embedded noun; else N.
		boolean corefEmbedNoun = false;
		ArrayList<CfgNode> CorefNPNodes = corefSentence.getParse().findPhrases("NP");
		if(CorefNPNodes != null){
			for(int i=0; i< CorefNPNodes.size(); i++)
				if((CorefNPNodes.get(i).getBeginWordSeq() < corefStart && CorefNPNodes.get(i).getEndWordSeq() >= corefEnd)
						||(CorefNPNodes.get(i).getBeginWordSeq() <= corefStart && CorefNPNodes.get(i).getEndWordSeq() > corefEnd)){
					corefEmbedNoun = true;break;
				}
		}
		if(corefEmbedNoun)
			newFeature.add("34:"+"t");
		else
			newFeature.add("34:"+"f");
		featureNumber++;
		
		//35 j if NP is an embedded noun; else N.
		boolean curEmbedNoun = false;
		ArrayList<CfgNode> curNPNodes = curSentence.getParse().findPhrases("NP");
		if(curNPNodes != null){
			for(int i=0; i< curNPNodes.size(); i++)
				if((curNPNodes.get(i).getBeginWordSeq() < curStart && curNPNodes.get(i).getEndWordSeq() >= curEnd)
						||(curNPNodes.get(i).getBeginWordSeq() <= curStart && curNPNodes.get(i).getEndWordSeq() > curEnd)){
					curEmbedNoun = true;break;
				}
		}
		if(curEmbedNoun)
			newFeature.add("35:t");
		else
			newFeature.add("35:f");
		featureNumber++;
	
		
        //36 Number Agreement Feature (NUMBER):
		boolean curPlural = isPluralChinese(curWords);
		boolean corefPlural = isPluralChinese(corefWords);
		if( curPlural == corefPlural)
			newFeature.add("36:t");
		else 
			newFeature.add("36:f");
		featureNumber++;
		//37 Gender Match; We determine the gender (male, female, or neuter) of the two phrases, and report whether they match (true, false, or unknown).
		String curGender = deterSex(curWords);
		String corefGender = deterSex(corefWords);
		if(curGender.equals("none") || corefGender.equals("none"))
			newFeature.add("37:u");
		else if(curGender.equals(corefGender))
			newFeature.add("37:t");
		else
			newFeature.add("37:f");
		featureNumber++;
		
		//381 AGREEMENT: both Gender and number of mentions match: y,n,u
		if( curGender.equals("none") || corefGender.equals("none"))
			newFeature.add("38:u");
		else if (curPlural == corefPlural && curGender.equals(corefGender))
			newFeature.add("38:t");
		else
			newFeature.add("38:f");		
					
		featureNumber++;
		//39 head Pos
		newFeature.add("39:"+curHeadPos+"+"+corefHeadPos);
		featureNumber++;
		
		//40
		if(curHeadPos.equals(corefHeadPos))
			newFeature.add("40:t");
		else
			newFeature.add("40:f");
		featureNumber++;
		
		//41 whether i is subject
		int iSubjectSig = 0;
		String iSubjectVerb = "";
		if(corefEnd+1 < corefSentenceLength){
			for(int i = corefEnd+1; i< corefSentenceLength; i++){
				if(corefSentence.getPOS(i).startsWith("VB")){
					iSubjectSig = 1;
					iSubjectVerb = corefSentence.getForm(i);
					break;
				}
			}
		}
		if(iSubjectSig == 1)
			newFeature.add("41:t");
		else
			newFeature.add("41:f");
		featureNumber++;
		//42 whether j is subject
		int jSubjectSig = 0;
		String jSubjectVerb = "";
		if(curEnd+1< curSentenceLength){
			for(int i = curEnd+1; i<curSentenceLength; i++){
				if(curSentence.getPOS(i).contains("VB")){
					jSubjectSig = 1;
					jSubjectVerb = curSentence.getForm(i).toLowerCase();
					break;
				}
			}
		}
		if(jSubjectSig == 1)
			newFeature.add("42:t");
		else
			newFeature.add("42:f");
		featureNumber++;
		//if the NPs match in animacy; else I.
		//43 Animacy
		
		featureNumber++;
		//44 named entity pairs:for each mention: name → its type, noun → NOUN , pronoun → its spelling
		String curNamedEntity = curSentence.getNamedEntity(curStart, curEnd);
		String corefNamedEntity = corefSentence.getNamedEntity(corefStart, corefEnd);
		
		String tmp = "";
		if(!curNamedEntity.equals("none"))
			tmp += curNamedEntity;
		else
			tmp +=curHeadPos;
		if(!corefNamedEntity.equals("none"))
			tmp += "+"+corefNamedEntity;
		else
			tmp +="+"+corefHeadPos;
		newFeature.add("44:"+tmp);
		featureNumber++;

		//45 how much NN* and PRP* are same
		int cleanSameWordsNumber = 0;
		for(int i=curStart; i<=curEnd; i++){
			for(int j=corefStart; j<=corefEnd; j++){
				if((curSentence.getPOS(i).startsWith("NN") || curSentence.getPOS(i).startsWith("PRP"))
						&& (corefSentence.getPOS(j).startsWith("NN") || corefSentence.getPOS(j).startsWith("PRP"))
						&& curSentence.getForm(i).toLowerCase().equals(corefSentence.getForm(j).toLowerCase()))
					cleanSameWordsNumber++;
			}
		}
		newFeature.add("45:"+cleanSameWordsNumber);
		featureNumber++;
		
		/**********************Syntaictic features****************************/
		
		//46 C-command ccmd(m1 , m2 ) (Luo, 2007)
		boolean ccmd = false;
		if(curSentenceNumber == corefSentenceNumber
				&& curCfgNode != null && corefCfgNode != null
				&& curCfgNode.getFatherNode() != null && corefCfgNode.getFatherNode()!=null){
			CfgNode corefFatherNode = corefCfgNode.getFatherNode();
			if(corefFatherNode.getEndWordSeq() >= curEnd)
				ccmd = true;
		}
		
		if(ccmd)
			newFeature.add("46:"+"t");
		else
			newFeature.add("46:"+"f");
		featureNumber++;
		
		//47 c-command path T (m1 , m2 )
		ArrayList<String> ccmdPath = new ArrayList<String>();
		if(ccmd){
			CfgNode corefFatherNode = corefCfgNode.getFatherNode();
			ArrayList<CfgNode> curAncestorNodes = curSentence.getParse().findAncestor(curEnd);
			if(curAncestorNodes!=null){
				int ancestorNumber = curAncestorNodes.size();
				int i=0;
				for(; i<ancestorNumber; i++){
					if(curAncestorNodes.get(i).getBeginWordSeq() >= corefFatherNode.getBeginWordSeq()
							&& curAncestorNodes.get(i).getEndWordSeq() <= corefFatherNode.getEndWordSeq())
						break;
				}
				if(i<ancestorNumber)
					for(;i<ancestorNumber;i++)
						ccmdPath.add(curAncestorNodes.get(i).getLabel());
			}
		}		
		int ccmdPathNumber = ccmdPath.size();		
		if(ccmdPathNumber > 0){
			String ccmdPathLabels = "";
			for(int i=0; i<ccmdPathNumber; i++)
				ccmdPathLabels += "+"+ccmdPath.get(i);
			newFeature.add("47:"+ccmdPathLabels);
		}
		else
			newFeature.add("47:n");
		featureNumber++;
		
		//48 N P count(m1 , m2 )
		if(ccmdPathNumber > 1){
			int ccmdPathNPs = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).equals("NP"))
					ccmdPathNPs++;
			newFeature.add("48:"+ccmdPathNPs);
		}
		else
			newFeature.add("48:"+"n");
		featureNumber++;
		//49 V P count(m1 , m2 )
		if(ccmdPathNumber > 1){
			int ccmdPathVPs = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).equals("VP"))
					ccmdPathVPs++;
			newFeature.add("49:"+ccmdPathVPs);
		}
		else
			newFeature.add("49:"+"n");
		featureNumber++;
		
		//50 S count(m1 , m2 ):
		if(ccmdPathNumber > 1){
			int ccmdPathClauses = 0;
			for(int i=0; i<ccmdPathNumber-1; i++)
				if(ccmdPath.get(i).startsWith("S"))
					ccmdPathClauses++;
			newFeature.add("50:"+ccmdPathClauses);
		}
		else
			newFeature.add("50:n");
		featureNumber++;
		
		//51 same head(m1 , m2 )
		String curFatherHeadWord = "none";
		String curFatherHeadPos = "none";
		String curGrandFatherHeadWord = "none";
		String curGrandFatherHeadPos = "none";
		String corefFatherHeadWord = "none";
		String corefFatherHeadPos = "none";
		String corefGrandFatherHeadWord = "none";
		String corefGrandFatherHeadPos = "none";
		if(curSentenceNumber == corefSentenceNumber
				&& curCfgNode != null && corefCfgNode != null ){
			ArrayList<CfgNode> curAncestorNodes = curSentence.getParse().findAncestor(curEnd);
			ArrayList<CfgNode> corefAncestorNodes = corefSentence.getParse().findAncestor(corefEnd);
			if(curAncestorNodes != null && curAncestorNodes.size()>0){
				int i = curAncestorNodes.size()-1;
				for(; i >=0 ; i--){
					if(!curAncestorNodes.get(i).getHeadWord().equals(curHeadWord)){
						curFatherHeadWord = curAncestorNodes.get(i).getHeadWord();
						curFatherHeadPos = curAncestorNodes.get(i).getHeadPos();
						break;
					}
				}
				if(i >= 0)
					for(; i>=0; i--){
						if(!curAncestorNodes.get(i).getHeadWord().equals(curFatherHeadWord)){
							curGrandFatherHeadWord = curAncestorNodes.get(i).getHeadWord();
							curGrandFatherHeadPos = curAncestorNodes.get(i).getHeadPos();
							break;
						}
					}
			}
			if(corefAncestorNodes != null && corefAncestorNodes.size() >0){
				int i= corefAncestorNodes.size() - 1;
				for(; i >=0 ; i--){
					if(!corefAncestorNodes.get(i).getHeadWord().equals(corefHeadWord)){
						corefFatherHeadWord = corefAncestorNodes.get(i).getHeadWord();
						corefFatherHeadPos = corefAncestorNodes.get(i).getHeadPos();
						break;
					}
				}
				if(i >= 0){
					for(; i>=0; i--){
						if(!corefAncestorNodes.get(i).getHeadWord().equals(corefFatherHeadWord)){
							corefGrandFatherHeadWord = corefAncestorNodes.get(i).getHeadWord();
							corefGrandFatherHeadPos = corefAncestorNodes.get(i).getHeadPos();
							break;
						}
					}
				}
			}
		}
		boolean commonFather = false;
		if(!curFatherHeadWord.equals("none") && !corefFatherHeadWord.equals("none")
			&& curFatherHeadWord.equals(corefFatherHeadWord)){
			commonFather = true;
			newFeature.add("51:"+curFatherHeadWord);
		}
		else
			newFeature.add("51:"+"none");
		featureNumber++;
		//52 same P OS(m1 , m2 )
		if(commonFather)
			newFeature.add("52:"+curFatherHeadPos);
		else
			newFeature.add("52:"+"none");
		featureNumber++;
		//53 mod(m1 , m2 )
		if(!corefFatherHeadWord.equals("none") && corefFatherHeadWord.equals(curHeadWord))
			newFeature.add("53:"+"t");
		else
			newFeature.add("53:"+"f");
		featureNumber++;
		//54 mod( m2, m1 )
		if(!curFatherHeadWord.equals("none") && curFatherHeadWord.equals(corefHeadWord))
			newFeature.add("54:"+"t");
		else
			newFeature.add("54:"+"f");
		featureNumber++;
		//55 same head2(m1 , m2 ):  h(h(m1 )) = h(m2 )
		boolean iGrandSig = false;
		if( !corefGrandFatherHeadWord.equals("none") && !curFatherHeadWord.equals("none")
				&& corefGrandFatherHeadWord.equals(curFatherHeadWord)){
			iGrandSig = true;
			newFeature.add("55:"+curFatherHeadWord);
		}
		else
			newFeature.add("55:"+"none");
		featureNumber++;
		//56 pos2(m1 , m2 )
		if(iGrandSig)
			newFeature.add("56:"+curFatherHeadPos);
		else
			newFeature.add("56:"+"none");
		featureNumber++;
		//57 same head2(m2 , m1 ): h(m1 ) = h( h(m2 ))
		boolean jGrandSig = false;
		if(!curFatherHeadWord.equals("none") && !corefGrandFatherHeadWord.equals("none")
				&& curGrandFatherHeadWord.equals(corefFatherHeadWord)){
			jGrandSig = true;
			newFeature.add("57:"+corefFatherHeadWord);
		}
		else
			newFeature.add("57:"+"none");
		featureNumber++;
		//58 pos2(m2,m1)
		if(jGrandSig)
			newFeature.add("58:"+corefFatherHeadPos);
		else
			newFeature.add("58:"+"none");
		featureNumber++;
		//59 same head22(m1 , m2 ):
		if(!curGrandFatherHeadWord.equals("none") && !corefGrandFatherHeadWord.equals("none")
				&& curGrandFatherHeadWord.equals(corefGrandFatherHeadWord)){
			newFeature.add("59:"+corefGrandFatherHeadWord);
		}
		else
			newFeature.add("59:"+"none");
		featureNumber++;
		//60 same parent(m1 , m2 ), MAXIMALNP: Both mentions have the same NP parent or they are nested: y,n
		if(curSentenceNumber == corefSentenceNumber){
			CfgNode coFather = corefSentence.getParse().findCoFather(curStart, curEnd, corefStart, corefEnd);
			if(coFather != null && coFather.getLabel().equals("NP"))
				newFeature.add("60:t");
			else
				if((curStart <= corefStart && curEnd >= corefEnd)
						|| (curStart >= corefStart && curEnd <= corefEnd))
					newFeature.add("60:t");
				else
					newFeature.add("60:f");
		}
		else
			newFeature.add("60:f");
		featureNumber++;
		

		/***********Semantic features*********************************************/

		//61 WordNet//8.Semantic Class Agreement Feature (SEMCLASS)

		featureNumber++;
		//62 alias: extract the head of
		//If both NPs are dates, we first normalize them
		//by a date tagger (Mani and Wilson, 2000) and then check whether the two refer
		//to the same date. If both are persons (e.g., John Smith and Smith), we pick the
		//last word of each NP and check whether the two words are the same. If both are
		//organizations (e.g., General Electric and GE), we check whether one is a potential
		//acronym of the other. The feature value is:

		featureNumber++;



		//semantic similarity




		//63,64 semantic role labeling (Ponzetto, 2006)
		//governing verb and its grammatical role
		
		ArrayList<String> curPredicates =curSentence.getSemantic().findPredicate(curStart, curEnd);
		ArrayList<String> corefPredicates = corefSentence.getSemantic().findPredicate(corefStart, corefEnd);
		String coPred = "none";
		if(curPredicates.size() != 0 || corefPredicates.size() != 0){
			for(int i=0; i<curPredicates.size(); i++)
				for(int j=0; j<corefPredicates.size(); j++)
					if(curPredicates.get(i).equals(corefPredicates.get(j))){
						coPred = curPredicates.get(i);
						break;
					}
		}
		if(!coPred.equals("none")) {
			String curRole = curSentence.getSemantic().findRole(curStart, curEnd, coPred);
			String corefRole = corefSentence.getSemantic().findRole(corefStart, corefEnd, coPred);
			if(curRole.equals(corefRole))
				newFeature.add("63:t");
			else
				newFeature.add("63:f");
			if(curRole.equals(corefRole) && curPredicates.equals(corefPredicates))
				newFeature.add("64:t");
			else
				newFeature.add("64:f");			
		}
		else {
			newFeature.add("63:f");
			newFeature.add("64:f");		
		}


				
		return newFeature;
	
	
	}
	
	public boolean isPluralChinese(String word) {
		String pluralWords = "[、|多|和|及|们|与|曹|等|辈|属|些|群|对|队|几|两|二|三|四|五|六|七|八|九|十|百|千|万|亿|兆]";
		for(int i=0; i<word.length(); i++) {
			if(word.substring(i, i+1).matches(".*"+pluralWords+".*"))
				return true;
		}
		//重复词
		for(int i=1; i<word.length(); i++) {
			if(word.substring(i, i+1).equals(word.substring(i-1, i)))
				return true;
		}		
		return false;
	}
	
	public String deterSex(String word) {
		String maleWords = "[他|武|厚|大|航|祥|洋|山|天|鑫|民|皓|哲|策|文|力|功|斌|冠|佳|彬|广|楠|军|永|新|彪|林|彦|乐|平|渊|进|义|之|国|茂|固|清|霖|裕|江|良|树|伯|勇|伦|超|发|震|玉|弘|达|友|子|龙|中|凡|盛|辰|栋|炎|腾|学|浩|时|奇|东|辉|和|飞|潇|世|会|善|小|强|伟|阳|奕|一|锋|丁|风|海|旭|安|铭|峰|邦|坚|信|心|宏|轩|宁|雄|宇|轮|志|恒|以|立|承|刚|鹏|涛|政|昊|润|雨|源|生|明|群|昌|河|毅|铄|才|建|俊|朗|磊|有|朋|涵|星|仁|琛|保|言|豪|家|启|嘉|然|松|富|景|顺|兴|晨|卓|健|博|钦|钧|梓|杰|行|翔|士|壮|千|庆|智|胜|榕|全|华|思|亨|亮|振|梁|岩|泰|致|维|睿|云|福|康|贤|金|泽|元|波|成|鸣|谦|瑞|德|敬|瑜|翰|君|克|圣|先]";
		String femaleWords = "[她|青|黛|璧|馥|馨|静|娜|冰|洁|素|娟|桂|珍|娅|珊|枫|蕊|佳|茜|彩|娴|筠|颖|茗|惠|园|燕|枝|娣|娥|璐|香|影|珠|舒|欢|婕|欣|丹|丽|霞|萍|荷|艺|玉|霄|希|锦|艳|凡|荣|凤|婉|伊|柔|婷|婵|聪|凝|露|玲|勤|悦|咏|叶|飘|荔|可|环|霭|琬|媛|蓉|昭|莲|真|巧|雁|宁|芬|琦|雅|春|莺|美|倩|琼|眉|妹|芳|宜|琴|琰|仪|芸|琳|蓓|雪|妍|秋|竹|秀|韵|育|月|红|慧|纨|芝|莎|澜|羽|莉|毓|纯|兰|嘉|菲|姬|姣|淑|英|瑾|滢|华|思|梅|晶|薇|寒|贞|瑶|云|怡|翠|亚|卿|岚|苑|爱|融|菁|瑞|君|菊|梦|晓|爽|瑗]";
		for(int i=0; i<word.length(); i++) {
			if(word.substring(i, i+1).matches(".*"+maleWords+".*"))
				return "male";
			else if(word.substring(i, i+1).matches(".*"+femaleWords+".*"))
				return "female";
		}
		return "none";
	}
	




	/**
	 * Store Document
	 * @param fileName
	 */
	public void storeTrainDocument(FileWriter newWriter) throws IOException{
		newWriter.write("#begin document (" + documentName + "); part "+partName+"\n");
		for(int i=0;i<sentenceNumber;i++){
			this.totalSentence.get(i).storeTrainSentence(newWriter);
			newWriter.write("\n");
		}
		newWriter.write("#end document\n");
	}


	/**
	 * Store Document
	 * <p> 存储一个文档
	 * @param fileName
	 */
	public void storeTestDocument(FileWriter newWriter) throws IOException{
		newWriter.write("#begin document (" + documentName + "); part "+partName+"\n");
		for(int i=0;i<sentenceNumber;i++){
			this.totalSentence.get(i).storeTestSentence(newWriter);
			newWriter.write("\n");
		}
		newWriter.write("#end document\n");
	}






	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
