package edu.hitsz.ml.maxent.lindekang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Establish a mapping between strings and integers so that a string
 * can be converted to an integer id as well as the other way
 * around. The integer id 0 is always reserved for the empty string. The
 * ids of other strings begin with 1.
 * 
 * <p> 建立一个将字符串和数字进行相互转换的Map
 */
public class Str2IdMap {
	
	HashMap<String, Integer> _toId; // mapping from string to id
	ArrayList<String> _toStr;            // mapping from id to string

	/** return the string corresponding to an id 
	 *  <p> 返回字符串对应的数字id
	 */
	String getStr(int id) {
		return _toStr.get(id);
	}

	Str2IdMap() {
		_toId = new HashMap<String, Integer>();
		_toStr = new ArrayList<String>();
		_toStr.add("");
	}


	/**
	 *
	 * Return the id corresponding to the string. If it is not
     * currently in the mapping, it will be added to the mapping and
     * assigned an id.
     * <p> 将字符串特征转换为数字特征，如果没有就添加到Map
     *
     * @param str
     * @return
    */
	public int getId(String str){
		if(!_toId.containsKey(str)){
			int id = _toStr.size();
			_toId.put(str, id);
			_toStr.add(str);
			return id;
		}
		else
			return _toId.get(str);
	}

	/**
	 * Like getId() except that if the string is not currently in the
     * mapping, the id 0 will be returned and it is NOT added to the
     * mapping.
     * 
     * <p> 类似于getId(), 只是如果没有该字符串特征，则返回0
     *
	 * @param str
	 * @return
	 */
	public int getExistingId(String str){
		if(!_toId.containsKey(str))
			return 0;
		else
			return _toId.get(str);
	}


	/**
	 * Convert the sequence of tokens in line into a sequence of
	 * integer ids. delim is the separator between the tokens.
	 * <p>
	 * @param line
	 * @param seq
	 * @param delim
	 */
	public void getIds(String line, ArrayList<Integer> seq, String delim){
		String[] words = line.split(delim);
		for(int i=0; i<words.length; i++)
			seq.add(this.getId(words[i]));
	}

	/**
	 * 将一行训练数据的特征vec转换为数字vec后，存入seq中
	 * @param words
	 * @param seq
	 */
	public void getIds(String[] words, ArrayList<Integer> seq){
		for(int i=1; i<words.length; i++)
			if(words[i].length()>0)
				seq.add(this.getId(words[i]));
	}


}
