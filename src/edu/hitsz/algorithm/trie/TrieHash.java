package edu.hitsz.algorithm.trie;

import java.util.HashMap;

public class TrieHash {
	
	int count;
	boolean exist;
	HashMap<String, TrieHash> next;
	
	public TrieHash() {
		count = 0;
        exist = false;
        next = new HashMap<String, TrieHash>();
	}
	
	
	void insert(String word) {
		
		int wordLength = word.length();
        if(wordLength > 0) {
            String character = word.substring(0,1);            
        	//如果不存在
        	if(!next.containsKey(character)) {        		
        		TrieHash trieNode = new TrieHash();
        		next.put(character, trieNode);
        		if(wordLength == 1) {
        			trieNode.count = 1;
        			trieNode.exist = true;
        		}
        		else {
        			String subWord = word.substring(1);
        			trieNode.insert(subWord);
        		}
        	}
        	else {
        		TrieHash trieNode = next.get(character);
        		trieNode.count += 1;
        		if(wordLength > 1) {
        			String subWord = word.substring(1);
        			trieNode.insert(subWord);
        		}
            } 
        }              
    }
	
	public boolean search(String word) {
		
		int wordLength = word.length();
        if(wordLength > 0) {
            String character = word.substring(0,1);
        	//如果不存在
            if(wordLength == 1 && next.containsKey(character) && next.get(character).count > 0) {
            	return true;
            }
        	if(next.get(character) == null) {
        		return false;
        	}
        	else {
        		TrieHash trieNode = next.get(character);
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
		TrieHash trie = new TrieHash();
		trie.insert("abc");
		trie.insert("ab");
		trie.insert("acb");
		System.out.println(trie.search("abc"));
		System.out.println(trie.search("ac"));
		
	}

}
