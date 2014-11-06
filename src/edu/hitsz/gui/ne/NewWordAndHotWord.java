package edu.hitsz.gui.ne;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.nerc.NERInstance;
import edu.hitsz.nlp.nerc.NERTrain;
import edu.hitsz.nlp.segmentation.CallStanfordSegmentor;
import edu.hitsz.nlp.struct.ConllFile;

public class NewWordAndHotWord extends JPanel{



	/** 打开测试文件按钮 */
	JButton findButton;
	JPanel topPanelTop;

	JButton newWordButton;
	JPanel newWordPanel;
	JTextArea newWord;
	JScrollPane newWordPane;

	JButton hotWordButton;
	JPanel hotWordPanel;
	JTextArea hotWord;
	JScrollPane hotWordPane;
	Box all;
	Box bottom;
	Box left;
	Box right;

	HashMap<String, Integer> personMap;
	HashMap<String, Integer> locationMap;
	HashMap<String, Integer> organizationMap;

	public NewWordAndHotWord(final String localDir){

		personMap = new HashMap<String, Integer>();
		locationMap = new HashMap<String, Integer>();
		organizationMap = new HashMap<String, Integer>();

		findButton = new JButton("找到新词和热点词");
		topPanelTop = new JPanel();
		topPanelTop.add(findButton);

		findButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				combineSentence(localDir);
				String outFileName = localDir + "result/all";
				String segFileName = outFileName + "-seg";
				//CallStanfordSegmentor.segment("pku", outFileName, "UTF-8", segFileName, 0);
				String charFileName = outFileName+"-char";
				//NERTrain.testWord2CharacterWithoutPOS(segFileName, charFileName);
				String resultFileName = outFileName+"-result";
				String finalFileName = outFileName+"-final";
				//NERTrain.callChineseNER(charFileName, resultFileName, finalFileName);
				String senFileName = outFileName+"-sen";
				String nerFileName = outFileName+"-ner";
				//String finalSentence = NERInstance.character2Sentence(finalFileName, senFileName, nerFileName, "4");
				//
				String personFileName = localDir+"dict/PersonName";
				String locationFileName = localDir+"dict/LocationName";
				String organizationFileName = localDir+"dict/OrganizationName";
				HashMap<String, Integer> allPersonMap = new HashMap<String, Integer>();
				HashMap<String, Integer> allLocationMap = new HashMap<String, Integer>();
				HashMap<String, Integer> allOrganizationMap = new HashMap<String, Integer>();
				readDict(allPersonMap, personFileName);
				readDict(allLocationMap, locationFileName);
				readDict(allOrganizationMap, organizationFileName);

				HashMap<String, Integer> personMap = new HashMap<String, Integer>();
				HashMap<String, Integer> locationMap = new HashMap<String, Integer>();
				HashMap<String, Integer> organizationMap = new HashMap<String, Integer>();
				readCurrentDict(personMap, locationMap, organizationMap, nerFileName);
				StringBuffer newWords = new StringBuffer();
				generateNewWords(allPersonMap, personMap, "PER",newWords);
				generateNewWords(allLocationMap, locationMap, "LOC",newWords);
				generateNewWords(allOrganizationMap, organizationMap, "ORG",newWords);
				newWord.setText(newWords.toString());
				StringBuffer hotWords = new StringBuffer();
				generateHotWords(personMap,locationMap,organizationMap, hotWords );
				hotWord.setText(hotWords.toString());

			}
		});


		newWordButton = new JButton("这是新词");
		newWordPanel = new JPanel();
		newWordPanel.add(newWordButton);

		newWord = new JTextArea(10,10);
		newWord.setLineWrap(true);
		newWord.setWrapStyleWord(true);
		newWordPane = new JScrollPane(newWord);

		left = Box.createVerticalBox();
		left.add(newWordPanel);
		left.add(newWordPane);



		hotWordButton = new JButton("这是热词");
		hotWordPanel = new JPanel();
		hotWordPanel.add(hotWordButton);

		hotWord = new JTextArea(10,10);
		hotWord.setLineWrap(true);
		hotWord.setWrapStyleWord(true);
		hotWordPane = new JScrollPane(hotWord);

		right = Box.createVerticalBox();
		right.add(hotWordPanel);
		right.add(hotWordPane);

		bottom = Box.createHorizontalBox();
		bottom.add(left);
		bottom.add(right);

		all = Box.createVerticalBox();
		all.add(topPanelTop);
		all.add(bottom);


		this.setLayout(new BorderLayout());
		this.add(all, BorderLayout.NORTH);

	}


	public void combineSentence(String localDir){
		String outFileName = localDir + "result/all";
		FileTree newTree = new FileTree();
		newTree.generateFrom(localDir+"clean");
		ArrayList<String> allFiles = newTree.getFileNames();
		if(allFiles.size()>0){
			try{
				FileWriter outWriter = new FileWriter(outFileName);
				for(int i=0; i<allFiles.size(); i++){
					System.out.println(i);
					StringBuffer newBuffer = new StringBuffer();
					BufferedReader reader = null;
					reader = new BufferedReader(new FileReader(new File(allFiles.get(i))));
					String tempString=null;
					while ((tempString = reader.readLine())!= null){
						newBuffer.append(tempString.trim()+"\n");
					}
					reader.close();
					outWriter.write(newBuffer.toString());
				}
				outWriter.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	public void readDict(HashMap<String, Integer> newMap, String fileName){
		try{
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(new File(fileName)));
			String tempString=null;
			while ((tempString = reader.readLine())!= null){
				newMap.put(tempString.trim(),1);
			}
			reader.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	public void readCurrentDict(HashMap<String, Integer> personMap,
			HashMap<String, Integer> locationMap, HashMap<String, Integer> organizationMap,
			String nerFileName){
		try{
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(new File(nerFileName)));
			String tempString=null;
			while ((tempString = reader.readLine())!= null){
				String[] tmp = tempString.trim().split("\t");
				if(tmp[1].equals("PER")){
					if(!personMap.containsKey(tmp[0]))
						personMap.put(tmp[0], 1);
					else{
						personMap.put(tmp[0], personMap.get(tmp[0])+1);
					}
				}
				else if(tmp[1].equals("LOC")){
					if(!locationMap.containsKey(tmp[0]))
						locationMap.put(tmp[0], 1);
					else{
						locationMap.put(tmp[0], locationMap.get(tmp[0])+1);
					}
				}
				else if(tmp[1].equals("ORG")){
					if(!organizationMap.containsKey(tmp[0]))
						organizationMap.put(tmp[0], 1);
					else{
						organizationMap.put(tmp[0], organizationMap.get(tmp[0])+1);
					}
				}
			}
			reader.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}


	public void generateNewWords(HashMap<String, Integer> allPersonMap,
			HashMap<String, Integer> personMap, String type, StringBuffer newWords){
		Iterator iter = personMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String person = (String) entry.getKey();
		    if(!allPersonMap.containsKey(person)){
		    	newWords.append(person+"\t"+type+"\n");
		    }
		}
	}

	public void generateHotWords(HashMap<String, Integer> personMap,
			HashMap<String, Integer> locationMap,HashMap<String, Integer> organizationMap,
			StringBuffer hotWords ){
		Iterator iter = personMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String ner = (String) entry.getKey();
		    Integer nerNumber = (Integer) entry.getValue();
		    if(nerNumber >=3){
		    	hotWords.append(ner+"\tPER\n");
		    }
		}
		iter = locationMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String ner = (String) entry.getKey();
		    Integer nerNumber = (Integer) entry.getValue();
		    if(nerNumber >=3){
		    	hotWords.append(ner+"\tLOC\n");
		    }
		}
		iter = organizationMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String ner = (String) entry.getKey();
		    Integer nerNumber = (Integer) entry.getValue();
		    if(nerNumber >=3){
		    	hotWords.append(ner+"\tORG\n");
		    }
		}

	}







	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
