/**
 * 
 */
package edu.hitsz.ml.application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import edu.hitsz.ml.evaluation.ClassificationEvaluation;
import edu.hitsz.ml.learning.Instances;
import edu.hitsz.ml.onlinelearning.VotedPerceptron;

/**
 * NIST OCR database
 * 
 * @author tm
 *
 */
public class NISTOCR {

	
	public void trainFile(String imageFileName, String labelFileName, String modelFileName){
		File imageFile = new File(imageFileName);
		File labelFile = new File(labelFileName);
		try{
			//read images and labels
			DataInputStream imageReader=new DataInputStream(new FileInputStream(imageFile));
			int imageMagicNumber = imageReader.readInt();
			int imageNumber = imageReader.readInt();
			int rowNumber = imageReader.readInt();
			int columnNumber = imageReader.readInt();
			DataInputStream labelReader=new DataInputStream(new FileInputStream(labelFile));
			int labelMagicNumber = labelReader.readInt();
			int labelNumber = labelReader.readInt();
			System.out.println("There are "+imageNumber+" numbers of images of "+rowNumber+"*"+columnNumber+" in train image file");
			System.out.println("There are "+labelNumber+" numbers of labels "+" in train label file");
			if(imageNumber != labelNumber){
				System.out.println("The number of image and label are different");
				System.exit(-1);
			}
			double[][] x = new double[imageNumber][rowNumber*columnNumber];
			String[] y = new String[imageNumber];
			System.out.print("Have read ");
			int i=0;
			for(; i<imageNumber; i++){
				if(i%10000 == 0)
					System.out.println(i+"...");
				else if(i%1000 == 0)
					System.out.print(i+"...");
				byte curLabel = labelReader.readByte();
				y[i] = Byte.toString(curLabel);
				for(int j=0; j<rowNumber; j++){
					for(int k=0; k<columnNumber; k++){
						byte pixel = imageReader.readByte();
						x[i][j*columnNumber+k] = Math.abs(pixel-0.0) < 1e-5 ? 0 : 1.0;
					}
				}
			}	
			System.out.println(i);
			imageReader.close();			
			labelReader.close();	
			System.out.println("done");
			//add to instance
			Instances newInst = new Instances();
			newInst.addFeatures(x);
			newInst.addResults(y);
			//train multiclass classifiers
			for(i=0; i<newInst.classNumber; i++){
				VotedPerceptron vp = new VotedPerceptron();
				vp.train(newInst.features,newInst.results[i]);
				vp.saveModel(modelFileName+newInst.classes.get(i));
			}			
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}			
	}	
	
	
	public void predictFile(String testimageFileName, String modelFileName, String trainimageFileName, String testlabelFileName){
		File testimageFile = new File(testimageFileName);
		try{
			DataInputStream testimageReader=new DataInputStream(new FileInputStream(testimageFile));
			int testimageMagicNumber = testimageReader.readInt();
			int testimageNumber = testimageReader.readInt();
			int testrowNumber = testimageReader.readInt();
			int testcolumnNumber = testimageReader.readInt();
			System.out.println("There are "+testimageNumber+" numbers of images of "+testrowNumber+"*"+testcolumnNumber+" in test image file");
			double[][] x = new double[testimageNumber][testrowNumber*testcolumnNumber];
			System.out.print("Have read ");
			int i=0;
			for(; i<testimageNumber; i++){
				if(i%10000 == 0)
					System.out.println(i+"...");
				else if(i%1000 == 0)
					System.out.print(i+"...");
				for(int j=0; j<testrowNumber; j++){
					for(int k=0; k<testcolumnNumber; k++){
						byte pixel = testimageReader.readByte();
						x[i][j*testcolumnNumber+k] = Math.abs(pixel-0.0) < 1e-5 ? 0 : 1.0;
					}
				}
			}	
			System.out.println(i);
			testimageReader.close();
			System.out.println("done");
			//
			File imageFile = new File(trainimageFileName);
			DataInputStream imageReader=new DataInputStream(new FileInputStream(imageFile));
			int imageMagicNumber = imageReader.readInt();
			int imageNumber = imageReader.readInt();
			int rowNumber = imageReader.readInt();
			int columnNumber = imageReader.readInt();
			System.out.println("There are "+imageNumber+" numbers of images of "+rowNumber+"*"+columnNumber+" in train image file");
			double[][] trainx = new double[imageNumber][rowNumber*columnNumber];
			System.out.print("Have read ");
			i=0;
			for(; i<imageNumber; i++){
				if(i%10000 == 0)
					System.out.println(i+"...");
				else if(i%1000 == 0)
					System.out.print(i+"...");
				for(int j=0; j<rowNumber; j++){
					for(int k=0; k<columnNumber; k++){
						byte pixel = imageReader.readByte();
						trainx[i][j*columnNumber+k] = Math.abs(pixel-0.0) < 1e-5 ? 0 : 1.0;
					}
				}
			}	
			System.out.println(i);
			imageReader.close();	
			System.out.println("done");
			//
			Vector<VotedPerceptron> vp = new Vector<VotedPerceptron>();			
			for(i=0; i<10; i++){
				VotedPerceptron newVp = new VotedPerceptron();
				newVp.loadModel(modelFileName+i);
				newVp.addInstanceX(trainx);
				vp.add(newVp);
			}			
			//
			byte[] y = new byte[testimageNumber];	
			System.out.print("Have processed ");
			for(i=0; i<testimageNumber; i++){
				if(i%1000 == 0)
					System.out.println(i+"...");
				else if(i%100 == 0)
					System.out.print(i+"...");
				double tmp = 0;
				y[i] = 0;
				for(byte j=0; j<10; j++){
					double prob = vp.get(j).predictProb(x[i]);
					if(prob > tmp){
						y[i] = j;
						tmp = prob;
					}
				}
			}			
			System.out.println("done");
			File labelFile = new File(testlabelFileName);
			DataOutputStream labelWriter= new DataOutputStream(new FileOutputStream(labelFile));
			labelWriter.writeInt(testimageMagicNumber);
			labelWriter.writeInt(testimageNumber);
			for(i=0; i<testimageNumber; i++)
				labelWriter.writeByte(y[i]);
			labelWriter.close();			
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}			
	}
	
	public void evalution(String goldLabelFileName, String predictLabelFileName, String evalFileName){
		File goldLabelFile = new File(goldLabelFileName);
		File predictLabelFile = new File(predictLabelFileName);
		try{
			//read gold labels
			DataInputStream goldLabelReader=new DataInputStream(new FileInputStream(goldLabelFile));
			int goldLabelMagicNumber = goldLabelReader.readInt();
			int goldLabelNumber = goldLabelReader.readInt();
			System.out.println("There are "+goldLabelNumber+" numbers of labels "+" in gold label file");
			String[] goldy = new String[goldLabelNumber];
			System.out.print("Have read ");
			int i=0;
			for(; i<goldLabelNumber; i++){
				if(i%10000 == 0)
					System.out.println(i+"...");
				else if(i%1000 == 0)
					System.out.print(i+"...");
				byte curLabel = goldLabelReader.readByte();
				goldy[i] = Byte.toString(curLabel);
			}	
			System.out.println(i);		
			goldLabelReader.close();
			System.out.println("done");
			//read predict labels
			DataInputStream predictLabelReader=new DataInputStream(new FileInputStream(predictLabelFile));
			int predictLabelMagicNumber = predictLabelReader.readInt();
			int predictLabelNumber = predictLabelReader.readInt();
			System.out.println("There are "+predictLabelNumber+" numbers of labels "+" in predict label file");
			String[] predicty = new String[predictLabelNumber];
			System.out.print("Have read ");
			i=0;
			for(; i<predictLabelNumber; i++){
				if(i%10000 == 0)
					System.out.println(i+"...");
				else if(i%1000 == 0)
					System.out.print(i+"...");
				byte curLabel = predictLabelReader.readByte();
				predicty[i] = Byte.toString(curLabel);
			}	
			System.out.println(i);		
			predictLabelReader.close();
			System.out.println("done");
			//eval
			ClassificationEvaluation newEval = new ClassificationEvaluation();
			newEval.eval(goldy, predicty);
			newEval.store(evalFileName);
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	
	
	
	
	
	
	
	
	
	public static void train(){
		String trainImageFileName = "/media/4083BE7D790F6BE0/ocr/train-images.idx3-ubyte";
		String trainLabelFileName = "/media/4083BE7D790F6BE0/ocr/train-labels.idx1-ubyte";
		String modelFileName = "/media/4083BE7D790F6BE0/ocr/train-model";
		String t10kimageFileName = "/media/4083BE7D790F6BE0/ocr/t10k-images.idx3-ubyte";
		String pt10klabelFileName = "/media/4083BE7D790F6BE0/ocr/p-t10k-labels.idx3-ubyte";
		String t10klabelFileName = "/media/4083BE7D790F6BE0/ocr/t10k-labels.idx1-ubyte";
		String evalFileName = "/media/4083BE7D790F6BE0/ocr/t10k-evaluation";
		NISTOCR newOCR = new NISTOCR();
		//newOCR.trainFile(trainImageFileName, trainLabelFileName, modelFileName);
		newOCR.predictFile(t10kimageFileName, modelFileName, trainImageFileName, pt10klabelFileName);
		//newOCR.evalution(t10klabelFileName, pt10klabelFileName, evalFileName);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		train();
	}

}
