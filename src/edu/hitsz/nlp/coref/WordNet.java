/**
 *
 */
package edu.hitsz.nlp.coref;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.CfgNode;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.Pointer;

/**
 * @author tm
 *
 */
public class WordNet {
	IDictionary dict;

	public WordNet(){
		String path = "/usr/local/WordNet-3.0" + File.separator + "dict";
		URL url = null;
		try{
			url = new URL("file", null, path);
		}
		catch (MalformedURLException e){
			e.printStackTrace();
		}
		if(url == null)
			return;
		//construct the dictionary object and open it
		dict = new Dictionary(url);
		dict.open();
	}



	public void testDictionary(){

		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
		IWordID wordID = idxWord.getWordIDs().get(0);
		IWord word = dict.getWord(wordID);
		//System.out.println("Id = " + wordID);
		//System.out.println("Lemma = " + word.getLemma());
		//System.out.println("Gloss = " + word.getSynset().getGloss());
	}



	public ArrayList<String> getSynonyms(String form, String pos){
		ArrayList<String> newSynonyms = new ArrayList<String>();
		// look up first sense of the word "dog"
		IIndexWord idxWord;
		if(pos.startsWith("N"))
			idxWord = dict.getIndexWord(form, POS.NOUN);
		else if(pos.startsWith("V"))
			idxWord = dict.getIndexWord(form, POS.VERB);
		else
			return null;
		try{
			IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();
			// iterate over words associated with the synset
			for(IWord w : synset.getWords()) {
				//System.out.println(w.getLemma());
				newSynonyms.add(w.getLemma());
			}
			return newSynonyms;
		}
		catch( NullPointerException e){
			return null;
		}
	}


	/**
	 * get hyper class of inputting form with pos, without top. top=0 means all
	 * @param form
	 * @param pos
	 * @param top
	 * @return
	 */
	public ArrayList<String> getHypernyms(String form, String pos, int top){
		if(!pos.startsWith("N") && !pos.startsWith("V") && !pos.startsWith("JJ") && !pos.startsWith("RB")){
			return null;
		}
		else{
			try{
				ArrayList<ISynset> newSynsets = new  ArrayList<ISynset>();
				ArrayList<String> words = new ArrayList<String>();
				if(pos.startsWith("N")){
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.NOUN);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("V")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.VERB);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("JJ")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.ADJECTIVE);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("RB")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.ADVERB);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				int sig = 1;
				int start = 0;
				int level =0;
				ArrayList<Integer> levelNumber = new ArrayList<Integer>();
				while(sig == 1){
					sig = 0;
					int newSynsetsNumber = newSynsets.size();
					levelNumber.add(newSynsetsNumber-start);
					level ++;
					for(int i=start; i<newSynsetsNumber; i++){
						//System.out.println(level);
						ISynset tmpSynset = newSynsets.get(i);
						//System.out.println(tmpSynset.getWords().get(0).getLemma());
						words.add(tmpSynset.getWords().get(0).getLemma());
						List<ISynsetID> hypernyms =tmpSynset.getRelatedSynsets(Pointer.HYPERNYM);
						if(hypernyms.size()>0){
							sig = 1;
							for(ISynsetID sid : hypernyms){
								if(!newSynsets.contains(dict.getSynset(sid)))
									newSynsets.add(dict.getSynset(sid));
							}
						}
					}
					start = newSynsetsNumber;
				}
				//delete the top 3 level
				//System.out.println(levelNumber);
				if(top == 0)
					return words;
				if(levelNumber.size()>top){
					for(int i=levelNumber.size()-1; i>=levelNumber.size()-top; i--){
						for( int j=0; j<levelNumber.get(i); j++)
							words.remove(words.size()-1);
					}
					return words;
				}
				return null;
			}
			catch( NullPointerException e){
				return null;
			}
		}
	}


	public ArrayList<String> getHyponyms(String form, String pos){
		if(!pos.startsWith("N") && !pos.startsWith("V") && !pos.startsWith("JJ") && !pos.startsWith("RB")){
			return null;
		}
		else{
			try{
				ArrayList<ISynset> newSynsets = new  ArrayList<ISynset>();
				ArrayList<String> words = new ArrayList<String>();
				if(pos.startsWith("N")){
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.NOUN);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("V")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.VERB);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("JJ")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.ADJECTIVE);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				else if(pos.startsWith("RB")) {
					IIndexWord idxWord;
					idxWord = dict.getIndexWord(form.toLowerCase(), POS.ADVERB);
					IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
					IWord word = dict.getWord(wordID);
					ISynset synset = word.getSynset();
					newSynsets.add(synset);
				}
				int sig = 1;
				int start = 0;
				int level =0;
				while(sig == 1){
					sig = 0;
					int newSynsetsNumber = newSynsets.size();
					level ++;
					for(int i=start; i<newSynsetsNumber; i++){
						//System.out.println(level);
						ISynset tmpSynset = newSynsets.get(i);
						//System.out.println(tmpSynset.getWords().get(0).getLemma());
						words.add(tmpSynset.getWords().get(0).getLemma());
						List<ISynsetID> hyponyms =tmpSynset.getRelatedSynsets(Pointer.HYPONYM);
						if(hyponyms.size()>0){
							sig = 1;
							for(ISynsetID sid : hyponyms){
								newSynsets.add(dict.getSynset(sid));
							}
						}
					}
					start = newSynsetsNumber;
				}
				return words;
			}
			catch( NullPointerException e){
				return null;
			}
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		//testDictionary2();
		WordNet newNet = new WordNet();
		System.out.println(newNet.getHypernyms("dog","NOUN",0));
	}
}
