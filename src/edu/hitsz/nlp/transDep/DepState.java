package edu.hitsz.nlp.transDep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.hitsz.algorithm.hash.HashCodeUtil;
import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;


public class DepState implements Comparable<DepState> {		

	private TransPipe pipe;
	private boolean isDp = true;
	//输入序列
	public InputSequence sequence;
	
	int curIdx;
	int bgn;
	int end;
	//当前权值
	public double scprf;	
	public double scins;
	public double scdlt;
	//栈
	public DepItem[] stack;
	
	public List<DepFeatureVector> fvins;
	public DepFeatureVector fvdlt;
	
	public Set<DepState> preds;
	public DepState pred0;
	public Map<DepState, Pair<DepFeatureVector, Double>> trans;	
	
	public int[] heads;
	public String[] pos;
	
	//所有动作
	public List<TransAction> actions;
	

	//AtomicFeature for Dynamic Programming
	public DynamicFeatures features;
	
	public boolean isGold; //是否是gold语句或动作	
	
	
	public DepState(TransPipe pipe, InputSequence sequence) {
		this.pipe = pipe;
		this.isDp = pipe.isDp;
		
		this.sequence = sequence;		
		curIdx = 0;
		bgn = -1;
		end = -1;
		scprf = 0.0d;
		scins = 0.0d;
		scdlt = 0.0d;
		stack = new DepItem[3];
		if(pipe instanceof TransPipeArcStandardHuang2010)
			stack = new DepItem[3];
		fvins = new LinkedList<DepFeatureVector>();
		fvdlt = new DepFeatureVector();
		preds = new TreeSet<DepState>();
		pred0 = null;
		trans = new LinkedHashMap<DepState, Pair<DepFeatureVector, Double>>();
		heads = new int[sequence.heads.length];
		pos= new String[sequence.postags.length];
		actions = new ArrayList<TransAction>();	

		stack[0] = new DepItem(-1, "ROOT", "ROOT", -2);
		for(int i=0; i<heads.length; i++)
			heads[i] = sequence.heads[i];
		resetDynamicFeatures(pipe);
		this.isGold = true;
	}		
	
	public DepState(TransPipe pipe, InputSequence sequence,
			DepItem[] stack, 
			int curIdx, int bgn, int end,
			double scprf, double scins, double scdlt,
			List<DepFeatureVector> fvins,
			DepFeatureVector fvdlt,
			Set<DepState> preds, DepState pred0, 
			Map<DepState, Pair<DepFeatureVector, Double>> trans,
			int[] heads,
			String[] pos,
			List<TransAction> actions,
			boolean isGold) {
		
		this.pipe = pipe;
		this.isDp = pipe.isDp;
		this.sequence = sequence;
		this.curIdx = curIdx;
		this.bgn = bgn;
		this.end = end;
		this.scprf = scprf;
		this.scins = scins;
		this.scdlt = scdlt;
		this.stack = stack;
		this.actions = actions;
		this.preds = preds;
		this.pred0 = pred0;
		this.trans = trans;
		this.heads = heads;
		this.pos = pos;
		this.fvins = fvins;
		this.fvdlt = fvdlt;
		this.isGold = isGold;
		resetDynamicFeatures(pipe);
	}
	

	/** 
	 * 
	 * @since Mar 7, 2013
	 */
	public DepState shift(boolean isGoldAction, boolean added) {
		
		//不能到达句尾，并且栈中有元素（栈顶index==-1(为ROOT),或者栈顶index的pos不为空）
		assert( !(curIdx == sequence.wordSize || stack[0].index != -1 && pos[stack[0].index] == null));
				
		//score			
		DepFeatureVector vs = pipe.getFeatureVector(this, TransAction.shift(), added);
		double scdlt = vs.getScore(pipe.params);
		if (!added) vs = null;
		List<DepFeatureVector> _fvins = added ? new LinkedList<DepFeatureVector>() : null;
		double _scprf = scprf + scdlt;
		double _scins = 0.0;
		//
		DepItem[] _stack = 	pushStack(new DepItem(curIdx, sequence.words[curIdx], sequence.postags[curIdx]));		
		Set<DepState> _preds = new HashSet<DepState>();
		_preds.add(this);
		Map<DepState, Pair<DepFeatureVector, Double>> _trans = new LinkedHashMap<DepState, Pair<DepFeatureVector, Double>>();
		_trans.put(this, new Pair<DepFeatureVector, Double>(vs, scdlt));
				
		//
		int[] _heads = new int[this.sequence.wordSize];
		Arrays.fill(_heads, -2);
		//添加下一个pos
		String[] _pos = new String[sequence.wordSize];
		if(_stack[0].postag != null)
			_pos[curIdx] = _stack[0].postag;
		
		List<TransAction> _actions = new LinkedList<TransAction>();
		_actions.add(TransAction.shift());
		
		return new DepState(pipe, sequence, _stack, curIdx+1, end+1, end+1, 
				_scprf, _scins, scdlt, _fvins, vs,  _preds, this, _trans, _heads, _pos,
				 _actions,isGoldAction && isGold);	
	}
	
	
	public DepItem[] pushStack(DepItem t)
	{
		DepItem[] _stack = new DepItem[stack.length];
		for (int i = 0; i < stack.length - 1; ++i) 
			_stack[i + 1] = stack[i] != null ? new DepItem(stack[i]) : null;
		_stack[0] = t;
		return _stack;
	}

		
	/** top of the stack is the father 
	 * 用于Arc-Standard, Huang 2010
	 * @since Mar 7, 2013
	 */	
	public List<DepState> reduceRight(String deprel, boolean isGoldAction, boolean added) {

		List<DepState> l = new ArrayList<DepState>();
		if(stack[0].index != -1 && pos[stack[0].index] == null)
			return l;				
		
		DepFeatureVector vr = pipe.getFeatureVector(this, TransAction.reduceRight(deprel), added);
		double sr = vr.getScore(pipe.params);	
		if(!added) vr = null;
		
		for(DepState prevState : preds) {
			//
			if(prevState.stack[0].index == -1 && curIdx < sequence.wordSize) continue;
			
			assert(trans.containsKey(prevState));
			Pair<DepFeatureVector, Double> pair = trans.get(prevState);
			double scdlt = pair.second + sr;
			
			List<DepFeatureVector> _fvins = null;
			if (added)
			{
				_fvins = new LinkedList<DepFeatureVector>(fvins);
				_fvins.addAll(prevState.fvins);
				_fvins.add(pair.first);
				_fvins.add(vr);
			}
			
			double _scprf = prevState.scprf + scins + scdlt;
			double _scins = prevState.scins + scins + scdlt;
			
			DepItem[] _stack = prevState.cloneStack();
			DepItem h = new DepItem(prevState.stack[0]);
			DepItem c = new DepItem(stack[0]);
			_stack[0] = h;
			
			if(h.index > c.index)
				h.leftSons.add(c);
			else
				h.rightSons.add(c);
			
			c.head = h.index;
			
			int[] _heads = Arrays.copyOf(heads, heads.length);
			for (int i = 0; i < prevState.heads.length; ++i)
				if (prevState.heads[i] != -2)
					_heads[i] = prevState.heads[i];
			_heads[c.index] = h.index;

			String[] _pos = Arrays.copyOf(pos, pos.length);
			for(int i=0; i < prevState.pos.length; i++)
				if(prevState.pos[i] != null)
					_pos[i] = prevState.pos[i];
			
			List<TransAction> _actions = new LinkedList<TransAction>(prevState.actions);
			_actions.addAll(actions);
			_actions.add(TransAction.reduceRight(deprel));
			
			l.add(new DepState(pipe, sequence, _stack, curIdx, Math.max(prevState.bgn, 0), end, 
					_scprf, _scins, scdlt,  _fvins, vr, prevState.preds, prevState.pred0, 
					prevState.trans, _heads, _pos, _actions, 
					isGold && prevState.isGold && isGoldAction));
			
		}
		return l;
		
	}	
	
	
	public DepItem[] cloneStack()
	{
		DepItem[] _pstck = new DepItem[stack.length];
		for (int i = 0; i < stack.length; ++i)
			_pstck[i] = stack[i] != null ? new DepItem(stack[i]) : null;
		return _pstck;
	}
		
	
	/** second of the stack is the father 
	 * 用于Arc-Standard, Huang 2010
	 * @since Mar 7, 2013
	 */
	public List<DepState> reduceLeft(String deprel, boolean isGoldAction, boolean added) {
		
		List<DepState> l = new ArrayList<DepState>();
		if(stack[0].index != -1 && pos[stack[0].index] == null)
			return l;
					
		DepFeatureVector vr = pipe.getFeatureVector(this, TransAction.reduceLeft(deprel), added);
		double sr = vr.getScore(pipe.params);	
		if(!added) vr = null;
		
		for(DepState prevState : preds) {
			//如果前一个状态是ROOT，表明子节点为ROOT，所以不成立
			if(prevState.stack[0].index == -1) continue;
			
			Pair<DepFeatureVector, Double> pair = trans.get(prevState);
			double scdlt = pair.second + sr;
			
			List<DepFeatureVector> _fvins = null;
			if (added)
			{
				_fvins = new LinkedList<DepFeatureVector>(fvins);
				_fvins.addAll(prevState.fvins);
				_fvins.add(pair.first);
				_fvins.add(vr);
			}
			
			double _scprf = prevState.scprf + scins + scdlt;
			double _scins = prevState.scins + scins + scdlt;
			
			DepItem[] _stack = prevState.cloneStack();
			DepItem h = new DepItem(stack[0]);
			DepItem c = new DepItem(prevState.stack[0]);
			_stack[0] = h;
			
			if(h.index > c.index)
				h.leftSons.add(c);
			else
				h.rightSons.add(c);
			
			c.head = h.index;
			
			int[] _heads = Arrays.copyOf(heads, heads.length);
			for (int i = 0; i < prevState.heads.length; ++i)
				if (prevState.heads[i] != -2)
					_heads[i] = prevState.heads[i];
			_heads[c.index] = h.index;
			
			String[] _pos = Arrays.copyOf(pos, pos.length);
			for(int i=0; i < prevState.pos.length; i++)
				if(prevState.pos[i] != null)
					_pos[i] = prevState.pos[i];
			
			List<TransAction> _actions = new LinkedList<TransAction>(prevState.actions);
			_actions.addAll(actions);
			_actions.add(TransAction.reduceLeft(deprel));
			
			l.add(new DepState(pipe, sequence, _stack, curIdx, Math.max(prevState.bgn, 0), end,
					_scprf, _scins, scdlt, _fvins, vr, prevState.preds, prevState.pred0, 
					 prevState.trans, _heads, _pos, _actions,
					isGold && prevState.isGold && isGoldAction));
			
		}
		return l;
		
	}
	

	
	
		
	/**************************************************************************/
	
	

	
	
	
	
	
	/**
	 * 判断从sonSeq开始右边items中是否有父亲为father
	 * <li> sonSeq到达句子尾，sonSeq >= heads.length
	 * <li> heads[>=sonSeq] == father
	 * @since Feb 25, 2013
	 * @param fatherIndex
	 * @param heads
	 * @param sonIndex
	 * @return
	 */
	public boolean existSon(int fatherIndex, int[] heads, int sonIndex) {
		for(int i=sonIndex; i< heads.length; i++) {
			if(heads[i] == fatherIndex)
				return true;			
		}
		return false;
	}
	
	/**
	 * 判断在从sonSeq开始右边isWords(表示是否为词首字），首字对应的词是否其父亲为father
	 * @since Mar 10, 2013
	 * @param father
	 * @param heads
	 * @param isWords
	 * @param sonSeq
	 * @return
	 */
	public boolean existSon(int father, int[] heads, int[] isWords, int sonSeq) {
		for(int i=sonSeq; i<isWords.length; i++) {
			//如果是词，并且该词的父亲是father
			if(isWords[i] >= 0 && heads[isWords[i]] == father)
				return true;			
		}
		return false;
	}
	
	/** 返回最后一个动作 */
	public TransAction getLastAction() {
		if(actions == null || actions.size() == 0)
			return null;
		else
			return actions.get(actions.size()-1);
	}
	
	
	/** 设置所有的DepItem，用于@DepConversion*/
	public void setAllItems(DependencyInstanceJoint instance) {		
		String[] words = instance.forms;
		String[] tags = instance.postags;
		int[] heads = instance.heads;		
		stack = new DepItem[words.length-1];
		
		for(int i=1; i<instance.length(); i++) {
			DepItem item = new DepItem(i, words[i], tags[i], heads[i]);
			stack[i-1] = item;
		}		
	}

	
	/** index减去1，从0开始*/
	public void reduceIndex() {
		for(DepItem item : stack)
			item.index -= 1;
	}
	
	public void addIndex() {
		for(DepItem item : stack)
			item.index += 1;
	}
	
	public void reduceHead() {
		for(DepItem item : stack)
			item.head -= 1;
	}
	
	public void addHead() {
		for(DepItem item : stack)
			item.head += 1;
	}
	
	
	/** 每次动作后，都需要特征更新，包括初始化，动作，clone
	 */
	private void resetDynamicFeatures(TransPipe pipe) {
		features = pipe.resetDynamicFeatures(this);
	}	
	
	public List<TransAction> getActionSequence()
	{
		return getActionSequence(this);		
	}

	public List<TransAction> getActionSequence(DepState s)
	{
		List<TransAction> l = new LinkedList<TransAction>();
		for (; s != null; s = s.pred0)
			l.addAll(0, s.actions);
		return l;
	}
	
	
	public int[] getParsedResult(int size) {
		
		int[] heads = new int[size];
		List<DepItem> lc = new ArrayList<DepItem>();
		List<DepItem> _lc = new ArrayList<DepItem>();
		
		DepState s = this;
		
		for(DepState _s = s; _s != null; _s = _s.preds.size() > 0 ? _s.pred0 : null) {
			lc.add(_s.stack[0]);
			while(lc.size() > 0) {
				for(DepItem item : lc) {
					_lc.addAll(item.leftSons);
					_lc.addAll(item.rightSons);
					
					int idx = item.index;
					if(idx == -1)
						continue;
					
					heads[idx] = _s.heads[idx]+1;
				}
				List<DepItem> __lc = lc;
				lc = _lc;
				_lc = __lc;
				
				_lc.clear();				
			}
			lc.clear();		
		}		
		return heads;
	}
	
	

	@Override
	public DepState clone() {		
		return new DepState(
				pipe,
				sequence,
				cloneStack(),
				curIdx,
				bgn,
				end,
				scprf,
				scins,
				scdlt,
				fvins != null ? new LinkedList<DepFeatureVector>(fvins) : null,
				fvdlt != null ? new DepFeatureVector(fvdlt) : null,
				new TreeSet<DepState>(preds),
				pred0,
				new LinkedHashMap<DepState, Pair<DepFeatureVector, Double>>(trans),
				Arrays.copyOf(heads, heads.length),
				Arrays.copyOf(pos, pos.length),
				new LinkedList<TransAction>(actions),
				isGold);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof DepState)) return false;
		DepState objState = (DepState) obj;	
		if(isDp) {
			return features.equals(objState.features);
		}
		else {	
			return this == obj;
		}
	}
	
	
	@Override
	public String toString() {
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("{");
		for(int i= stack.length-1; i >= 0; i--)
			if(stack[i] != null)
				strbuf.append(stack[i].toString() + " ");
		strbuf.append(": ");
		for(int i=curIdx; i<sequence.wordSize; i++)
			strbuf.append(sequence.words[i]+":"+sequence.postags[i]);
		strbuf.append("}");
		return stack.toString();		
	}
	
	@Override
	public int hashCode() {		
		if (isDp) {
			return features.hashCode();
		}
		else {
			return super.hashCode();
		}
	}
	
	@Override
	public int compareTo(DepState o)
	{
		int h1 = hashCode();
		int h2 = o.hashCode();
		return h1 > h2 ? 1 : h1 == h2 ? 0 : -1;
	}
    
}
