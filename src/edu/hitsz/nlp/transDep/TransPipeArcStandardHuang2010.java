package edu.hitsz.nlp.transDep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;

public class TransPipeArcStandardHuang2010 extends TransPipe{

    public TransPipeArcStandardHuang2010(TransOptions options) throws IOException {
		super(options);		
	}
    
    /**  
     * 对单个Instance进行分析,解析出其所有动作
     * @since Feb 27, 2013
     * @param instance
     */
    public List<Pair<TransAction, DepState>> simulateGold(InputSequence goldSequence) {    	
        
    	DepState state = new DepState(this, goldSequence); 
    	
    	List<Pair<TransAction, DepState>> golds = new ArrayList<Pair<TransAction, DepState>>();  
		
		int totalActionNumber = 2*state.sequence.wordSize - 1;
		int actionNumber = 0;		
		
		while(actionNumber < totalActionNumber) {
			Pair<TransAction, DepState> next = moveNextGold(state, goldSequence,false);
			golds.add(next);
			state = next.second;
			actionNumber++;
		}			
		
		if(actionNumber != totalActionNumber) {
			System.out.println("there are different action numbers");
			System.exit(-1);
		}		
		
		return golds;		
    }	
    
    
    /** 移动到下一个gold状态
     * <p> 返回一个gold动作，gold状态
     */
    public Pair<TransAction, DepState> moveNextGold(DepState state, InputSequence goldSequence, boolean added) {
    	
    	TransAction action = getGoldAction(state, goldSequence);
    	List<DepState> nextStates = moveNext(state, action, true, added);
    	DepState goldState = null;

		for (DepState nextState : nextStates)
		{
			assert (!nextState.isGold || goldState == null);
			if (nextState.isGold) goldState = nextState;
		}

		// might not be true if pruning is disabled, because states are sorted during pruning process
		assert (goldState != null && goldState == nextStates.get(0));

		return new Pair<TransAction, DepState>(action, goldState);
    	
    }
    
    	
    /** 获得当前状态的下一个gold action */
    public TransAction getGoldAction(DepState state, InputSequence goldSequence) {
    	
    	if(state.isGold == false) return TransAction.NotAvailable;
    	//end
    	if(isEnd(state)) return TransAction.End;
    	
		//栈顶
		DepItem topItem = state.stack[0];
		DepItem top2Item = state.stack[1];
    	
    	//如果栈数量小于2，则shift
		if(top2Item != null) {		
			int[] heads = goldSequence.heads;
			//如果栈顶top2Item的父节点是栈顶topItem,RL
			if(top2Item.index != -1 && heads[top2Item.index] == topItem.index + 1) {
				return TransAction.reduceLeft(top2Item.deprel);
			}
			//如果栈顶topItem的父节点是栈顶top2Item, 
			//需要判断topItem是否在剩余序列中有sons,RR
			else if(heads[topItem.index] == top2Item.index + 1
					&& !state.existSon(topItem.index+1, heads, state.curIdx)) {
				return TransAction.reduceRight(topItem.deprel);
			}
		}
		//shift
		if(state.curIdx < state.sequence.wordSize) {
			return TransAction.shift();
		}			
		else {
			return TransAction.NotAvailable;
		}		
	}	    
    
    
    
	public boolean isEnd(DepState state) {
		if(state.curIdx == state.sequence.wordSize && state.stack[1] == null)
			return true;
		else
			return false;
	}
    
    
    /** 
	 * 单个状态，单个动作，可能有多个产生的状态 
	 * <p> 根据动作名称采取动作，返回下一个状态（新生成的，不是当前状态的修改）*/
	public List<DepState> moveNext(DepState state, 
			TransAction action, 
			boolean isGoldAction, boolean added) {

		List<DepState> l = new ArrayList<DepState>(); 
		
		if(action.isShift()) {
			DepState newState = state.shift(isGoldAction, added);
			if(newState != null)
				l.add(newState);
		}
		else if(action.isReduceLeft()) {
			l = state.reduceLeft(action.m_pos, isGoldAction, added);
		}
		else if(action.isReduceRight()) {
			l = state.reduceRight(action.m_pos, isGoldAction, added);
		}
		else if(action == TransAction.End)
			l.add(state);
		
		return l;
	}
	
	
	/** 单个初始状态，多个连续动作，产生一个最终的状态 */
	public DepState moveSequentActions(InputSequence goldSequence, List<TransAction> actions, boolean added) {

		DepState state = new DepState(this, goldSequence);
		
		for(TransAction action : actions) {
			List<DepState> states = moveNext(state, action, true, added);
			state = states.get(0);	
		}		
		return state;
	}
		
	
	/** 下一个可能的动作 */
    public List<TransAction> getNextActions(DepState state, InputSequence goldSequence) {
    	
    	assert(goldSequence == null);
    	List<TransAction> actions = new ArrayList<TransAction>();
    	
    	if(isEnd(state)) {
    		actions.add(TransAction.End);
    		return actions;
    	}
    	
    	//如果当前状态的位置 小于 输入序列长度，则可以有Shift操作
		if(state.curIdx < state.sequence.wordSize) 
			actions.add(TransAction.shift());
		
		//如果当前状态中栈里只有两个节点，则第二个栈顶元素为头结点时，不能reduce left
		//只有大于两个节点才能reduce left
		if(state.stack[1] != null) {
			//如果为label，则依赖关系不为ROOT，因为不是指向头结点
			if(isLabeled) {
				deprels.remove("ROOT");
				for(String deprel : deprels) 
					actions.add(TransAction.reduceLeft(deprel));
			}
			else
				actions.add(TransAction.reduceLeft(null));
			//
			if(isLabeled) {
				//如果只有两个元素
				if(state.stack[2] != null) {
					deprels.clear();
					deprels.add("ROOT");
				}
				for(String deprel : deprels) 			
					actions.add(TransAction.reduceRight(deprel));
			}
			else
				actions.add(TransAction.reduceRight(null));			
		}		
		assert(!actions.contains(null));
		return actions;    	
    }
    
    /** */
	public List<Pair<TransAction, DepState>> moveNext(DepState state, InputSequence goldSequence, boolean bAdd)
	{
		List<Pair<TransAction, DepState>> l = new ArrayList<Pair<TransAction, DepState>>();
		TransAction goldAction = goldSequence != null ? getGoldAction(state, goldSequence) : TransAction.NotAvailable;

		for (TransAction action : getNextActions(state, null))
			for (DepState nextState : moveNext(state, action, action.shallowEquals(goldAction), bAdd))
				l.add(new Pair<TransAction, DepState>(action, nextState));
		return l;
	}
    
	/**
     * 解析
     * @since Feb 27, 2013
     * @param instance
     * @param params 参数
     * @param K 
     * @param isTrain 是否训练。如果是，则true;
     * @param goldActions 
     * @param iter
     * @return
     */
    public DependencyInstanceJoint decoding(
    		InputSequence goldSequence, 
    		int K,
    		boolean isTrain,
    		int[] iter) {    
    	
    	InputSequence sequence = InputSequence.getRawInputSequence(goldSequence);

		DepState goldState = new DepState(this, sequence);
		DepState state = new DepState(this, sequence);
    	
		DepChart curChart = new DepChart(isDp);
		DepChart nextChart = new DepChart(isDp);	
		
		curChart.update(state);		

    	boolean bStopped = false;
    	int actionNumber = 0;  
    	while(true) {
    		
    		actionNumber++;	 
    		//System.out.println(actionNumber);
    		//如果训练时，找到下一个gold动作和状态
    		if(isTrain)
    			goldState = moveNextGold(goldState, goldSequence, false).second;
    		
    		nextChart.maps.clear();
    		InputSequence _goldSequence = isTrain ? goldSequence : null;    		
    		boolean bResult = updateChart(curChart, nextChart, _goldSequence, false);    		
    		if(!bResult) break;
    		if( K > 0 ) nextChart.prune(K);
    		    		
    		if(isTrain && nextChart.isEarlyStop()) {
    			bStopped = true;
    			break;
    		}
    		
    		DepChart _chart = curChart;
    		curChart = nextChart;
    		nextChart = _chart;    		
    	}

		DepState predState = nextChart.getBestState();	  	
    	if(isTrain) {
			DepFeatureVector goldFV = getFeatures(state, goldState);
			DepFeatureVector predictFV = getFeatures(state, predState);
			params.update(goldFV.fv, predictFV.fv, iter[0]);
			iter[0] += 1;	
			return null;
		}	
    	else {
    		String[] words = sequence.words;
    		String[] tags = sequence.postags;
    		int[] heads = predState.getParsedResult(words.length);
	    	DependencyInstanceJoint newInstance = new DependencyInstanceJoint(words, tags, heads);
	    	return newInstance;    
	    }
    }

    
    /** 根据当前DepChart，产生下一个DepChart */
	public boolean updateChart(DepChart curChart, DepChart nextChart, InputSequence sequence, boolean added) {
		
		boolean bAnyUpdated = false; //是否状态有更新，并不都是End
		boolean bEnd = false;        //是否结束了，没有下一个动作了，到句子结尾了
		
		for(DepState curState : curChart.maps.keySet()) {
			// 
    		List<Pair<TransAction, DepState>> nexts = moveNext(curState, sequence, false);
    		//
    		for(Pair<TransAction, DepState> next : nexts) {
    			TransAction nextAction = next.first;
    	    	DepState nextState = next.second;	
    	    	
    	    	if(nextAction == TransAction.End)
    	    		bEnd = true;
    	    	else
    	    		bAnyUpdated = true;	    	    	
    	    	assert(!bEnd || nextAction == TransAction.End);
    	    	
    	    	nextChart.update(nextState);	    			
    		}    		 
		}
		if(nextChart.maps.size() == 0)
			throw new RuntimeException("Unexpected error: no next state found");
		return bAnyUpdated;
	}
    
    
    /** 得到当前状态的所有特征 */
	public DepFeatureVector getFeatures(DepState state, DepState curState) {

		DepFeatureVector fvprf2 = new DepFeatureVector();
		List<TransAction> lAct = curState.getActionSequence();
		DepState startState = state;
		for (TransAction act : lAct)
			startState = moveNext(startState, act, false, true).get(0);
		DepState sss1 = startState;
		for (DepState sss2 = sss1; sss2 != null; sss2 = sss2.pred0)
		{
			for (DepFeatureVector v : sss2.fvins)
				fvprf2.add(v);
			if (sss2 != sss1)
				fvprf2.add(sss1.trans.get(sss2).first);
			sss1 = sss2;
		}
		return fvprf2;
	}
    
    

			
	
	
	  
	
    public DynamicFeatures resetDynamicFeatures(DepState state) {
    	
    	String[] features = new String[24]; //15,24
    			
    	DepState s1 = state.preds.size() > 0 ? state.pred0 : null;
    	
		DepItem top0= state.stack[0];
		DepItem top1 = state.stack[1];
		DepItem top2 = state.stack[2];
				
		//栈顶的左右子树，栈顶2的左右子树
		DepItem top0LeftSon = top0 != null && top0.leftSons.size() > 0 ? top0.leftSons.get(top0.leftSons.size()-1) : null;
		DepItem top0RightSon = top0 != null && top0.rightSons.size() > 0 ? top0.rightSons.get(top0.rightSons.size()-1) : null;
		DepItem top1LeftSon = top1 != null && top1.leftSons.size() > 0 ? top1.leftSons.get(top1.leftSons.size()-1) : null;
		DepItem top1RightSon = top1 != null && top1.rightSons.size() > 0 ? top1.rightSons.get(top1.rightSons.size()-1) : null;
		
		int idx = state.curIdx;
		
		String top0Word = top0.word;
		String top1Word = top1 != null ? top1.word : "$";
		//当前词，下一个词
		String seqp1Word = idx > 0 ? state.sequence.words[idx-1] : "$";
		String seqf1Word = idx < state.sequence.wordSize ? state.sequence.words[idx] : "$";

		String top0Tag = top0.postag;
		String top1Tag = top1 != null ? top1.postag : "$";
		String top2Tag = top2 != null ? top2.postag : "$";
		//当前词，前一个词的词性
		String seqp1Tag = idx > 0 ? state.sequence.postags[idx-1] : "$";
		String seqp2Tag = idx > 1 ? (state.bgn <= idx -2 ) ? state.pos[idx-2] : s1.pos[idx-2] : "$";
		
		String seqf1Tag = idx < state.sequence.wordSize ? state.sequence.postags[idx] : "$";
		String seqf2Tag = idx < state.sequence.wordSize - 1 ? state.sequence.postags[idx+1] : "$";
		
		String top0LeftSonTag = top0LeftSon != null ? top0LeftSon.postag : "$";
		String top0RightSonTag = top0RightSon != null ? top0RightSon.postag : "$";
		String top1LeftSonTag = top1LeftSon != null ? top1LeftSon.postag : "$";
		String top1RightSonTag = top1RightSon != null ? top1RightSon.postag : "$";
					
		String sPunct = (top0 != null && top1 != null) ? getPunctInBetween(state.sequence.words,
				state.sequence.postags, top0.index, top1.index) : "$";		
		boolean bAdjoin = (top0 != null && top1 != null && Math.abs(top1.index - top0.index) == 1);
		
		
		assert(top0Tag != null);
		assert(top1Tag != null);
		assert(top2Tag != null);
		assert(seqp1Tag != null);
		assert(seqp2Tag != null);
		assert(top0LeftSonTag != null);
		assert(top0RightSonTag != null);
		assert(top1LeftSonTag != null);
		assert(top1RightSonTag != null);
		
		features[0] = Integer.toString(state.curIdx);
		features[1] = Integer.toString(state.bgn);
		features[2] = Integer.toString(state.end);
		features[3] = Integer.toString(top0.index);
		features[4] = Integer.toString(top0.index >= 0 ? state.heads[top0.index] : -2);
		features[5] = Integer.toString(top1 != null ? top1.index : -2);
		features[6] = top0Word;
		features[7] = top1Word;
		features[8] = seqp1Word;
		features[9] = seqf1Word;
		features[10] = top0Tag;
		features[11] = top1Tag;
		features[12] = top2Tag;
		features[13] = seqp2Tag;
		features[14] = seqp1Tag;
		features[15] = seqf1Tag;
		features[16] = seqf2Tag;
		features[17] = top0RightSonTag;
		features[18] = top0LeftSonTag;
		features[19] = top1RightSonTag;
		features[20] = top1LeftSonTag;
		features[21] = sPunct;
		features[22] = Boolean.toString(bAdjoin);		
		features[23] = "$";
		
		return new DynamicFeatures(features);
		    		
    }
    
    
    public String getPunctInBetween(String[] words, String[] postags, int start, int end) {
    	if(start > end) {
    		int i = end;
    		end = start;
    		start = i;
    	}
    	for(int i=start+1; i<end; i++)
    		if("PU".equals(postags[i]))
    			return words[i];
    	return "NA";
    }
		 
	
	/**
	 * Liang Huang and Kenji Sagae 2010 
	 * @since Feb 26, 2013
	 * @return
	 */
	public DepFeatureVector getFeatureVector(DepState state,
			TransAction action,
			boolean added) {

		DepFeatureVector dfv = new DepFeatureVector();
		
		String actionName = action.getActionName();		
		String[] dynamicFeatures = state.features.features;		
				
		int curIdx = Integer.parseInt(dynamicFeatures[0]); 
		int bgn = Integer.parseInt(dynamicFeatures[1]); 
		int end = Integer.parseInt(dynamicFeatures[2]); 
		int top0Index = Integer.parseInt(dynamicFeatures[3]); 
		int top0Head = Integer.parseInt(dynamicFeatures[4]); 
		int top1Index = Integer.parseInt(dynamicFeatures[5]); 
		String top0Word = dynamicFeatures[6];
		String top1Word = dynamicFeatures[7];
		String seqp1Word = dynamicFeatures[8];
		String seqf1Word = dynamicFeatures[9];
		String top0Tag = dynamicFeatures[10];
		String top1Tag = dynamicFeatures[11];
		String top2Tag = dynamicFeatures[12];
		String seqp2Tag = dynamicFeatures[13];
		String seqp1Tag = dynamicFeatures[14];
		String seqf1Tag = dynamicFeatures[15];
		String seqf2Tag = dynamicFeatures[16];
		String top0RightSonTag = dynamicFeatures[17];
		String top0LeftSonTag = dynamicFeatures[18];
		String top1RightSonTag = dynamicFeatures[19];
		String top1LeftSonTag = dynamicFeatures[20];
		String sPunct = dynamicFeatures[21];
		boolean bAdjoin = Boolean.parseBoolean(dynamicFeatures[22]);
		
		final int length = state.sequence.wordSize;		
		
		ArrayList<String> features = new ArrayList<String>();	
		
		features.add("1:"+top0Word);
		features.add("2:"+top0Tag);
		features.add("3:"+top0Word+"_"+top0Tag);
		
		features.add("4:"+top1Word);
		features.add("5:"+top1Tag);
		features.add("6:"+top1Word+"_"+top1Tag);
		
		features.add("7:"+seqf1Word);		
		features.add("8:"+seqf1Tag);
		features.add("9:"+seqf1Word+"_"+seqf1Tag);
				
		features.add("10:"+top0Word+"_"+top1Word);
		features.add("11:"+top0Tag+"_"+top1Tag);
		
		features.add("12:"+top0Tag+"_"+seqf1Tag);
		
		features.add("13:"+top0Word+"_"+top0Tag+"_"+top1Tag);
		features.add("14:"+top0Word+"_"+top0Tag+"_"+top1Word);
		features.add("15:"+top0Word+"_"+top1Word+"_"+top1Tag);
		features.add("16:"+top0Tag+"_"+top1Word+"_"+top1Tag);
		features.add("17:"+top0Word+"_"+top0Tag+"_"+top1Word+"_"+top1Tag);
		
		features.add("18:"+top0Tag+"_"+seqf1Tag+"_"+seqf2Tag);
		features.add("19:"+top0Tag+"_"+top1Tag+"_"+seqf1Tag);
		features.add("20:"+top0Word+"_"+seqf1Tag+"_"+seqf2Tag);
		features.add("21:"+top0Word+"_"+top1Tag+"_"+seqf1Tag);
		
		features.add("22:"+top0Tag+"_"+top1Tag+"_"+top1LeftSonTag);
		features.add("23:"+top0Tag+"_"+top1Tag+"_"+top1RightSonTag);
		features.add("24:"+top0Tag+"_"+top0RightSonTag+"_"+top1Tag);
		features.add("25:"+top0Tag+"_"+top0LeftSonTag+"_"+top1Tag);
		
		features.add("26:"+top0Word+"_"+top1Tag+"_"+top1RightSonTag);
		features.add("27:"+top0Word+"_"+top1Tag+"_"+top0LeftSonTag);
		
		features.add("28:"+top0Tag+"_"+top1Tag+"_"+top2Tag);
		

		for(String feature : features) {
			add(feature + "_" + actionName, dfv, added);
		}		
		
		if(bAdjoin) {
			add("29:" + actionName, dfv, added);
			add("30:" + top0Tag+"_"+top1Tag +"_"+actionName, dfv, added);
		}
		add("31:" + sPunct + "_"+ actionName, dfv, added);
		add("32:" + top0Tag+"_"+top1Tag +"_"+sPunct + "_"+ actionName, dfv, added);
		
		return dfv;
	}
	

	
}
