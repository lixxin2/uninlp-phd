package edu.hitsz.ml.maxent.lindekang;

import java.util.ArrayList;

/**
 * A Event consists of a set of binary features (corresponding to the
 * integers in the vector).
 * 
 * <p> 一个最大熵实例，包括实例的类型和实例出现的次数（通常为1）
 * <p> 由于它继承了ArrayList<Integer>,其中包含了实例的特征。
 *
 * @author tm
 *
 */
public class MaxEntEvent extends ArrayList<Integer>{

	double _count; // the number of instances of this event (typicall 1).
	int _classId;  // the class that this event belongs to.

	MaxEntEvent() {
		_count = 0;
		_classId = 0;
	}

	public double getCount(){
		return _count;
	}

	public void setCount(double c){
		_count = c;
	}

	public int getClassId(){
		return _classId;
	}

	/**
	 * 设置该事件的类别
	 * @param id
	 */
	public void setClassId(int id){
		_classId = id;
	}

}
