package edu.hitsz.ml.maxent.lindekang;

import java.util.ArrayList;

public class GISTrainer extends MaxEntTrainer{

	/**
	 * GIS训练模型,更新策略
	 * <p> 
	 * @param MaxEntModel
	 * @param EventSet 
	 */
	public void train(MaxEntModel model, EventSet events){
		
		/** 
		 * 统计实例中特征的出现次数,是个常数
		 * <p> \sum_{x,y} p_{stat}(x,y) f(x,y)
		 */
		ArrayList<Double> obsCounts = new ArrayList<Double>(); 
		/**
		 * ME模型给出特征的统计信息
		 * <p> \sum_{x,y} p_{stat}(x) p(y|x) f(x,y)
		 */
		ArrayList<Double> expects = new ArrayList<Double>();   //
		
		double C = model.getObsCounts(events, obsCounts);
		double sumLogProb = 0, prevSumLogProb = 0;
		
		for(int i=0; i<_maxIterations; i++){
			sumLogProb = model.getExpects(events, expects);
			if(_printDetails)
				System.out.println("Iteration "+(i+1)+" logProb="+sumLogProb);
			
			if(i>0 && sumLogProb-prevSumLogProb<=_threshold)
				break;
			prevSumLogProb = sumLogProb;
			
			ArrayList<Double> lambda = model.lambda();
			
			for(int j=0; j<lambda.size(); j++){
				// w_i = w_i + 1/M * log(E'(p)/E(p))
				double obs = obsCounts.get(j) - _alpha;
				double newLambda = 0;
				if(obs > 0)
					newLambda = lambda.get(j)+Math.log(obs/expects.get(j))/C;
				if(newLambda > 0)
					lambda.set(j, newLambda);
				else
					lambda.set(j, 0.0);
			}
		}
		
	}
	
	
		
	
	
	
	
	

}
