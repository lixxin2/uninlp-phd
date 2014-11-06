/**
 * 
 */
package edu.hitsz.nlp.coref;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.CfgNode;
import edu.hitsz.nlp.struct.CfgTree;
import edu.hitsz.nlp.struct.CfgTreeHead;

/**
 * @author tm
 *
 */
public class Conll11Sentence {
	
	
	private int sentenceLength;
	private int sentenceSeq;
	//1     Document ID             This is a variation on the document filename
	private String documentID;
	//2   	Part number 	        Some files are divided into multiple parts numbered as 000, 001, 002, ... etc.
	private String partNumber;
	//3   	Word number 	
	private ArrayList<Integer> wordNumber;
	//4   	Word itself 	
	private ArrayList<String> form;
	//5   	Part-of-Speech 	
	private ArrayList<String> pos;
	//6   	Parse bit 	            This is the bracketed structure broken before the first open parenthesis in the parse, and the word/part-of-speech leaf replaced with a *. The full parse can be created by substituting the asterix with the "([pos] [word])" string (or leaf) and concatenating the items in the rows of that column.
	private ArrayList<String> parseBit;
	private CfgTree parse;
	//7   	Predicate lemma 	    The predicate lemma is mentioned for the rows for which we have semantic role information. All other rows are marked with a "-"
	private ArrayList<String> predicateLemma;
	//8   	Predicate Frameset ID 	This is the PropBank frameset ID of the predicate in Column 7.
	private ArrayList<String> framesetID;
	//9   	Word sense 	            This is the word sense of the word in Column 3.
	private ArrayList<String> wordSense;
	//10   	Speaker/Author 	        This is the speaker or author name where available. Mostly in Broadcast Conversation and Web Log data.
	private ArrayList<String> speaker;
	//11   	Named Entities 	        These columns identifies the spans representing various named entities.
	private ArrayList<String> namedEntityColumn;
	private ArrayList<NamedEntityNode> namedEntity;
	//12:N  Predicate Arguments 	There is one column each of predicate argument structure information for the predicate mentioned in Column 7.
	private ArrayList<ArrayList<String>>  predicateArguments;
	private SemanticRole semantic;
	//N   	Coreference 	        Coreference chain information encoded in a parenthesis structure.
	private ArrayList<String> coreferenceColumn;
	private ArrayList<CoreferenceNode> coreference;	
	
	//To store all the information
	//public ArrayList<ArrayList<String>> word;

	
	public Conll11Sentence(int number){
		sentenceLength = 0;
		this.sentenceSeq = number;
		documentID = new String();
		partNumber = new String();
		wordNumber = new ArrayList<Integer>();
		form = new ArrayList<String>();
		pos = new ArrayList<String>();
		parseBit = new ArrayList<String>();
		parse = new CfgTree();
		predicateLemma = new ArrayList<String>();
		framesetID = new ArrayList<String>();
		wordSense = new ArrayList<String>();
		speaker = new ArrayList<String>();
		namedEntityColumn = new ArrayList<String>();
		namedEntity = new ArrayList<NamedEntityNode>();
		predicateArguments = new ArrayList<ArrayList<String>>();
		semantic = new SemanticRole();
		coreferenceColumn = new ArrayList<String>();
		coreference = new ArrayList<CoreferenceNode>(); 
				
		//To store all the information
		//word = new ArrayList<ArrayList<String>>();
	}
	
	public Conll11Sentence(){
		sentenceLength = 0;
		this.sentenceSeq = 0;
		documentID = new String();
		partNumber = new String();
		wordNumber = new ArrayList<Integer>();
		form = new ArrayList<String>();
		pos = new ArrayList<String>();
		parseBit = new ArrayList<String>();
		parse = new CfgTree();
		predicateLemma = new ArrayList<String>();
		framesetID = new ArrayList<String>();
		wordSense = new ArrayList<String>();
		speaker = new ArrayList<String>();
		namedEntityColumn = new ArrayList<String>();
		namedEntity = new ArrayList<NamedEntityNode>();
		predicateArguments = new ArrayList<ArrayList<String>>();
		semantic = new SemanticRole();
		coreferenceColumn = new ArrayList<String>();
		coreference = new ArrayList<CoreferenceNode>(); 
	}
	
	public int getSentenceLength(){
		return this.sentenceLength;
	}
	
	public String getDocumentID(){
		return this.documentID;
	}
	
	public String getPartNumber(){
		return this.partNumber;
	}
	
	public String getForm(int i){
		return this.form.get(i);
	}
	
	public String getPOS(int i){
		return this.pos.get(i);
	}
	
	public ArrayList<String> getPOS(){
		return this.pos;
	}
	
	public int getNamedEntityNumber(){
		return this.namedEntity.size();
	}
	
	public String getNamedEntityLabel(int i){
		return this.namedEntity.get(i).label;
	}
	
	public ArrayList<String> getPOSEx(int length){
		ArrayList<String> newVec = new ArrayList<String>();
		newVec.addAll(this.pos);
		for(int i=2; i<=length; i++){
			if(this.sentenceLength>i){
				for(int j=0;j<this.sentenceLength-i+1;j++){
					String tmp="";
					for(int k=0;k<i;k++)
						tmp += "+"+this.pos.get(j+k);
					newVec.add(tmp.substring(1));
				}
					
			}
		}
		return newVec;
	}
	
	public CfgTree getParse(){
		return this.parse;
	}
	
	public void clearCoreference(){
		this.coreference.clear();
	}
	
	public void addCoreference(CoreferenceNode newNode){
		this.coreference.add(newNode);
	}
	
	public int getCoreferenceSize(){
		return this.coreference.size();
	}
	
	public ArrayList<CoreferenceNode> getCoreference(){
		return this.coreference;
	}
	
	public SemanticRole getSemantic(){
		return this.semantic;
	}
	
	
	public boolean isNamedEntity(int kstart, int kend){
		int namedEntityNumber = namedEntity.size();
		if(namedEntityNumber > 0)
			for(int i=0; i<namedEntityNumber; i++)
				if(namedEntity.get(i).start == kstart 
						&& namedEntity.get(i).end == kend)
					return true;
		return false;
	}
	
	public String getNamedEntity(int kstart, int kend){
		int namedEntityNumber = namedEntity.size();
		if(namedEntityNumber > 0){
			for(int i=0; i<namedEntityNumber; i++){
				if(namedEntity.get(i).start == kstart && namedEntity.get(i).end == kend)
					return namedEntity.get(i).label;
				else if (namedEntity.get(i).start >= kstart && namedEntity.get(i).end <= kend)
					return namedEntity.get(i).label;
				else if (namedEntity.get(i).start <= kstart && namedEntity.get(i).end >= kend)
					return namedEntity.get(i).label;
			}
		}
		return "none";
	}
	
	
	/**
	 * read training data
	 * @param tempSentence A sentence structured as a ArrayList of strings
	 * @param len the length of the ArrayList
	 */
	public void processTrain(ArrayList<String> tempSentence){
		sentenceLength=tempSentence.size();
		int wordsSize;
		for(int i=0;i<sentenceLength;i++){
			String row=tempSentence.get(i);
			String[] words = row.replaceAll("\\s{1,}", " ").split(" ");
			wordsSize = words.length;			
			if(wordsSize>0){
				if(i==0){
					documentID = words[0];
					partNumber = words[1];
				}
				wordNumber.add(Integer.parseInt(words[2]));
				form.add(words[3]);
				pos.add(words[4]);
				parseBit.add(words[5]);				
				predicateLemma.add(words[6]);
				framesetID.add(words[7]);
				wordSense.add(words[8]);
				speaker.add(words[9]);
				namedEntityColumn.add(words[10]);
				//
				if(wordsSize>12){
					ArrayList<String> tmp2;
					//add ArrayList<String> to ArrayList<ArrayList<String>>
					if(i==0){
						for(int k=11; k<wordsSize-1; k++){
							tmp2=new ArrayList<String>();
							predicateArguments.add(tmp2);
						}
					}
					//add String to ArrayList<String> in ArrayList<ArrayList<String>>
					if(i>=0){
						for(int k=11; k<wordsSize-1; k++){
							predicateArguments.get(k-11).add(words[k]);
						}
					}
				}
				coreferenceColumn.add(words[wordsSize-1]);
			}
		}
		//process named entity
		NamedEntityNode.getFromColumn(namedEntity, namedEntityColumn);
		
		//process syntactic parsing
		parse.loadWithPW(parseBit,pos,form);
		
		//process predicates and arguments
		if(predicateArguments.size()>0){
			semantic.readPred(predicateLemma);
			semantic.readArgument(predicateArguments);		
		}	
		
		//process coreference 
		CoreferenceNode.getFromColumn(coreference, coreferenceColumn, sentenceSeq);
		
	}
	
	
	/**
	 * read testing data, and process them into structure
	 * @param tempSentence A sentence structured as a ArrayList of strings
	 * @param len the length of the ArrayList
	 */
	public void processTest(ArrayList<String> tempSentence){
		sentenceLength=tempSentence.size();
		int wordsSize;
		for(int i=0;i<sentenceLength;i++){
			String row=tempSentence.get(i);
			String[] words = row.replaceAll("\\s{1,}", " ").split(" ");
			wordsSize = words.length;			
			if(wordsSize>0){
				if(i==0){
					documentID = words[0];
					partNumber = words[1];
				}
				wordNumber.add(Integer.parseInt(words[2]));
				form.add(words[3]);
				pos.add(words[4]);
				parseBit.add(words[5]);				
				predicateLemma.add(words[6]);
				framesetID.add(words[7]);
				wordSense.add(words[8]);
				speaker.add(words[9]);
				namedEntityColumn.add(words[10]);
				//
				if(wordsSize>11){
					ArrayList<String> tmp2;
					if(i==0){
						for(int k=11; k<wordsSize-1; k++){
							tmp2=new ArrayList<String>();
							predicateArguments.add(tmp2);
						}
					}
					if(i>=0){
						for(int k=11; k<wordsSize-1; k++){
							predicateArguments.get(k-11).add(words[k]);
						}
					}
				}
				coreferenceColumn.add("-");
			}
		}
		//process named entity
		NamedEntityNode.getFromColumn(namedEntity, namedEntityColumn);
			
		//process syntactic parsing
		parse.loadWithPW(parseBit,pos,form);
		
		//process predicates and arguments
		if(predicateArguments.size()>0){
			semantic.readPred(predicateLemma);
			semantic.readArgument(predicateArguments);		
		}		
		
		//process coreference NP and PRP
		preProcessTestCoreferenceNP();
	}
	
	
	public void findHead(CfgTreeHead newTreeHead){
		this.parse.findHead(newTreeHead);
	}
	

	/**
	 * read all possible coreferent, including NPs, pronouns, names
	 * Called by processTest method
	 */
	public void preProcessTestCoreferenceNP(){
		coreference.clear();
		ArrayList<CfgNode> npNodes = this.parse.findPhrases("NP");
		int npNodesNumber = npNodes.size();
		if(npNodesNumber>0){
			for(int i=0; i<npNodesNumber; i++){
				CoreferenceNode newNode = new CoreferenceNode();
				newNode.setStart(npNodes.get(i).getBeginWordSeq());
				newNode.setEnd(npNodes.get(i).getEndWordSeq());
				newNode.setSentenceNumber(sentenceSeq);
				coreference.add(newNode);
			}			
		}
		if(sentenceLength>0){
			for(int i=0; i<sentenceLength; i++){
				if(pos.get(i).startsWith("PRP")){
					//sig for 
					int sig = 0;
					if(npNodesNumber>0){
						for(int j=0; j<npNodesNumber; j++){
							if(coreference.get(j).getStart() == i
									&& coreference.get(j).getEnd() == i){
								sig = 1;
								break;
							}								
						}
						if(sig == 0){
							CoreferenceNode newNode = new CoreferenceNode();
							newNode.setStart(i);
							newNode.setEnd(i);
							newNode.setSentenceNumber(sentenceSeq);
							coreference.add(newNode);
						}
					}
				}
			}
		}			
		
	}
	
	public void removeCoreferenceColumn(){
		for(int i=0; i<sentenceLength; i++)
			coreferenceColumn.set(i,"-");
	}
	
	
	/**
	 * convert coreferences to coreferenceColumn
	 */
	public void postProcessCoreference(){
		int coreferenceNumber = coreference.size();
		if( coreferenceNumber > 0 ){
			for( int i=0; i<coreferenceNumber; i++){
				CoreferenceNode curCoreference = coreference.get(i);
				int curStart = curCoreference.getStart();
				int curEnd = curCoreference.getEnd();
				// coreference is in one token
				if(curStart == curEnd){
					if(coreferenceColumn.get(curStart).equals("-")){
						String tmp = "("+Integer.toString(curCoreference.getLabel())+")";
						coreferenceColumn.set(curStart, tmp);
					}
					else{
						String tmp = coreferenceColumn.get(curStart)+"|("+curCoreference.getLabel()+")";
						coreferenceColumn.set(curStart, tmp);
					}
				}
				// coreference is in several tokens
				else{
					//
					if(coreferenceColumn.get(curStart).equals("-")){
						String tmp = "("+Integer.toString(curCoreference.getLabel());
						coreferenceColumn.set(curStart, tmp);
					}
					else{
						String tmp = coreferenceColumn.get(curStart)+"|("+curCoreference.getLabel();
						coreferenceColumn.set(curStart, tmp);
					}
					//
					if(coreferenceColumn.get(curEnd).equals("-")){
						String tmp = Integer.toString(curCoreference.getLabel())+")";
						coreferenceColumn.set(curEnd, tmp);
					}
					else{
						String tmp = coreferenceColumn.get(curEnd)+"|"+curCoreference.getLabel()+")";
						coreferenceColumn.set(curEnd, tmp);
					}
				}				
			}
		}
	}

	
	/**
	 * set coreference Column according to label in CoreferenceNode
	 * @param newNode
	 */
	public void setCoreference(CoreferenceNode newNode){
		int kStart = newNode.getStart();
		int kEnd = newNode.getEnd();
		if(kStart == kEnd){
			if(coreferenceColumn.get(kStart) == "-")
				coreferenceColumn.set(kStart, "("+Integer.toString(newNode.getLabel())+")");
			else
				coreferenceColumn.set(kStart, coreferenceColumn.get(kStart)+"|("+Integer.toString(newNode.getLabel())+")");
		}
		else{
			if(coreferenceColumn.get(kStart) == "-")
				coreferenceColumn.set(kStart, "("+Integer.toString(newNode.getLabel()));
			else
				coreferenceColumn.set(kStart, coreferenceColumn.get(kStart)+"|("+Integer.toString(newNode.getLabel()));
			if(coreferenceColumn.get(kEnd) == "-")
				coreferenceColumn.set(kEnd, Integer.toString(newNode.getLabel())+")");
			else
				coreferenceColumn.set(kEnd, coreferenceColumn.get(kEnd)+"|"+Integer.toString(newNode.getLabel())+")");		
			
		}
	}
	
	
	/**
	 * Store sentence in file
	 * @throws IOException 
	 * 
	 */
	public void storeTrainSentence(FileWriter newWriter) throws IOException{
		
		if(this.sentenceLength>0){
			for(int i=0;i<this.sentenceLength;i++){
				String tmpString="";
				String delimiter = "\t";
				tmpString+=delimiter+documentID;
				tmpString+=delimiter+partNumber;
				//tmpString+=" "+sentenceSeq;
				tmpString+=delimiter+wordNumber.get(i);
				tmpString+=delimiter+form.get(i);
				tmpString+=delimiter+pos.get(i);
				tmpString+=delimiter+parseBit.get(i);
				tmpString+=delimiter+predicateLemma.get(i);
				tmpString+=delimiter+framesetID.get(i);
				tmpString+=delimiter+wordSense.get(i);
				tmpString+=delimiter+speaker.get(i);
				tmpString+=delimiter+namedEntityColumn.get(i);
				int argsNumber = predicateArguments.size();
				if(argsNumber>0){					
					for(int j=0; j<argsNumber; j++)
						tmpString+=delimiter+predicateArguments.get(j).get(i);					
				}
				tmpString+=delimiter+coreferenceColumn.get(i);
				newWriter.write(tmpString.trim()+"\n");
			}
		}

		
	}
	
	
	/**
	 * Store sentence in file
	 * @throws IOException 
	 * 
	 */
	public void storeTestSentence(FileWriter newWriter) throws IOException{	
		if(this.sentenceLength>0){
			for(int i=0;i<this.sentenceLength;i++){
				String tmpString="";
				String delimiter = "\t";
				tmpString+=delimiter+documentID;
				tmpString+=delimiter+partNumber;
				//tmpString+=" "+sentenceSeq;
				tmpString+=delimiter+wordNumber.get(i);
				tmpString+=delimiter+form.get(i);
				tmpString+=delimiter+pos.get(i);
				tmpString+=delimiter+parseBit.get(i);
				tmpString+=delimiter+predicateLemma.get(i);
				tmpString+=delimiter+framesetID.get(i);
				tmpString+=delimiter+wordSense.get(i);
				tmpString+=delimiter+speaker.get(i);
				tmpString+=delimiter+namedEntityColumn.get(i);
				int argsNumber = predicateArguments.size();
				if(argsNumber>0){					
					for(int j=0; j<argsNumber; j++)
						tmpString+=delimiter+predicateArguments.get(j).get(i);					
				}
				tmpString+=delimiter+coreferenceColumn.get(i);
				newWriter.write(tmpString.trim()+"\n");
			}
		}
	}
	
	
	/**
	 * Store sentence in file
	 * @throws IOException 
	 * 
	 */
	public void storeGeneratedSentence(FileWriter newWriter) throws IOException{	
		if(this.sentenceLength>0){
			for(int i=0;i<this.sentenceLength;i++){
				String tmpString="";
				tmpString+=" "+documentID;
				tmpString+=" "+partNumber;
				tmpString+=" "+sentenceSeq;
				tmpString+=" "+wordNumber.get(i);
				tmpString+=" "+form.get(i);
				tmpString+=" "+pos.get(i);
				tmpString+=" "+parseBit.get(i);
				tmpString+=" "+predicateLemma.get(i);
				tmpString+=" "+framesetID.get(i);
				tmpString+=" "+wordSense.get(i);
				tmpString+=" "+speaker.get(i);
				tmpString+=" "+namedEntityColumn.get(i);
				int argsNumber = predicateArguments.size();
				if(argsNumber>0){					
					for(int j=0; j<argsNumber; j++)
						tmpString+=" "+predicateArguments.get(j).get(i);					
				}
				tmpString+=" "+coreferenceColumn.get(i);
				newWriter.write(tmpString.trim()+"\n");
			}
		}
		newWriter.write("\nparse tree:\n\t");
		this.parse.store(newWriter);		
		if(this.semantic.predicateNumber>0){
			newWriter.write("\nsemantic relation:\n");
			this.semantic.storeSR(newWriter);
		}
		if(namedEntity.size()>0){
			newWriter.write("\nnamed entities:");
			for(int i=0; i<namedEntity.size(); i++){
				newWriter.write("\n\t"+namedEntity.get(i).label+" "+namedEntity.get(i).start+" "+namedEntity.get(i).end);
			}
		}
		newWriter.write("\n");
		/*
		newWriter.write("\nparse tree:\n\t");
		this.parse.storeCTree(newWriter);		
		if(this.semantic.predicateNumber>0){
			newWriter.write("\nsemantic relation:\n");
			this.semantic.storeSR(newWriter);
		}
		if(namedEntity.size()>0){
			newWriter.write("\nnamed entities:");
			for(int i=0; i<namedEntity.size(); i++){
				newWriter.write("\n\t"+namedEntity.get(i).label+" "+namedEntity.get(i).start+" "+namedEntity.get(i).end);
			}
		}
		if(coreference.size()>0){
			newWriter.write("\n\ncoreferences:");
			for(int i=0; i<coreference.size(); i++){
				newWriter.write("\n\t"+coreference.get(i).getLabel()+" "+coreference.get(i).getStart()+" "+coreference.get(i).getEnd());
			}
		}	
		newWriter.write("\n");
		*/
	}
	
	
	
	
	
	
	
	
	
	private static void test(){	
		ArrayList<String> a = new ArrayList<String>();
		a.add("-");
		a.add("-");
		a.add("-");
		a.add("(134|(165");
		a.add("165)");
		a.add("-");
		a.add("(2");
		a.add("134)|2)");
		a.add("-");
		int aLength = a.size();
		int i=0;
		while(i<aLength){
			String[] parts = a.get(i).split("\\|");
			for(String part : parts){
				if(part.startsWith("(")){
					if(part.endsWith(")"))
						System.out.println(part.substring(1,part.length()-1)+" "+i+" "+i);
					else{
						String label = part.substring(1);
						System.out.print(label);
						System.out.print(" "+i);
						int j=i+1;
						int sign =0;
						while(j<aLength){
							String[] sparts = a.get(j).split("\\|");
							for(String spart : sparts){
								if(spart.endsWith(")")){
									int length = spart.length();
									if(spart.substring(0,length-1).equals(label)){
										System.out.println(" "+j);
										sign=1;
										break;
									}								
								}							
							}
							if(sign==1)
								break;
							j++;						
						}
						if(j==aLength)
							System.out.println(" "+Integer.toString(j-1));	
					}
				}
			}
			i++;
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
}
