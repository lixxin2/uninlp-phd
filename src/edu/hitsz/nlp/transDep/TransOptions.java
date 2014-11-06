package edu.hitsz.nlp.transDep;

public class TransOptions {
	
	/** 不同的解析方法 
	 * wordArcStandardHuang2010
	 * wordArcEagerZhang2011
	 * charArcStandardLi2012
	 * charArcStandardCharHatori2012
	 */
	public String decodingMethod = "wordArcStandardHuang2010";
	public boolean train = false;
	public boolean dev = false;
	public boolean test = false;
	public boolean eval = true;	
	public boolean actionCheck = false;

	public boolean isSegPos = false; //是否包含分词和词性标注
	public boolean isLabeled = false;  
	public boolean isDp = true;
	

	public int K = 16;
	int numIters = 1;
	
	String trainFileName = "/home/tm/disk/disk1/transDep/ctb5-dev-dep-labelled-UTF-8";
	String modelFileName = "/home/tm/disk/disk1/transDep/dev-huang-noDp.model";
	String testFileName = "/home/tm/disk/disk1/transDep/ctb5-dev-dep-labelled-UTF-8";
	String resultFileName = "/home/tm/disk/disk1/transDep/ctb5-dev-dep-labelled-UTF-8.result.huang";
	
	String goldFileName = "/home/tm/disk/disk1/transDep/ctb5-dev-dep-labelled-UTF-8";
	
	
}
