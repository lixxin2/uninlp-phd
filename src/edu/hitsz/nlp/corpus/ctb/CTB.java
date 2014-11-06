package edu.hitsz.nlp.corpus.ctb;



/**
 * CTB语料库中所有的常量，是通过统计得到的，不一定全
 * @author Xinxin Li
 * @since Jan 9, 2012
 */
public class CTB {

	/** CTB中所有词性 */
	public static String[] ctbPosTags = {
		"AD","AS","BA","CC","CD","CS","DEC","DEG","DER","DEV",
		"DT","ETC","FW","IJ","JJ","LB","LC","M","MSP","NN",
		"NR","NT","OD","ON","P","PN","PU","SB","SP","URL",
		"VA","VC","VE","VV","X"};
	
	public static String[] ctbClosedSetPosTags = {
		"AS","BA","CC","CS","DEC","DEG","DER","DEV",
		"DT","ETC","IJ","LB","LC",
		"P","PN","PU","SB","SP",
		"VC","VE"
	};

	/** CTB中所有数字 */
	public static String[] arabNumbers = {
		"０","１","２","３","４","５","６","７","８","９",
	};
	
	public static String[] hanNumbers = {
		"零","一","二","三","四","五","六","七","八","九",
		"十","百","千","万","亿"
	};

	/** CTB中所有日期，年月日 */
	public static String[] dates ={"月","日","年","时","分","秒"};

	/** CTB中所有英文字母（中文编码，长字） */
	public static String[] letters ={
		"ａ","ｂ","ｃ","ｄ","ｅ","ｆ","ｇ","ｈ","ｉ","ｊ","ｋ","ｌ","ｍ","ｎ",
		"ｏ","ｐ","ｑ","ｒ","ｓ","ｔ","ｕ","ｖ","ｗ","ｘ","ｙ","ｚ",
		"Ａ","Ｂ","Ｃ","Ｄ","Ｅ","Ｆ","Ｇ","Ｈ","Ｉ","Ｊ","Ｋ","Ｌ","Ｍ","Ｎ",
		"Ｏ","Ｐ","Ｑ","Ｒ","Ｓ","Ｔ","Ｕ","Ｖ","Ｗ","Ｘ","Ｙ","Ｚ"
	};

	/** CTB中所有标点 */
	public static String[] punctuations = {
		"。","？","！","，","、","；","：","．","·","・",
		"“","”","‘","’","＂",
		"﹁","﹂","﹃","﹄","『","』","「","」",
		"（","）","〈","〉","[","]","［","］","〔","〕","【","】","《","》",
		"-","－","―","—","━","——","──","———","－－",
		"…","……","﹏","﹏﹏","＿","＿＿",
		"％","／","～"
		};

	public static String[] endPunctuations = {"。","？","！","，","；","："};



	





}
