/**
 * 
 */
package edu.hitsz.ml.onlinelearning;

/**
 * @author tm
 *
 */
public class Perceptron {
	//
	public int iterNumber=100;
	public double errorRate=1e-5;
	public double singleError=1e-10;
	public double learningRate=1;
	public int weightLen=1;
	public double[] weights;
	
	
	
	public Perceptron(){
		
	}
	
	public void setIterNum(int inputIterNumber){
		this.iterNumber = inputIterNumber;
	}
	
	public void setError(int inputError){
		this.errorRate=inputError;
	}
	
	/**
	 * initialize the weights
	 * @param len
	 */
	public void initWeights(int len){
		weightLen=len+1;
		weights=new double[weightLen];
		for( int i=0;i<weightLen-1;i++){
			//weights[i]=Math.random();
			weights[i]=1;
		}
		weights[weightLen-1]=1;
		//printWeights();
	}
	
	/**
	 * initialize weights along with inputing array
	 * @param inputweights
	 */
	public void initWeights(double [] inputweights){
		weightLen=inputweights.length+1;
		weights=new double[weightLen];
		for( int i=0;i<weightLen-1;i++){
			weights[i]=inputweights[i];
		}
		weights[weightLen-1]=1;
		//printWeights();
	}
	
	public void printWeights(){
		System.out.println("weights");
		for( int i=0;i<weightLen;i++){
			System.out.println(weights[i]);
		}
	}
	
	/**
	 * predict the result of one sample
	 * @param x
	 * @return
	 */
	public int predictC(double[] x){
		double pre=0;
		int xLen=x.length;
		for(int i=0; i< xLen; i++)
			pre+=x[i]*weights[i];
		pre+=weights[weightLen-1];
		if(pre>=0)
			return 1;
		else
			return -1;
	}
	
	/**
	 * iterative parameters for one sample
	 * @param x 
	 * @param y
	 * @return
	 */
	public boolean updateC(double [] x, int y) {
		int xLen=x.length;
		double pre=predictC(x);
		if(Math.abs(y-pre)>singleError){
			int i=0;
			for(;i<xLen;i++){
				weights[i]=weights[i]+learningRate*y*x[i];
			}
			weights[i]=weights[i]+learningRate*y;
			return false;
		}
		else return true;
	}
		
	/**
	 * train parameters for perceptron
	 * @param x features
	 * @param y samples
	 */
	public void trainC(double [][] x, int[] y){
		int xLen=x.length;
		int x2Len=x[0].length;
		if(y.length!=xLen){
			System.out.println("Number of Input and Output of the Perceptron are different!\nPlease check it out.");
		}
		initWeights(x2Len); 
		int allRight=0;
		for(int i=0; i<iterNumber; i++){
			for(int j=0; j<xLen; j++){				
				if(!updateC(x[j],y[j])){
					allRight+=1;
					//printWeights();
				}								
			}
			if(allRight/(double)xLen>1-errorRate)
				break;
		}
	}
		
	
	
	public double predictR(double [] x){
		double pre=0;
		int xLen=x.length;
		for(int i=0; i< xLen; i++)
			pre+=x[i]*weights[i];
		pre+=weights[weightLen-1];
		return pre;
	}
	

	public boolean updateR(double [] x, double y) {
		int xLen=x.length;
		double pre=predictR(x);
		if(Math.abs(y-pre)>singleError){
			int i=0;
			for(;i<xLen;i++){
				weights[i]=weights[i]+learningRate*(y-pre)*x[i];
			}
			weights[i]=weights[i]+learningRate*(y-pre);			
			return false;
		}
		else return true;
	}

	public void trainR(double [][] x,int[] y){
		int xLen=x.length;
		int x2Len=x[0].length;
		if(y.length!=xLen){
			System.out.println("Number of Input and Output of the Perceptron are different!\nPlease check it out.");
		}
		initWeights(x2Len);
		int allRight=0;
		for(int i=0; i<iterNumber; i++){
			for(int j=0; j<xLen; j++){				
				if(!updateR(x[j],y[j])){
					allRight+=1;
					printWeights();
				}								
			}
			if(allRight/(double)xLen>1-errorRate)
				break;
		}
	}
		

	public int[] predictAllC(double [][]x){
		int xLen=x.length;
		int[] y=new int[xLen];
		for(int i=0;i<xLen;i++){
			y[i]=predictC(x[i]);
		}
		return y;				
	}
	
	public double[] predictAllR(double [][]x){
		int xLen=x.length;
		double[] y=new double[xLen];
		for(int i=0;i<xLen;i++){
			y[i]=predictC(x[i]);
		}
		return y;				
	}
	
	
	public static void test(){
		double[][] x={{0,1},{1,0},{0,2},{2,1},{1,3},{2,2}};
		int[] y={-1,1,-1,1,-1,1};
		Perceptron ap=new Perceptron();
		ap.trainC(x,y);
		//ap.printWeights();
		int xLen=x.length;
		int[] z=ap.predictAllC(x);
		for(int i=0;i<xLen;i++)
			System.out.println(z[i]);
	}
	
	
	public static void main(String[] args){		
		test();
	}
		
	
}
