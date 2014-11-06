package edu.hitsz.nlp.transDep.fileformat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;
import edu.hitsz.nlp.transDep.DepItem;
import edu.hitsz.nlp.transDep.DepState;
import edu.hitsz.nlp.transDep.InputSequence;
import edu.hitsz.nlp.transDep.TransOptions;
import edu.hitsz.nlp.transDep.TransPipe;
import edu.hitsz.nlp.transDep.TransPipeArcStandardHuang2010;


/** 格式转换 */
public class DepConversion {
	
	public ArrayList<DepState> states;
	
	public DepConversion() {
		states = new ArrayList<DepState>();
	}	
	
	//Corbit Malt Format (default)
	//Each line represents a word. Sentences are separated by a single blank line.
	//[index] .... [word form] .... [POS] .... [head index]
	//[index] .... [word form] .... [POS] .... [head index]
	//[index] .... [word form] .... [POS] .... [head index]
 
	//[index] .... [word form] .... [POS] .... [head index]
	//[index] .... [word form] .... [POS] .... [head index]
	public void storeCorbitMalt(String corbitCTBFileName) throws IOException {
		
		FileWriter writer = new FileWriter(corbitCTBFileName);		
		for(DepState state : states) {
			state.reduceIndex();
			state.reduceHead();
			String sentence = getCorbitMaltSentence(state);
			writer.write(sentence+"\n");
		}
		writer.close();		
	}
	
	/** 获得CorbitCTBSentence的句子 */
	public String getCorbitMaltSentence(DepState state) {
		StringBuffer strbuf = new StringBuffer();
		InputSequence sequence = state.sequence;
		String[] words = sequence.words;
		String[] postags = sequence.postags;
		int[] heads = sequence.heads;
		String[] deprels = sequence.deprels;
		for(int i=0; i<sequence.wordSize; i++) {
			//strbuf.append(item.index); strbuf.append("\t");
			strbuf.append(words[i]);strbuf.append("\t");
			strbuf.append(postags[i]);strbuf.append("\t");
			strbuf.append(heads[i]); strbuf.append("\t");
			strbuf.append(deprels[i]); strbuf.append("\n");	
		}
		return strbuf.toString();		
	}
	
	//Corbit CTB Format
	//Each line represents one sentence. Word indices start from 0, and the head index of -1 indicates a dependency to the root. Note that word forms, POS, and head indices must be put in parentheses.
    //[index]:([word form])_([POS])_([head index]) [index]:([word form])_([POS])_([head index]) ...
	//[index]:([word form])_([POS])_([head index]) [index]:([word form])_([POS])_([head index]) ..	
	/** 
	 * 存储
	 * @since May 7, 2013
	 * @param corbitCTBFileName
	 * @throws IOException 
	 */
	public void storeCorbitCTB(String corbitCTBFileName) throws IOException {
		
		FileWriter writer = new FileWriter(corbitCTBFileName);		
		for(DepState state : states) {
			state.reduceIndex();
			state.reduceHead();
			String sentence = getCorbitCTBSentence(state);
			writer.write(sentence+"\n");
		}
		writer.close();		
	}
	

	
	
	/** 获得CorbitCTBSentence的句子 */
	public String getCorbitCTBSentence(DepState state) {
		StringBuffer strbuf = new StringBuffer();
		InputSequence sequence = state.sequence;
		String[] words = sequence.words;
		String[] postags = sequence.postags;
		int[] heads = sequence.heads;
		String[] deprels = sequence.deprels;
		for(int i=0; i<sequence.wordSize; i++) {
			strbuf.append(i); strbuf.append(":");
			strbuf.append("("); strbuf.append(words[i]); strbuf.append(")"); 
			strbuf.append("_(");strbuf.append(postags[i]);strbuf.append(")");
			strbuf.append("_(");strbuf.append(heads[i]);  strbuf.append(")");	
			strbuf.append("_(");strbuf.append(deprels[i]);  strbuf.append(")");						
			strbuf.append(" ");
		}
		return strbuf.toString().trim();		
	}
	
	
	/** */
	public void readConllMalt(String maltFileName) throws IOException {
		
		TransOptions options = new TransOptions();
		TransPipeArcStandardHuang2010 pipe = new TransPipeArcStandardHuang2010(options);
		pipe.initInputFile(maltFileName);		
		DependencyInstanceJoint instance = pipe.depReader.getNext();   
		int count = 0;		
		while(instance != null) {
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count % 1000 == 0) {
					System.out.println();					
				}
			}
			count++;
			InputSequence sequence = InputSequence.getInputSequence(instance.forms, 
					instance.postags, instance.heads, instance.deprels, 
					options.decodingMethod, false);
			DepState state = new DepState(TransPipe.getPipe(options), sequence);
			state.setAllItems(instance);
			states.add(state);
			instance = pipe.depReader.getNext();			
		}
	}
	
	


	
	public static void main(String[] args) throws IOException {
		
		String type = "train"; //train,dev,test
		String maltFileName = "/home/tm/disk/disk1/transDep/ctb5-" + type + "-dep-labelled-UTF-8";
		String corbitMaltFileName = "/home/tm/disk/disk1/transDep/ctb5-" + type + "-dep-labelled-UTF-8.corbitMalt";
		String corbitCTBFileName = "/home/tm/disk/disk1/transDep/ctb5-" + type + "-dep-labelled-UTF-8.corbitCTB";
		
		DepConversion conv = new DepConversion();
		conv.readConllMalt(maltFileName);
		conv.storeCorbitMalt(corbitMaltFileName);
		//conv.storeCorbitCTB(corbitCTBFileName);
		
		
	}
	
}
