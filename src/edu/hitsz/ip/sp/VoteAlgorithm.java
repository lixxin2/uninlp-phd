package edu.hitsz.ip.sp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class VoteAlgorithm {
	
	
	public void simpleVote(String dirName, String outDirName) throws IOException {
		
		File dir = new File(dirName);
		if(dir.isDirectory()) {
			//子目录,目录名(矩名)
			File[] subDirs = dir.listFiles();
			int dirNumber = subDirs.length;
			String[] dirNames = new String[dirNumber];
			for(int i=0; i<dirNumber; i++) {
				dirNames[i] = subDirs[i].getName();				
			}
			//文件名
			File[] oneDir = subDirs[0].listFiles();
			int fileNumber = oneDir.length;
			String[] fileNames = new String[fileNumber];
			for(int i=0; i<fileNumber; i++) {
				fileNames[i] = oneDir[i].getName().split("\\.")[0];	
				System.out.println(fileNames[i]);
			}
			//读取所有文件名
			File[][] allFiles = new File[dirNumber][fileNumber];
			for(int i=0; i<dirNumber; i++) {
				File[] subFiles = subDirs[i].listFiles();
				for(int j=0; j<fileNumber; j++) {
					boolean bFindFile = false;
					for(int k=0; k<fileNumber; k++) {
						if(subFiles[j].getName().split("\\.")[0].equals(fileNames[k])) {
							bFindFile = true;
							allFiles[i][k] = subFiles[j];
							break;
						}							
					}
					if(!bFindFile) {
						System.out.println("file is wrong: " + subFiles[j].getPath());
						System.exit(-1);
					}
				}
			}
			for(int i=0; i<dirNumber; i++) {
				for(int j=0; j<fileNumber; j++) {
					//System.out.println(allFiles[i][j].getPath());		
				}
			}	
			
			//处理每个文件
			for(int i=0; i<fileNumber; i++) {				
				if(fileNames[i].equals("result_of_ssi_level0"))
					System.out.println();
				String outFileName = outDirName + File.separator + fileNames[i];
				FileWriter writer = new FileWriter(outFileName);		
				int symbolNumber = 0;
				ArrayList<String> symbolNames = new ArrayList<String>();
				ArrayList<String> goldSymbols = new ArrayList<String>();
				HashMap<String, HashMap<String, Integer>> predictSymbolMap = new HashMap<String, HashMap<String, Integer>>();

				//每个目录(矩), 保存预测symbol数目
				for(int j=0; j<dirNumber; j++) {	
					ArrayList<String> cursymbolNames = new ArrayList<String>();
					ArrayList<String> curgoldSymbols = new ArrayList<String>();
					ArrayList<String> curpredictSymbols = new ArrayList<String>();
					String curFileName = allFiles[j][i].getName().split("\\.")[0];
					if(!curFileName.equals(fileNames[i])) {
						System.out.println("no corresponding file");
						System.exit(-1);
					}
					int curSymbolNumber = getSymbolNames(allFiles[j][i], cursymbolNames, curgoldSymbols, curpredictSymbols);
					if(symbolNumber == 0) {
						symbolNumber = curSymbolNumber;
						symbolNames = cursymbolNames;
						goldSymbols = curgoldSymbols;
					}
					else if(symbolNumber != curSymbolNumber) {
						System.out.println("symbols is different between " + allFiles[0][i].getPath() 
								+ " and " + allFiles[j][i].getPath());
						System.exit(-1);
					}
					for(int k=0; k<symbolNumber; k++) {
						put(symbolNames.get(k), curpredictSymbols.get(k), predictSymbolMap);
					}
					if(symbolNumber != predictSymbolMap.size()) {
						System.out.println("symbols number is different between " + allFiles[0][i].getPath() 
								+ " and " + allFiles[j][i].getPath());
						System.exit(-1);
					}
				}
				//保存最优的结果,simple vote
				int cor = 0;
				for(int k=0; k<symbolNumber; k++) {
					String symbolName = symbolNames.get(k);
					String predictSymbol = simpleVote(symbolName, predictSymbolMap);
					String yesNo = goldSymbols.get(k).equals(predictSymbol) ? "Yes":"No";
					cor += yesNo.equals("Yes")?1:0;
					writer.write(symbolNames.get(k) + "     " + goldSymbols.get(k) 
							+ "     " + predictSymbol + "     " + yesNo + "\n");
				}
				writer.write("\nCorrection:"+cor/(double)symbolNumber + "\n");
				writer.write("---------------------------------------------" + "\n");
				writer.close();				
			}			
		}//close dir		
	}
	
	/** 将正确symbol和预测symbol存入map */
	private void put(String symbolName, String curpredictSymbol, HashMap<String, HashMap<String, Integer>> predictSymbolMap) {
		if(!predictSymbolMap.containsKey(symbolName)) 
			predictSymbolMap.put(symbolName, new HashMap<String, Integer>());
		
		HashMap<String, Integer> symbolMap = predictSymbolMap.get(symbolName);
		if(symbolMap.containsKey(curpredictSymbol)) 
			symbolMap.put(curpredictSymbol, symbolMap.get(curpredictSymbol)+1);
		else
			symbolMap.put(curpredictSymbol, 1);		
		
	}
	
	/** 返回次数最多的symbol */
	private String simpleVote(String symbolName, HashMap<String, HashMap<String, Integer>> predictSymbolMap) {
		if(!predictSymbolMap.containsKey(symbolName)) {
			System.out.println("no symbol: " + symbolName);
			System.exit(-1);
		}
		
		HashMap<String, Integer> symbolMap = predictSymbolMap.get(symbolName);
		int max = 0;
		String bestSymbol = "None";
		Iterator<Entry<String, Integer>> iter = symbolMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String curSymbol = entry.getKey();
			int curNumber = entry.getValue();
			if(curNumber > max) {
				max = curNumber;
				bestSymbol = curSymbol;
			}
		}
		return bestSymbol;		
	}
	
	public int getSymbolNames(File file, ArrayList<String> symbolNames, 
			ArrayList<String> goldSymbols, ArrayList<String> predictSymbols) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = reader.readLine();
		int count = 0;
		while((line != null)) {
			line = line.trim();
			String parts[] = line.split("[ \t]");
			line = reader.readLine();
			if(parts.length < 10)
				continue;
			count += 1;
			int index=0;
			for(int i=0; i<parts.length; i++) {
				if(parts[i].length() > 0) {
					if(index == 0)
						symbolNames.add(parts[i]);
					else if(index == 1)
						goldSymbols.add(parts[i]);
					else if(index == 2) {
						predictSymbols.add(parts[i]);
						break;
					}
					else
						break;
					index += 1;
				}					
			}
		}	
		reader.close();	
		return count;
	}
	
	
	public static void main(String[] args) throws IOException {
		String dirName = "/home/tm/Documents/vector50";
		String outDirName = "/home/tm/Documents/newvector";
		new VoteAlgorithm().simpleVote(dirName, outDirName);		
	}
	
	

}
