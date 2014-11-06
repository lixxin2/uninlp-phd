package edu.hitsz.nlp.transDep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DepChart {

	boolean isDp = true;
	Map<DepState, Pair<DepState, double[]>> maps;
	
	public DepChart(boolean isDp) {
		this.isDp = isDp;
		maps = new LinkedHashMap<DepState, Pair<DepState, double[]>>();
	}
	
	
	/** 更新 新状态 */
	public synchronized DepState update(DepState state) {
		
		assert(state != null);
		//如果 当前state 已经存在 则合并
		if(isDp && maps.containsKey(state)) {
			DepState _s = maps.get(state).first;
			DepState mergedState = merge(_s, state);
			maps.remove(_s);
			maps.put(mergedState, new Pair<DepState, double[]>(mergedState, new double[] { mergedState.scprf, mergedState.scins }));
			return mergedState;
		}
		//不存杂	
		else {
			maps.put(state, new Pair<DepState, double[]>(state, new double[] {state.scprf, state.scins}));
			return state;
		}		
	}
	
	
	
	/** 动态规划，合并相同状态 */
	public DepState merge(DepState s1, DepState s2)
	{
		assert (s1.features.equals(s2.features));

		if( !(s1.scprf > s2.scprf || s1.scprf == s2.scprf && s1.scins > s2.scins))
		{
			DepState _s = s1;
			s1 = s2;
			s2 = _s;
		}

		DepState ps = s1.clone();

		for (DepState _ps : s2.preds)
			if (!ps.preds.contains(_ps))
				ps.preds.add(_ps);

		for (Entry<DepState, Pair<DepFeatureVector, Double>> p : s2.trans.entrySet())
		{
			DepState sk = p.getKey();
			if (!ps.trans.containsKey(sk))
				ps.trans.put(sk, p.getValue());
		}
		ps.isGold = s1.isGold || s2.isGold && Arrays.equals(s1.heads, s2.heads);
		
		return ps;
	}
	
	/** 根据概率排序，并选取kBeam个最优值 */
	public void prune(int kBeam) {
		//将Map中的数据存入list中
		List<Entry<DepState, Pair<DepState, double[]>>> l = new ArrayList<Entry<DepState, Pair<DepState, double[]>>>();
		for(Entry<DepState, Pair<DepState, double[]>> entry : maps.entrySet()) {
			l.add(entry);
		}
		//排序
		Collections.sort(l, new Comparator<Entry<DepState, Pair<DepState, double[]>>>()
		{
			public int compare(Entry<DepState, Pair<DepState, double[]>> p1, Entry<DepState, Pair<DepState, double[]>> p2)
			{
				double[] d1 = p1.getValue().second;
				double[] d2 = p2.getValue().second;
				return d1[0] < d2[0] ? 1 : (d1[0] == d2[0] ? (d1[1] < d2[1] ? 1 : d1[1] == d2[1] ? 0 : -1) : -1);
			}
		});
		maps.clear();
		for(int i=0; i< l.size(); i++) {
			if(i >= kBeam) break;
			maps.put(l.get(i).getKey(), l.get(i).getValue());
		}
	}
	
	/** 是否需要EarlyStop停止 */
	public boolean isEarlyStop() {
		boolean isFound = false;
		for(DepState state : maps.keySet()) {
			if(state.isGold) {
				isFound = true;
				break;
			}
		}
		return !isFound;
	}
	
	
	/** 获得最有的DepState */
	public DepState getBestState() {		
		DepState sBest = null;
		double[] dBest = new double[] {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
		for(Pair<DepState, double[]> pair : maps.values()) {
			if(pair.second[0] > dBest[0] || pair.second[0] == dBest[0] && pair.second[1] > dBest[1]) {
				sBest = pair.first;
				dBest = pair.second;
			}
		}
		return sBest;		
	}
	
}
