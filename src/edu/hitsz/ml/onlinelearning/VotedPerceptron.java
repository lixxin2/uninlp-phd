/**
 * 
 */
package edu.hitsz.ml.onlinelearning;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;



/**
 * Voted Perceptron (Freund, 1999) Large margin classification using the perceptron algorithm <br/>
 * which combines perceptron, batch learning, kernel function.
 * 
 * @author tm
 *
 */
public class VotedPerceptron {	
	/**
	 * 
	 */

	/** The maximum number of alterations to the perceptron, 允许感知器的最大个数 */
	private int m_MaxK = 60000;
	
	/** The number of iterations，迭代次数 */
	private double m_NumIterations = 1;
	
	/** The actual number of alterations，实际的感知器个数 */
	private int m_K = 0;
	
	/** The exponent for polynomial kernel */
	private double m_PolynomialExponent = 4.0;
	
	/** Seed used for shuffling the dataset */
	private int m_Seed = 1;
	
	/** The training instances added to the perceptron (ith)， 第i个感知器对应的样本位置 */
	private int[] m_Additions = null;
	
	/** Addition or subtraction? 第i个感知器的结果，是正是负*/
	private boolean[] m_IsAddition = null;
	
	/** The weights for each perceptron, the number of correct samples before making errors，第i个感知器的权重，即第i个错误前识别正确的样本个数 */
	private int[] m_Weights = null;	
	
	/** The training instances */
	private double[][] instanceX = null;
	private int[] instanceY = null;


	
	public void addInstanceX(double[][] x){
		instanceX = x;
	}	
	
	/**
	 * train model, with input x and y
	 * @param x
	 * @param y
	 */
	public void train(double[][] x, int[] y){			
		int xLen = x.length;
		if(xLen<0 || xLen!=y.length || x[0].length<0){
			System.out.println("\n training data is wrong");
			System.exit(-1);
		}		
		instanceX = x;
		instanceY = y;
		
	    /** Make space to store perceptrons */
	    m_Additions = new int[m_MaxK + 1];
	    m_IsAddition = new boolean[m_MaxK + 1];
	    m_Weights = new int[m_MaxK + 1];

	    /** Compute perceptrons */
	    m_K = 0;
	    out:
	    	for (int it = 0; it < m_NumIterations; it++) {
	    		System.out.print("the "+it+"th iteration:");
	    		if(m_NumIterations<1)
	    			xLen *= m_NumIterations;
	    		for (int i = 0; i < xLen; i++) {
	    			if(i%10000 == 0)
	    				System.out.println();
	    			else if(i%1000 == 0)
	    				System.out.print(i+"...");
	    			int prediction = predictInner(m_K, x[i]);
	    			if (prediction == y[i]) {
	    				m_Weights[m_K]++;
	    			} 
	    			else {
		    		    m_IsAddition[m_K] = (y[i] == 1);
		    		    m_Additions[m_K] = i;
		    		    m_K++;
		    		    m_Weights[m_K]++;
	    			}
	    			if (m_K == m_MaxK) {
	    				break out;
	    			}
	    		}
	    	}
	    System.out.println("There are "+m_K+" perceptrons in the classifier");

	}

	/**
	 * predict the classes of inputing samples
	 * @param x
	 * @return
	 */
	public int[] predict(double[][] x){
		int xLen = x.length;
		int[] results = new int[xLen];
		for(int i=0; i<xLen; i++)
			if(predictProb(x[i])>0.5)
				results[i] = 1;
			else
				results[i] = -1;
		return results;
	}
	
	/**
	 * predict the probabilities of inputing samples as positive classes
	 * @param x
	 * @return
	 */
	public double[] predictProb(double[][] x){
		int xLen = x.length;
		double[] results = new double[xLen];
		for(int i=0; i<xLen; i++)
			results[i] = predict(x[i]);
		return results;		
	}
	
	/**
	 * predict the class of an inputing sample
	 * @param x
	 * @return
	 */
	public int predict(double[] x){
		if(predictProb(x)>0.5)
			return 1;
		else
			return -1;
	}
	
	/**
	 * predict the probability of an inputing sample
	 * @param k
	 * @param x train feature
	 * @return
	 */
	public double predictProb(double[] x){  	    
	    // Get probabilities
	    double output = 0, sumSoFar = 0;
	    if(x.length != instanceX[0].length){
	    	System.out.println("The length of input sample is different from the instanceX");
	    	System.exit(-1);
	    }
	    if (m_K > 0) {
	    	for (int i = 0; i <= m_K; i++) {
				if (sumSoFar < 0) {
					output -= m_Weights[i];
				} 
				else {
					output += m_Weights[i];
				}
				if (m_IsAddition[i]) {
					sumSoFar += polynomialInnerProduct(instanceX[m_Additions[i]], x);
				} 
				else {
					sumSoFar -= polynomialInnerProduct(instanceX[m_Additions[i]], x);
				}
	    	}
	    }	 
	    return 1/(1+Math.exp(-output));	     
	}
	
	
	/**
	 * save the training model
	 * @param modelFileName
	 */
	public void saveModel(String modelFileName){
	    // Save the classifier if an object output file is provided
		try{
			OutputStream os = new FileOutputStream(modelFileName);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
			System.out.print("saving model "+modelFileName+"...");
			objectOutputStream.writeObject(m_MaxK);	
			objectOutputStream.writeObject(m_NumIterations);
			objectOutputStream.writeObject(m_K);
			objectOutputStream.writeObject(m_PolynomialExponent);
			objectOutputStream.writeObject(m_Seed);
			objectOutputStream.writeObject(m_Additions);
			objectOutputStream.writeObject(m_IsAddition);
			objectOutputStream.writeObject(m_Weights);
			//objectOutputStream.writeObject(instanceX);	
			objectOutputStream.flush();
			objectOutputStream.close();
			System.out.println("done");
		}
		catch(IOException e){
			System.out.println("IOException" + e);
		}
	}
	    	
	/**
	 * load model from file
	 * @param modelFileName
	 * @return
	 */
	public void loadModel(String modelFileName){	
		try{
			FileInputStream is = new FileInputStream(modelFileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(is);			
			// Load classifier from file
			if (objectInputStream != null) {
				System.out.println("loading model "+modelFileName+"...");
				m_MaxK = (Integer) objectInputStream.readObject();	
				m_NumIterations = (Double) objectInputStream.readObject();
				m_K = (Integer) objectInputStream.readObject();
				m_PolynomialExponent = (Double) objectInputStream.readObject();
				m_Seed = (Integer) objectInputStream.readObject();
				m_Additions = (int[]) objectInputStream.readObject();
				m_IsAddition = (boolean[]) objectInputStream.readObject();
				m_Weights = (int[]) objectInputStream.readObject();
				//instanceX = (double[][]) objectInputStream.readObject();	
				objectInputStream.close();
				System.out.println("done");
			}
		}      
		catch (ClassNotFoundException e){
    		System.out.println("IOException" + e);
    	}
    	catch(IOException e){
	    	System.out.println("IOException" + e);
	    }	
	}
		
		
	
	
	/**
	 * Compute a prediction from a perceptron
	 * @param k
	 * @param x train feature
	 * @return
	 */
	private int predictInner(int k, double[] x){
		double result = 0;
		for (int i = 0; i < k; i++) {
			if (m_IsAddition[i]) {
				result += polynomialInnerProduct(instanceX[m_Additions[i]], x);
			} 
			else {
				result -= polynomialInnerProduct(instanceX[m_Additions[i]], x);
			}
		}
	    if (result < 0) {
	    	return -1;
	    } 
	    else {
	    	return 1;
	    }
	}
	
		
	 /** 
	   * Computes the polynomail inner product of two instances
	   * 
	   * @param ix first instance
	   * @param x second instance
	   * @return the inner product
	   */
	 private double polynomialInnerProduct(double[] ix, double[] x) {
		 // we can do a fast dot product
		 double result = 1.0;
		 int n1 = ix.length;
		 for (int i=0; i < n1; i++) {
			result += ix[i] * x[i];
		 }	    	    
		 if (m_PolynomialExponent != 1) {
			 return Math.pow(result, m_PolynomialExponent);
		 } 
		 else {
			 return result;
		 }
	 }
	
		
	
	

	
	public static void main(String[] args){
		double[][] x={{0,1},{1,0},{0,2},{2,1},{1,3},{2,2}};
		int[] y={-1,1,-1,1,-1,1};
		VotedPerceptron vp=new VotedPerceptron();
		vp.train(x, y);
		vp.saveModel("/media/4083BE7D790F6BE0/seg/1.model");
		vp.predictProb(x);
		//vp = vp.loadModel("/media/4083BE7D790F6BE0/seg/1.model");
	}		
}
	