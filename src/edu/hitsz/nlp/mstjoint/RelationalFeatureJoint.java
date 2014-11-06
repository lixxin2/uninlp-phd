package edu.hitsz.nlp.mstjoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;



public class RelationalFeatureJoint implements Serializable {

    public String name;
    public String[][] values;

    /**
     * 初始化特征
     * @param size 大小
     * @param declaration 名字
     * @param br 从输入流读取特征值
     * @throws IOException
     */
    public RelationalFeatureJoint(int size, String declaration, BufferedReader br) throws IOException {
		values = new String[size][size];
		String[] declist = declaration.split(" ");
		name = declist[2];
		for (int i=0; i<size; i++) {
		    values[i] = br.readLine().substring(2).split(" ");
		}
    }

    /**
     * 得到特征
     * @param firstIndex
     * @param secondIndex
     * @return
     */
    public String getFeature(int firstIndex, int secondIndex) {
		if (firstIndex == 0 || secondIndex == 0)
		    return name+"=NULL";
		else
		    //System.out.println(values.length + "** " + name+"="+values[firstIndex-1][secondIndex-1]);
		    return name+"="+values[firstIndex-1][secondIndex-1];
    }

    private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeObject(name);
		out.writeObject(values);
    }


    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		name = (String)in.readObject();
		values = (String[][])in.readObject();
    }


}