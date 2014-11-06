package edu.hitsz.nlp.mstjoint;

import java.util.ArrayList;


/**
 * 句法span
 * @author tm
 *
 */
public class ParseForestItemJoint {

    public int s,r,t,dir,comp,length,type;   
    public double prob; 
    public FeatureVector fv;
    
    public ParseForestItemJoint left, right;

    public int leftK, rightK; //左右词索引
    public WordPos leftWP, rightWP; //左右词  
    ArrayList<WordPos> allWP;
    
    // productions
    public ParseForestItemJoint(
    		int i, int k, int j,  		
    		int type,
			int dir,  
			int comp,
			double prob,
			FeatureVector fv,
			ParseForestItemJoint left, ParseForestItemJoint right,
			int leftK, int rightK,  
			WordPos leftWP, WordPos rightWP,
			ArrayList<WordPos> allWP) {
    	
		this.s = i;
		this.r = k;
		this.t = j;

		this.type = type;
		this.dir = dir;
		this.comp = comp;		
		length = 6;

		this.prob = prob;
		this.fv = fv;

		this.left = left;
		this.right = right;		

		this.leftK = leftK;
		this.rightK = rightK;
		
		this.leftWP = leftWP;
		this.rightWP = rightWP;
		
		this.allWP = allWP;
		
    }
    

    /**
     * 
     * @since Mar 26, 2012
     * @param s 开始位置
     * @param t 结束位置
     * @param type 依存类型
     * @param dir 方向
     * @param comp 完整span or not
     * @param prob 概率
     * @param fv
     * @param left
     * @param right
     * @param leftK 左词索引
     * @param rightK 右词索引
     * @param leftWP 左词
     * @param rightWP 右词
     * @param allWP
     */
    public ParseForestItemJoint(
    		int s, int t,   
    		int type,
			int dir,  
			int comp,
			double prob,
			FeatureVector fv,
			ParseForestItemJoint left, ParseForestItemJoint right,
			int leftK, int rightK,  
			WordPos leftWP, WordPos rightWP,
			ArrayList<WordPos> allWP) {
    	
		this.s = s;
		this.t = t;

		this.type = type;
		this.dir = dir;
		this.comp = comp;		
		length = 2;

		this.prob = prob;
		this.fv = fv;

		this.left = left;
		this.right = right;		

		this.leftK = leftK;
		this.rightK = rightK;
		
		this.leftWP = leftWP;
		this.rightWP = rightWP;
    		
		this.allWP = allWP;
		
    }
    
    

    public ParseForestItemJoint() {}
    
    
    
    public String getParse() {
		if(left == null || right == null) {
			if(dir == 1) {
				return " "+leftWP.getStart()+":"+leftWP.getEnd()+":"+
						leftWP.getWord()+":"+leftWP.getPos();
			}
			else {
				return " "+rightWP.getStart()+":"+rightWP.getEnd()+
						":"+rightWP.getWord()+":"+rightWP.getPos();
			}			
		}
		else {
		    return (left.getParse()+" "+right.getParse()).trim();
		}
    }
    
    public String getDep() {
    
    	if(comp == 1) {
    		if(left == null || right == null)
    			return "s:"+s+":t:"+t+":dir:"+dir+":comp:"+comp+":leftK:"+leftK+":rightK:"+rightK;
    		else {
				return left.getDep()+"s:"+s+":t:"+t+":dir:"+dir+":comp:"+comp+":leftK:"+leftK+":rightK:"+rightK+right.getDep();
    		}    			
    	}
    	else {
    		if(left == null || right == null)
    			return " ";
    		else
    			return left.getDep()+right.getDep();        	
    	}
    	
    }
    
    
    public DependencyInstanceJoint getBestParse( ) {
    	
    	String parse = getParse();
    	String[] subs = parse.split(" ");
    	int i = 0;
    	ArrayList<WordPos> allWP = new ArrayList<WordPos>();
    	WordPos preWP = null;
    	for(String sub : subs) {
    		if(sub.length() > 1) {
    			if(i> 1 && i % 2 == 0){    				
    				String[] parts = sub.split(":");
    				WordPos wp = new WordPos(parts[2], parts[3], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    				if(preWP == null || wp.getStart() != preWP.getStart()) {
    					allWP.add(wp);
    					preWP = wp;
    				}
    			}    
    			i++;
    		}
    	}
    	int length = allWP.size();
    	String[] forms = new String[length];
    	String[] poses = new String[length];
    	int[] heads = new int[length];
    	
    	//
    	String dep = getDep();
    	String[] res = dep.split(" ");

	    for(String oneDep : res) {
	    	if(oneDep.length()>12){
		    	String[] trip = oneDep.split(":");
		    	int s = Integer.parseInt(trip[1]);
		    	int t = Integer.parseInt(trip[3]);
		    	int dir = Integer.parseInt(trip[5]);
		    	int start = 0;
		    	if(s == 0) start = 0;
		    	else for(; start<length; start++){
		    		if(allWP.get(start).getStart() == s) 
		    			break;
		    	}
		    	int end = 0;
		    	for(; end<length; end++){
		    		if(allWP.get(end).getEnd() == t) 
		    			break;
		    	}
		    	if(dir == 0) {
		    		if(s == 0)
		    			heads[end] = start;
		    		else
		    			heads[end] = start+1;
		    	}
		    	else {
		    		heads[start] = end+1;
		    	}
	    	}
	    }
    	for(i=0; i<length; i++) {
    		forms[i] = allWP.get(i).getWord();
    		poses[i] = allWP.get(i).getPos();
    	}  	
    	
    	return new DependencyInstanceJoint(forms, poses, heads);
    	
    }
    
    
    
    
    

    public void copyValues(ParseForestItemJoint p) {
		p.s = s;
		p.r = r;
		p.t = t;
		p.dir = dir;
		p.comp = comp;
		p.prob = prob;
		p.length = length;
		p.left = left;
		p.right = right;
		p.type = type;
    }

    // way forest works, only have to check rule and indeces
    // for equality.
    public boolean equals(ParseForestItemJoint p) {
		return s == p.s && t == p.t && r == p.r
		    && dir == p.dir && comp == p.comp
		    && type == p.type;
    }

    public boolean isPre() { return length == 2; }
    
    
    
    
    /**
     * 得到依存span的特征向量
     * @param pfi
     * @return
     */
    public FeatureVector getFeatureVector() {
    	
		if(left == null)
		    return fv;

		return fv.cat(left.getFeatureVector()).cat(right.getFeatureVector());
    }
   
    
    /**
     * 得到span的字符串
     * @param pfi
     * @return
     */
    public String getDepString(ParseForestItemJoint pfi) {
		if(pfi.left == null || pfi.right == null) {
			if(pfi.dir == 0) {
				return " "+pfi.leftWP.getStart()+"|"+pfi.leftWP.getEnd()+":"+
						pfi.leftWP.getWord()+":"+pfi.leftWP.getPos();
			}
			else {
				return " "+pfi.rightWP.getStart()+"|"+pfi.rightWP.getEnd()+
						":"+pfi.rightWP.getWord()+":"+pfi.rightWP.getPos();
			}			
		}
		else {
		    return (getDepString(pfi.left)+" "+getDepString(pfi.right)).trim();
		}
    }
    
    
    

}
