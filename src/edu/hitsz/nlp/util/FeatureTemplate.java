package edu.hitsz.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * 特征模板文件
 * [i,j] //for jth column of ith row, j=-1 represents the last column
 * [i,j]&[m,n] //combination of two feature
 *
 * @author tm
 *
 */


public class FeatureTemplate {

	public class FeaPair{
		public int row;
		public int column;
	}

	public Vector<Vector<FeaPair>> template;
	public Vector<String> templateString;

	public int getFeatureNumber(){
		return template.size();
	}

	public String getFeatureString(int i){
		return templateString.get(i);
	}

	public FeatureTemplate(){
		template=new Vector<Vector<FeaPair>>();
		templateString = new Vector<String>();
	}



	/**
	 * read feature template from template file
	 * @param outPath
	 * @param tempName
	 */
	public void readFromFile(String tempName){
		File file = new File(tempName);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("\nRead Template file successed:");
			String tempString=null;
			try{
				while ((tempString = reader.readLine())!= null){
					if (!(tempString.trim().equals(""))&&(tempString!=null)){
						if(tempString.startsWith("//") || tempString.startsWith("#"))
							continue;
						System.out.println(tempString);
						templateString.add(tempString);
						Vector<FeaPair> singleTemplate=new Vector<FeaPair>();
						String[] singleString=tempString.split("&");
						for(int i=0;i<singleString.length;i++){
							int len=singleString[i].length();
							String singlePosition=singleString[i].substring(1, len-1);
							String[] singlePos=singlePosition.split(",");
							if(singlePos.length==2){
								FeaPair newPair=new FeaPair();
								newPair.row=Integer.parseInt(singlePos[0]);
								newPair.column=Integer.parseInt(singlePos[1]);
								singleTemplate.add(newPair);
							}
						}
						template.add(singleTemplate);
					}
				}
				try {
					reader.close();
					}
				catch (IOException e) {
					e.printStackTrace();
					}
				}
			catch (FileNotFoundException e) {
				System.err.println(e);
				}
			}
		catch (IOException e){
			System.out.println("IOException: " + e);
			}
	}

	/**
	 * Display the template to the screen.
	 */
	public void outputTemplate(){
		int templateSize=template.size();
		if(templateSize>0){
			for(int i=0;i<templateSize;i++){
				int singleTemplateSize=template.get(i).size();
				if(singleTemplateSize>0){
					String tmpString="";
					for(int j=0;j<singleTemplateSize;j++){
						tmpString+="["+template.get(i).get(j).row+","+template.get(i).get(j).column+"]\t";
					}
					System.out.println(tmpString);
				}
				else{
					System.out.println("There is an empty feature template. It must be WRONG!");
					break;
				}
			}
		}
		else{
			System.out.println("There is no feature template.");
		}
	}

	private static void test(){
		FeatureTemplate newTemplate=new FeatureTemplate();
		String templateName="/windows/F/experiments/joint/chunktemplate";
		newTemplate.readFromFile(templateName);
		newTemplate.outputTemplate();
	}





}
