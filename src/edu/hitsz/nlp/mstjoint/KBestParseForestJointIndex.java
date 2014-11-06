package edu.hitsz.nlp.mstjoint;

import java.util.ArrayList;

public class KBestParseForestJointIndex {

	
	public static int rootType;

    public ParseForestItemJoint[][][][][] chart;
    private String[] sent,pos;
    private int start,end;
    private int K;

    /**
     * 初始化
     * @param start
     * @param end
     * @param inst
     * @param K
     */
    public KBestParseForestJointIndex(int start, int end, DependencyInstanceJoint inst, int K) {
		this.K = K;
		chart = new ParseForestItemJoint[end+1][end+1][2][2][K];
		this.start = start;
		this.end = end;
		this.sent = inst.forms;
		this.pos = inst.postags;
    }
	
    /**
     * 添加初始span
     * @since Mar 26, 2012
     * @param s 开始位置
     * @param type 依存类型
     * @param dir 方向
     * @param comp 完整性，总为1
     * @param score 概率
     * @param leftK 左边词索引
     * @param rightK 右边词索引
     * @param K
     * @param leftWP 左边词
     * @param rightWP 右边词
     * @param allWP
     * @return
     */
    public boolean addLR(int s, 
 		   int type, 
 		   int dir, 
 		   int comp,
 		   double score, 
 		   int leftK, int rightK, 
 		   int k,
 		   WordPos leftWP, WordPos rightWP,
 		   ArrayList<WordPos> allWP) {
 		
 		if(chart[s][s][dir][0][k] == null || chart[s][s][dir][0][k].prob < score) {
 			chart[s][s][dir][0][k] =
 				new ParseForestItemJoint(s,s,type,dir,comp,score,null,null,null,leftK,rightK,leftWP,rightWP, allWP);
 			return true;
 		}		
 		return false;
 	   
    }
    
    
    
    public boolean addLR(int s, int t,
    		int dir,
    		int comp,
    		int k,
    		ParseForestItemJoint item) {
    	
    	chart[s][t][dir][comp][k] = item;
    	return true;    	
    }
    
    
    
    
    public FeatureVector getFeatureVector(DependencyPipeJoint pipe, 
    		DependencyInstanceJoint inst, String bestParse){
    	String[] res = bestParse.split(" ");
	   for(int j = 0; j < res.length; j++) {
			String[] trip = res[j].split("[\\|:]");
			inst.deprels[j+1] = pipe.types[Integer.parseInt(trip[2])];
			inst.heads[j+1] = Integer.parseInt(trip[0]);
	    }
	   return pipe.createFeatureVector(inst, true);
    	
    }
    
    
	
	
}
