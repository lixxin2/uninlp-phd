package edu.hitsz.nlp.corpus;

import java.util.ArrayList;

public class OntoNotes {

	/** OntoNotes中所有CTB的词性 */
	public static final String[] ctbpostags = {
		"AD","AS","BA","CC","CD","CS","DEC","DEG","DER","DEV","DT",
		"ETC","FW","JJ","LB","LC","M","MSP","NN","NR","NT","OD","P",
		"PN","PU","SB","SP","VA","VC","VE","VV"};

	public static ArrayList<String> ctbpostagsArr;
	
	static {
		ctbpostagsArr = new ArrayList<String>();
		for(int i=0; i<ctbpostags.length; i++){
			ctbpostagsArr.add(ctbpostags[i]);
		}
	}

	
	/** OntoNotes中所有数字 */
	public static String[] numbers = {
		"０","１","２","３","４","５","６","７","８","９",
		"零","一","二","三","四","五","六","七","八","九",
		"十","百","千","万","亿"};

	/** OntoNotes中所有日期，年月日 */
	public static String[] dates ={"月","日","年"};

	/** OntoNotes中所有英文字母（中文编码，长字） */
	public static String[] letters ={
		"ａ","","","","","","","","","","","","","",
		"","","","","","","","","","","","","","",
		"Ａ","Ｂ","Ｃ","","Ｅ","","Ｇ","Ｈ","Ｉ","","","Ｌ","Ｍ","Ｎ",
		"Ｏ","Ｐ","","Ｒ","Ｓ","Ｔ","","Ｖ","","","",""
	};

	/** OntoNotes中所有标点 */
	public static String[] punctuations = {
		"。","？","！","，","、","；","：","“","”","．",
		"﹁","﹂","‘","’","﹃","﹄","『","』",
		"（","）","［","］","〔","〕","【","】","《","》","〈","〉",
		"…","……","-","—","━","——","──","－－","～","·","﹏","﹏﹏","＿＿"};

	public static String[] endPunctuations = {"。","？","！"};

	public OntoNotes() {
			
	}




}
