package edu.hitsz.algorithm.trie;


/**
 * 英语trie
 * @author Xinxin Li
 * @since Oct 27, 2012
 */
public class TrieEnglish {

	int count;
	TrieEnglish[] next;
    boolean exist;
   
    public TrieEnglish() {
        count = 0;
        exist = false;
        next = new TrieEnglish[26];
    }
      
	void insert(String word) {
       
		int wordLength = word.length();
        if(wordLength > 0) {
            char character = word.charAt(0);
            int id = character - 'a';
        	//如果不存在
        	if(next[id] == null) {
        		TrieEnglish trieNode = new TrieEnglish();
        		next[id] = trieNode;
        		if(wordLength == 1) {
        			next[id].count = 1;
        			next[id].exist = true;
        		}
        		else {
        			String subWord = word.substring(1);
        			next[id].insert(subWord);
        		}
        	}
        	else {
        		TrieEnglish trieNode = next[id];
        		trieNode.count += 1;
        		if(wordLength > 1) {
        			String subWord = word.substring(1);
        			next[id].insert(subWord);
        		}
            } 
        }              
    }
	
	
	public boolean search(String word) {
		
		int wordLength = word.length();
        if(wordLength > 0) {
            char character = word.charAt(0);
            int id = character - 'a';
        	//如果不存在
            if(wordLength == 1 && next[id] != null && next[id].count > 0) {
            	return true;
            }
        	if(next[id] == null) {
        		return false;
        	}
        	else {
        		TrieEnglish trieNode = next[id];
        		if(trieNode.count == 0)
        			return false;
        		if(wordLength > 1) {
        			String subWord = word.substring(1);
        			return trieNode.search(subWord);
        		}
            } 
        } 
        return false;	
		
	}
	

	    
	
	public static void main(String[] args) {
		TrieEnglish trie = new TrieEnglish();
		trie.insert("abc");
		trie.insert("ab");
		trie.insert("acb");
		System.out.println(trie.search("abc"));
		System.out.println(trie.search("ac"));
		
	}
    
    
	
	
}
