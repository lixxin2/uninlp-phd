/**
 *
 */
package edu.hitsz.nlp.coref;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.struct.CfgTreeHead;




/**
 * @author tm
 *
 */
public class CoreferenceResolution {

	String englishHeadruleFile = "/home/tm/disk/disk1/conll2012/materials/headrules/collins-english-headrules.txt";
	String englishGenderFile = "/home/tm/disk/disk1/conll2012/materials/gender.data";
	String chineseHeadruleFile = "/home/tm/disk/disk1/conll2012/materials/headrules/penn2malt_chn_headrules";

	/**
	 * combine all training files in directory, into a file
	 * @param dir
	 * @param outName
	 */
	public static void combineTrainFile(String dir, String outName){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("auto_conll");

		//store into one file
		try{
			FileWriter outFileWriter=new FileWriter(outName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				newFile.readTrainFile(allFiles.get(i), 0);
				newFile.storeTrainFile(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("\nStore train file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * combine all test files in directory, into a file
	 * @param dir
	 * @param outName
	 */
	public static void combineTestFile(String dir, String outName){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("auto_conll");

		//store into one file
		try{
			FileWriter outFileWriter=new FileWriter(outName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				newFile.readTestFile(allFiles.get(i), 0);
				newFile.storeTestFile(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("\nStore train file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * generate features from training file, Maximum Entropy Style
	 * @param dir
	 * @param feaName
	 */
	public void generateEnglishTrainingFea(String dir, String feaName){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("gold_conll");
		//read headrules file
		CfgTreeHead newTreeHead = new CfgTreeHead();
		newTreeHead.loadHeadFile(englishHeadruleFile);
		//read gender file
		Gender newGender = new Gender();
		newGender.readFile(englishGenderFile);
		WordNet newNet = new WordNet();
		//store all features
		try{
			FileWriter outFileWriter=new FileWriter(feaName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				newFile.readTrainFile(allFiles.get(i),newTreeHead, 0);
				int documentNumber = newFile.getDocumentNumber();
				if(documentNumber > 0){
					for(int m=0; m<documentNumber; m++){
						//System.out.println(m);
						ArrayList<ArrayList<String>> features = new ArrayList<ArrayList<String>>();
						ArrayList<String> outcomes = new ArrayList<String>();
						newFile.getDocument(m).extractEnglishTrainFeatures(features, outcomes, newGender, newNet);
						int trainNumber = features.size();
						if(trainNumber > 0 && outcomes.size() == trainNumber){
							for(int j=0; j<trainNumber; j++){
								int featureNumber = features.get(j).size();
								if(featureNumber > 0){
									String tmp = "";
									for(int k=0; k<featureNumber; k++)
										tmp += " "+ features.get(j).get(k);
									outFileWriter.write(outcomes.get(j)+" "+tmp.trim()+"\n");
								}
							}
						}
					}
				}
			}
			outFileWriter.close();
			System.out.println("\nStore fea file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	
	
	/**
	 * generate features from training file, Maximum Entropy Style
	 * @param dir
	 * @param feaName
	 */
	public void generateChineseTrainingFea(String dir, String feaName){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("gold_conll");
		//read headrules file
		CfgTreeHead newTreeHead = new CfgTreeHead();
		newTreeHead.loadHeadFile(chineseHeadruleFile);
		//store all features
		try{
			FileWriter outFileWriter=new FileWriter(feaName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				newFile.readTrainFile(allFiles.get(i),newTreeHead, 0);
				int documentNumber = newFile.getDocumentNumber();
				if(documentNumber > 0){
					for(int m=0; m<documentNumber; m++){
						//System.out.println(m);
						ArrayList<ArrayList<String>> features = new ArrayList<ArrayList<String>>();
						ArrayList<String> outcomes = new ArrayList<String>();
						newFile.getDocument(m).extractChineseTrainFeatures(features, outcomes);
						int trainNumber = features.size();
						if(trainNumber > 0 && outcomes.size() == trainNumber){
							for(int j=0; j<trainNumber; j++){
								int featureNumber = features.get(j).size();
								if(featureNumber > 0){
									String tmp = "";
									for(int k=0; k<featureNumber; k++)
										tmp += " "+ features.get(j).get(k);
									outFileWriter.write(outcomes.get(j)+" "+tmp.trim()+"\n");
								}
							}
						}
					}
				}
			}
			outFileWriter.close();
			System.out.println("\nStore fea file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	/**
	 * generate features of all test files into one file, Maximum Entropy Style
	 * @param dir Directory of training files
	 * @param feaName File name of generated features
	 * @param mentionExist  Whether the mentions have been annotated or not: True=yes, False=no
	 */
	public void generateEnglishTestFea(String dir, String feaName, boolean mentionExist){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("auto_conll");
		//read headrules file
		CfgTreeHead newTreeHead = new CfgTreeHead();
		newTreeHead.loadHeadFile(englishHeadruleFile);
		//read gender file
		Gender newGender = new Gender();
		newGender.readFile(englishGenderFile);
		WordNet newNet = new WordNet();

		//store all features
		try{
			//for all file into one feature file
			FileWriter outFileWriter=new FileWriter(feaName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				if (mentionExist)
					newFile.readTrainFile(allFiles.get(i), newTreeHead, 0);
				else
					newFile.readTestFile(allFiles.get(i), newTreeHead, 0);
				int documentNumber = newFile.getDocumentNumber();
				if(documentNumber > 0){
					for(int m=0; m<documentNumber; m++){
						System.out.println(m);
						newFile.getDocument(m).extractEnglishTestFeatures(newGender, newNet, outFileWriter);
					}
				}
			}
			outFileWriter.close();
			System.out.println("\nStore fea file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * generate features of all test files into one file, Maximum Entropy Style
	 * @param dir Directory of training files
	 * @param feaName File name of generated features
	 * @param mentionExist  Whether the mentions have been annotated or not: True=yes, False=no
	 */
	public void generateChineseTestFea(String dir, String feaName, boolean mentionExist){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("auto_conll");
		//read headrules file
		CfgTreeHead newTreeHead = new CfgTreeHead();
		newTreeHead.loadHeadFile(chineseHeadruleFile);

		//store all features
		try{
			//for all file into one feature file
			FileWriter outFileWriter=new FileWriter(feaName);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				if (mentionExist)
					newFile.readTrainFile(allFiles.get(i), newTreeHead, 0);
				else
					newFile.readTestFile(allFiles.get(i), newTreeHead, 0);
				int documentNumber = newFile.getDocumentNumber();
				if(documentNumber > 0){
					for(int m=0; m<documentNumber; m++){
						System.out.println(m);
						newFile.getDocument(m).extractChineseTestFeatures(outFileWriter);
					}
				}
			}
			outFileWriter.close();
			System.out.println("\nStore fea file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}




	/**
	 * read prob files, and do the coreference clustering
	 * @param testFileName File name of test
	 * @param probFileName File name of probabilities of test file
	 * @param resultFileName File name of results
	 * @param selectStrategy Strategy of selecting precedent coreferent of a mention. c: cloest; b: best; ap: allprecedent.
	 * @param mentionExist Whether the mentions have been annotated or not: True=yes, False=no
	 */
	public void generateTestCoreference(String testFileName, String probFileName, String resultFileName, String selectStrategy, boolean mentionExist){
		Conll11File newFile = new Conll11File();
		System.out.println("\nFile "+testFileName);
		if (mentionExist)
			newFile.readTrainFile(testFileName, 0);
		else
			newFile.readTestFile(testFileName, 0);
		int documentNumber = newFile.getDocumentNumber();
		if(documentNumber > 0){
			//read prob file
			ArrayList<Double> probs = new ArrayList<Double>();
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(probFileName));
				System.out.println("Read Training file successed:");
				String tempString=null;
				try{
					while ((tempString = reader.readLine())!= null)
						probs.add(Double.parseDouble(tempString));
					reader.close();
				}
				catch (FileNotFoundException e) {
					System.err.println(e);
				}
			}
			catch (IOException e){
				System.out.println("IOException: " + e);
			}
			//pass probs to ArrayList
			int probsNumber = probs.size();
			int probSeq=0;
			ArrayList<ArrayList<ArrayList<Double>>> coreferenceProbs = new ArrayList<ArrayList<ArrayList<Double>>>();
			for(int m=0; m<documentNumber; m++){
				int coreferenceNumber = newFile.getDocument(m).getTotalCoreference().size();
				if(coreferenceNumber > 1){
					ArrayList<ArrayList<Double>> CoreferenceProb = new ArrayList<ArrayList<Double>>();
					for(int j=0; j<coreferenceNumber; j++){
						ArrayList<Double> oneCoreferenceProb = new ArrayList<Double>();
						for(int k=0; k<j; k++)
							oneCoreferenceProb.add(probs.get(probSeq++));
						CoreferenceProb.add(oneCoreferenceProb);
					}
					coreferenceProbs.add(CoreferenceProb);
				}
			}
			if(probSeq != probsNumber){
				System.out.println("probSeq:"+probSeq);
				System.out.println("probsNumber:"+probsNumber);
				System.out.println("the probabilites of coreference is not equal");
				System.exit(1);
			}
			probs.clear();
			//
			for(int m=0; m<documentNumber; m++){
				System.out.println(m);
				if(selectStrategy.toLowerCase().equals("c") || selectStrategy.toLowerCase().equals("closest"))
					newFile.getDocument(m).postProcessCoreferenceClosest(coreferenceProbs.get(m));
				else if(selectStrategy.toLowerCase().equals("b") || selectStrategy.toLowerCase().equals("best"))
					newFile.getDocument(m).postProcessCoreferenceHighest(coreferenceProbs.get(m));
				else if(selectStrategy.toLowerCase().equals("a") || selectStrategy.toLowerCase().equals("all"))
					newFile.getDocument(m).postProcessCoreferenceAllPrecedent(coreferenceProbs.get(m));
				else{
					System.out.println("no coreferent cluster algorithm is chosen: c, h, a");
					System.exit(-1);
				}

			}
		}
		newFile.storeTrainFile(resultFileName);
	}


	/**
	 * read prob files, and do the coreference clustering
	 * @param testFileName
	 * @param probFileName
	 * @param resultFileName
	 */
	public void generateTestCoreferenceWithExistAnnotation(String testFileName, String probFileName, String resultFileName){
		Conll11File newFile = new Conll11File();
		System.out.println("\nFile "+testFileName);
		newFile.readTrainFile(testFileName, 0);
		int documentNumber = newFile.getDocumentNumber();
		if(documentNumber > 0){
			//read prob file
			ArrayList<Double> probs = new ArrayList<Double>();
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(probFileName));
				System.out.println("Read Training file successed:");
				String tempString=null;
				try{
					while ((tempString = reader.readLine())!= null)
						probs.add(Double.parseDouble(tempString));
					reader.close();
				}
				catch (FileNotFoundException e) {
					System.err.println(e);
				}
			}
			catch (IOException e){
				System.out.println("IOException: " + e);
			}
			//pass probs to ArrayList
			int probsNumber = probs.size();
			int probSeq=0;
			ArrayList<ArrayList<ArrayList<Double>>> coreferenceProbs = new ArrayList<ArrayList<ArrayList<Double>>>();
			for(int m=0; m<documentNumber; m++){
				int coreferenceNumber = newFile.getDocument(m).getTotalCoreference().size();
				if(coreferenceNumber > 1){
					ArrayList<ArrayList<Double>> CoreferenceProb = new ArrayList<ArrayList<Double>>();
					for(int j=0; j<coreferenceNumber; j++){
						ArrayList<Double> oneCoreferenceProb = new ArrayList<Double>();
						for(int k=0; k<j; k++)
							oneCoreferenceProb.add(probs.get(probSeq++));
						CoreferenceProb.add(oneCoreferenceProb);
					}
					coreferenceProbs.add(CoreferenceProb);
				}
			}
			if(probSeq != probsNumber){
				System.out.println("probSeq:"+probSeq);
				System.out.println("probsNumber:"+probsNumber);
				System.out.println("the probabilites of coreference is not equal");
				System.exit(1);
			}
			probs.clear();
			//
			for(int m1=0, m2=0; m1<documentNumber&&m2<coreferenceProbs.size(); m1++, m2++){
				int coreferenceNumber = newFile.getDocument(m1).getTotalCoreference().size();
				if(coreferenceNumber > 1){
					System.out.println(m1);
					newFile.getDocument(m1).postProcessCoreferenceHighest(coreferenceProbs.get(m2));
				}
				else
					m2 -= 1;
			}
		}
		newFile.storeTrainFile(resultFileName);
	}




	/**
	 * read prob files, and convert them to LP format(linear programming), one lp file for one document
	 * @param testFileName
	 * @param probFileName
	 * @param lpDir
	 * @param mentionExist
	 */
	public void generateTestCoreferenceLpProb(String testFileName, String probFileName, String lpDir, boolean mentionExist){
		Conll11File newFile = new Conll11File();
		System.out.println("\nFile "+testFileName);
		if(mentionExist)
			newFile.readTrainFile(testFileName, 0);
		else
			newFile.readTestFile(testFileName, 0);
		int documentNumber = newFile.getDocumentNumber();
		if(documentNumber > 0){
			//read prob file
			ArrayList<Double> probs = new ArrayList<Double>();
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(probFileName));
				System.out.println("Read Training file successed:");
				String tempString=null;
				try{
					while ((tempString = reader.readLine())!= null)
						probs.add(Double.parseDouble(tempString));
					reader.close();
				}
				catch (FileNotFoundException e) {
					System.err.println(e);
				}
			}
			catch (IOException e){
				System.out.println("IOException: " + e);
			}
			//pass probs to ArrayList
			int probsNumber = probs.size();
			int probSeq=0;
			ArrayList<ArrayList<ArrayList<Double>>> coreferenceProbs = new ArrayList<ArrayList<ArrayList<Double>>>();
			for(int m=0; m<documentNumber; m++){
				int coreferenceNumber = newFile.getDocument(m).getTotalCoreference().size();
				if(coreferenceNumber > 1){
					ArrayList<ArrayList<Double>> CoreferenceProb = new ArrayList<ArrayList<Double>>();
					for(int j=0; j<coreferenceNumber; j++){
						ArrayList<Double> oneCoreferenceProb = new ArrayList<Double>();
						for(int k=0; k<j; k++)
							oneCoreferenceProb.add(probs.get(probSeq++));
						CoreferenceProb.add(oneCoreferenceProb);
					}
					coreferenceProbs.add(CoreferenceProb);
				}
			}
			if(probSeq != probsNumber){
				System.out.println("the probabilites of coreference is not equal");
				System.exit(1);
			}
			probs.clear();
			//
			for(int m1=0, m2=0; m1<documentNumber&&m2<coreferenceProbs.size(); m1++, m2++){
				int coreferenceNumber = newFile.getDocument(m1).getTotalCoreference().size();
				if(coreferenceNumber > 1){
					System.out.println(m1);
					newFile.getDocument(m1).storeCoreferenceProbCplexlp(coreferenceProbs.get(m2), lpDir+"/"+m1+".lp");
				}
				else
					m2 -= 1;
			}
		}
	}

	/**
	 * get result: read prob files, and do the coreference clustering
	 * @param testFileName
	 * @param lpDir
	 * @param resultFileName
	 * @param mentionExist
	 */
	public void generateTestCoreferenceLpResult(String testFileName, String lpDir, String resultFileName, boolean mentionExist){
		Conll11File newFile = new Conll11File();
		System.out.println("\nFile "+testFileName);
		if(mentionExist)
			newFile.readTrainFile(testFileName, 0);
		else
			newFile.readTestFile(testFileName, 0);
		int documentNumber = newFile.getDocumentNumber();
		if(documentNumber > 0){
			for(int i=0; i<documentNumber; i++) {
				//read prob file
				ArrayList<Double> probs = new ArrayList<Double>();
				BufferedReader reader = null;
				try{
					reader = new BufferedReader(new FileReader(lpDir+"/"+i+".prob"));
					System.out.println("Read Training file successed:");
					String tempString=null;
					try{
						while ((tempString = reader.readLine())!= null)
							probs.add(Double.parseDouble(tempString));
						reader.close();
					}
					catch (FileNotFoundException e) {
						System.err.println(e);
					}
				}
				catch (IOException e){
					System.out.println("IOException: " + e);
				}
				//pass probs to ArrayList
				int probSeq = 0;
				int coreferenceNumber = newFile.getDocument(i).getTotalCoreference().size();
				ArrayList<ArrayList<Double>> CoreferenceProb = new ArrayList<ArrayList<Double>>();
				for(int j=0; j<coreferenceNumber; j++){
					ArrayList<Double> oneCoreferenceProb = new ArrayList<Double>();
					for(int k=0; k<j; k++)
						oneCoreferenceProb.add(probs.get(probSeq++));
					CoreferenceProb.add(oneCoreferenceProb);
				}
				if(probSeq != probs.size()){
					System.out.println("probSeq:"+probSeq);
					System.out.println("probsNumber:"+probs.size());
					System.out.println("the probabilites of coreference is not equal");
					System.exit(1);
				}
				probs.clear();
				//process
				System.out.println(i);
				newFile.getDocument(i).postProcessCoreferenceAllPrecedent(CoreferenceProb);

			}
		}
		newFile.storeTrainFile(resultFileName);
	}

	/**
	 * Align the documents in a file with another file
	 * 从existFilaName中抽取与dir中文档名相同的文档，保存到adjustFile
	 * @param dir
	 * @param existFileName
	 * @param adjustFile
	 */
	public void align(String dir, String existFileName, String adjustFile){
		//read all files in a directory
		FileTree newFileTree = new FileTree();
		newFileTree.generateFrom(dir);
		ArrayList<String> allFiles = newFileTree.getFileNamesWithSuffix("250");
		//read headrules file
		//read a existing file
		Conll11File existFile = new Conll11File();
		existFile.readTrainFile(existFileName, 0);
		int existDocumentNumber = existFile.getDocumentNumber();

		//store all features
		try{
			//for all file into one feature file
			FileWriter outFileWriter=new FileWriter(adjustFile);
			int fileNumber = allFiles.size();
			for(int i=0; i<fileNumber; i++){
				Conll11File newFile = new Conll11File();
				System.out.println("\nFile "+i+": "+allFiles.get(i));
				newFile.readTrainFile(allFiles.get(i), 0);
				int documentNumber = newFile.getDocumentNumber();
				if(documentNumber > 0){
					for(int m=0; m<documentNumber; m++){
						for(int n=0; n<existDocumentNumber; n++){
							String curDocumentName = newFile.getDocument(m).getDocumentName()+"/"+newFile.getDocument(m).getPartName();
							if(curDocumentName.equals(existFile.getDocument(n).getDocumentName()+"/"+existFile.getDocument(n).getPartName())){
								//System.out.println(m);
								existFile.getDocument(n).storeTrainDocument(outFileWriter);
							}
						}
					}
				}
			}
			outFileWriter.close();
			System.out.println("\nStore adjust file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	public static void trainingFea(){
		
		String enTrainingDir = "/home/tm/disk/disk1/conll2012/train/english";
		//String trainingDir = "/home/tm/conll/data/train/data/english/annotations/nw";
		//String trainingDir = "/home/tm/conll/test";
		String enFeaName = "/home/tm/disk/disk1/conll2012/data/en_traindev_gold_fea";
		CoreferenceResolution newCR = new CoreferenceResolution();
		newCR.generateEnglishTrainingFea(enTrainingDir, enFeaName);
		//String existFile = "/home/tm/conll/test/qsh/alltrainfile_gold_conll_Result_precision";
		//newCR.generateTrainingFeaForMEWithExistFea(trainingDir,feaName, existFile);
		
		/*
		String chTrainingDir = "/home/tm/disk/disk1/conll2012/train/chinese";
		//String trainingDir = "/home/tm/conll/data/train/data/english/annotations/nw";
		//String trainingDir = "/home/tm/conll/test";
		String chFeaName = "/home/tm/disk/disk1/conll2012/data/ch_traindev_gold_fea";
		CoreferenceResolution chCR = new CoreferenceResolution();
		chCR.generateChineseTrainingFea(chTrainingDir, chFeaName);
		*/
	}


	public static void testFea(){
		
		
		String testDir = "/home/tm/disk/disk1/conll2012/train/english/development";
		String feaName = "/home/tm/disk/disk1/conll2012/data/en_dev_auto_fea";
		
		//CoreferenceResolution newCR = new CoreferenceResolution();
		//newCR.generateEnglishTestFea(testDir, feaName, false);
		
		
		
		String testDir2 = "/home/tm/disk/disk1/conll2012/conll-2012";
		String feaName2 = "/home/tm/disk/disk1/conll2012/data/ch_final_test_auto_fea";
		CoreferenceResolution newCR2 = new CoreferenceResolution();
		newCR2.generateChineseTestFea(testDir2, feaName2, false);
		
	}

	public static void generateTest(String testFileName, String probFileName, String resultFileName){
		//String testDir = "/home/tm/conll/data/dev";
		ArrayList<Integer> feaLists = new ArrayList<Integer>();
		feaLists.add(1);feaLists.add(7);feaLists.add(13);feaLists.add(27);
		feaLists.add(28);feaLists.add(30);feaLists.add(32);feaLists.add(36);
		feaLists.add(37);feaLists.add(50);feaLists.add(61);feaLists.add(62);
		//for(int i=1; i<=65; i++){
			//if(!feaLists.contains(i)){
				//String testFileName = "/home/tm/conll/test/dev_auto_conll";
				CoreferenceResolution newCR = new CoreferenceResolution();
				newCR.generateTestCoreference(testFileName, probFileName, resultFileName, "b", false);
			//}
		//}
	}

	public static void testGenerateLP(){
		//String testFileName = "/home/tm/conll/test/dev_all_gold_conll";
		String testFileName = "/home/tm/conll/test/dev_auto_conll_coref_number_150_gold";
		//String probFileName = "/media/4083BE7D790F6BE0/test/prob_dev_all_gold";
		String probFileName = "/media/4083BE7D790F6BE0/coreference/dev_150_prob";
		//String resultFileName = "/home/tm/conll/test/scorer/result_dev_all_gold_lp";//+"_"+i;
		String resultFileName = "/home/tm/conll/test/scorer/dev_150_0.02_2_lp";
		//String lpDir = "/media/4083BE7D790F6BE0/test/lp/dev-gold";
		String lpDir = "/media/4083BE7D790F6BE0/coreference/lp/dev_150_0.02_2";
		CoreferenceResolution newCR = new CoreferenceResolution();
		newCR.generateTestCoreferenceLpProb(testFileName, probFileName, lpDir, false);
		//lp_solve to get the solutionjMax
		newCR.generateTestCoreferenceLpResult(testFileName, lpDir, resultFileName, false);
	}

	/** 合并原文件，可以是训练文件，可以是测试文件*/
	public static void combine(){

		//CoreferenceResolution.combineTrainFile("/home/tm/disk/disk1/conll2012/train/chinese/development", "/home/tm/disk/disk1/conll2012/data/ch_dev_auto_conll");
		CoreferenceResolution.combineTestFile("/home/tm/disk/disk1/conll2012/conll-2012", "/home/tm/disk/disk1/conll2012/data/ch_final_test_auto_conll");
		
	}

	public static void adjust(){
		CoreferenceResolution newCR = new CoreferenceResolution();
		//newCR.align("/home/tm/disk/disk1/conll2012/data", "/home/tm/disk/disk1/conll2012/data/en_dev_gold_conll", "/home/tm/disk/disk1/conll2012/data/en_dev_200_gold_conll");
		newCR.align("/home/tm/disk/disk1/conll2012/data", "/home/tm/disk/disk1/conll2012/data/ch_dev_gold_conll", "/home/tm/disk/disk1/conll2012/data/ch_dev_250_gold_conll");

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		combine();
		//trainingFea();
		//testFea();
		/*
		if(args.length != 3) {
			System.out.println("wrong parameters. testFileName probFileName resultFileName");
			System.exit(0);
		}
		String testFileName = "/home/tm/disk/disk1/conll2012/data/en_dev_auto_conll_200";
		testFileName = args[0];
		String probFileName = "/home/tm/disk/disk1/conll2012/data/en_dev_200_prob";
		probFileName = args[1];
		String resultFileName = "/home/tm/disk/disk1/conll2012/data/dev_200_result";//+"_"+i;
		resultFileName = args[2];		
		generateTest(testFileName, probFileName, resultFileName);
		*/
		//testGenerateLP();
		//adjust();
	}

}
