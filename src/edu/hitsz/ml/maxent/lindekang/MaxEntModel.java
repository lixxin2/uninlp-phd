package edu.hitsz.ml.maxent.lindekang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * The parameters in a maximum entropy classifier.
 * <p> 最大熵模型，包含特征对应的参数
 * @author tm
 *
 */
public class MaxEntModel {
	
	/** the number of possible output classes */
	int _classes;
	
	/** mapping features to indices in the _lambda vector， 
	 *  <p> 第i个特征在_lambda中的位置，等于i*c（c表示类别数目） 
	 */
	HashMap<Integer, Integer> _index;
	
	/** _lambda[_index[f]+c] is the lambda value for feature f and class c, 
	 * <p> 特征权重，表示每个特征位于每个类别的位置
	 * <p> 比如有3个类别C，4个特征，则f1*C1,f1*C2,f1*C3,f2*C1,f2*C2,...,f4*C3
	 */
	ArrayList<Double> _lambda; 	//

	MaxEntModel() {
		_classes = 0;
		_index = new HashMap<Integer, Integer>();
		_lambda = new ArrayList<Double>();
	}

	MaxEntModel(int classes) {
		_classes = classes;
		_index = new HashMap<Integer, Integer>();
		_lambda = new ArrayList<Double>();
	}

	ArrayList<Double> lambda(){
		return _lambda;
	}

	/**
	 * Compute the probability of all classes given the event. Return the class with the highest probability.
	 * <p> 给定实例，计算每个类别的概率p_{me}(y|x)，返回概率最大的类别
	 * <p> 概率p_{me}(y|x)是通过计算权重_lambda与特征乘积的和，p_{me}(y|x)=exp(w*x)/sum(exp(w*x))
	 * @param event
	 * @param probs 返回每个类别的概率p_{me}(y|x)=exp(prob)/sum(exp(prob))
	 * @return
	 */
	int getProbs(MaxEntEvent event, ArrayList<Double> probs){
		probs.clear();
		for(int i=0; i<_classes; i++)         //每个类别的概率
			probs.add(0.0);
		int max = -1;
		//对于每个类别
		for(int c=0; c<_classes; c++){        //对于每个类别y
			double s = 0;
			//对于每个特征，叠加
			for(int f=0; f<event.size(); f++){                //对于每个特征f(x,y)
				if(_index.containsKey(event.get(f))){         //如果包含该特征
					int itSecond = _index.get(event.get(f));  //该特征的起始位置
					s += _lambda.get(itSecond + c);           //该特征的权重w(x,y)
				}
			}
			// s = \sum w(x,y)*f(x,y),为计算p(y|x)做准备
			probs.set(c, s);                    //设置属于该类别的概率
			if( max<0 || probs.get(max) < s)
				max = c;                        //最大概率的类别
		}
		
		double sum = LogProbs.sumLogProb(probs);
		
		for(int i=0; i<_classes; i++)
			probs.set(i, Math.exp(probs.get(i))/Math.exp(sum));
		//for(int i=0; i<probs.size(); i++)
		//	System.out.print("probs："+probs.get(i));
		//System.out.println();
		return max;
	}


	/**
	 * Compute the observed counts of all features. Return the maximum number of features in any event.
	 * <p> 统计所有特征在所有类别中出现的次数obsCounts，返回所有实例中包含特征的最大数目（每个实例的特征可能不相同）
	 * <p> P_{stat}(f(x.y)) = \sum_{x,y} p_{stat}(x,y)f(x,y)
	 * @param events
	 * @param obsCounts 每个特征位于每个类别的个数: [f*c]
	 * @return
	 */
	double getObsCounts(EventSet events, ArrayList<Double> obsCounts){
				
		obsCounts.clear();
		for(int i=0; i<_lambda.size(); i++)
			obsCounts.add(0.0);
		double maxFtSum = 0;
		for( int i=0; i<events.size(); i++){   //对于每个实例x
			MaxEntEvent e = events.get(i); 
			int c = e.getClassId();            //它的类别y
			double count = e.getCount();       //它的出现次数p(x,y)
			double ftSum = 0;
			for(int j=0; j<e.size(); j++){                 //每个特征f(x,y)
				if(_index.containsKey(e.get(j))){          //如果包含该特征
					int seq = _index.get(e.get(j));        //该特征的起始位置f*c
					double tmp = obsCounts.get(seq+c);     //该特征的目前次数f*c+c
					obsCounts.set(seq+c, tmp + count);     //更新该特征的目前次数
				}
				else{// new feature, need to expand obsCounts and _lambda； 新特征，扩展
					for(int k=0; k<_classes; k++)
						obsCounts.add(0.0);
					//增加实例对应类别的数目
					obsCounts.set(_lambda.size()+c, obsCounts.get(_lambda.size()+c)+count); 
					addFeature(e.get(j));
				}
				ftSum++;
			}
			if (ftSum>maxFtSum)
				maxFtSum = ftSum;
		}
		//for(int i=0; i<_index.size(); i++)
		//	System.out.print(_index.get(i)+"\t");
		//System.out.println();
		//for(int i=0; i<_lambda.size(); i++)
		//	System.out.print(_lambda.get(i)+"\t");
		//System.out.println();
		//for(int i=0; i<obsCounts.size(); i++)
		//	System.out.print(obsCounts.get(i)+"\t");
		//System.out.println();
		return maxFtSum;
		
	}

	/**
	 * Add a feature to the model， 向模型增加一个新的特征，特征对应的值为其在_lambda的初始位置
	 * @param f
	 */
	public void addFeature(int f){
		_index.put(f, _lambda.size());
		for(int i=0; i<_classes; i++)
			_lambda.add(0.0);
	}

	/**
	 * Compute the expected value of all features. Return the log likelihood of the events
	 * <p> 计算所有特征的期望值(predicted count(f,_lambda)), 
	 * p_{me}(f(x,y)) = \sum_{x,y} p_{stat}(x) p_{me}(y|x) f(x,y)
	 * <p> 返回事件的log likelihood， log(\prod p(y|x)) = \sum (log p(y|x))
	 * @param events
	 * @param expects 每个特征对于每个类别的概率期望值 f1*c1, f1*c2, f2*c1, f2*c2, f3...
	 */
	public double getExpects(EventSet events, ArrayList<Double> expects){
		
		expects.clear();
		for(int i=0; i<_lambda.size(); i++) //对于每个类别和特征
			expects.add(0.0);
		
		double sumLogProb = 0;
		for(int i=0; i<events.size(); i++){
			
			MaxEntEvent e = events.get(i);               //对于每个实例
			ArrayList<Double> probs = new ArrayList<Double>();
			getProbs(e, probs);                          //获取实例其属于每个类别的概率p(y|x)
			
			//统计\sum_c P(c|d)f(c,d)
			for(int c=0; c<_classes; ++c){
				double count = probs.get(c)*e.getCount(); //新概率
				for(int j=0; j<e.size(); j++){            //
					if(_index.containsKey(e.get(j))){
						int tmp = _index.get(e.get(j));
						expects.set(tmp+c, expects.get(tmp+c)+count); 
					}
				}
			}			
			sumLogProb += Math.log(probs.get(e.getClassId()));			
		}

		return sumLogProb;		
	}
	

	public void setClasses(int classes) {
		_classes = classes;
	}

	/**
	 * print the parameters in the model
	 * @param trainer
	 */
	public void print(MaxEntTrainer trainer){
		Iterator iter = _index.entrySet().iterator();
		while(iter.hasNext()){
			Entry entry = (Entry)iter.next();
			int i = (Integer) entry.getValue();
			for( int c=0; c<_classes; c++){
				System.out.println("lambda("+trainer.getClassName(c)+","+trainer.getStr((Integer)entry.getKey())+")="
						+_lambda.get(i+c));
			}
		}
	}
	
	public void store(String modelFileName) {
		
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(modelFileName)));
			oos.writeObject(this._classes);	//括号内参数为要保存java对象
			oos.writeObject(this._index);
			oos.writeObject(this._lambda);
			oos.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void read(String modelFileName){
		
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(new File(modelFileName)));
			this._classes = (Integer) ois.readObject();//强制类型转换
			this._index = (HashMap<Integer, Integer>) ois.readObject();
			this._lambda = (ArrayList<Double>) ois.readObject();
			ois.close();			
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 		
		
	}



}
