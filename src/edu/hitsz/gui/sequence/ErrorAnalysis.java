package edu.hitsz.gui.sequence;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import edu.hitsz.ml.onlinelearning.AveragedPerceptron;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.FeatureTemplate;
import edu.hitsz.nlp.util.SequenceTagCandidate;

public class ErrorAnalysis extends JPanel{

	/** 打开测试文件按钮 */
	JButton openTestFileButton;
	/** 打开测试文件Frame */
	JFrame openTestFileFrame;
	/** 测试文件选择器 */
	JFileChooser testFileChooser;
	/** 测试文件名称 */
	String testFileName;
	/** 测试文件 */
	ConllFile testFile;
	boolean testFileChosen = false;

	/** 打开特征模板文件按钮 */
	JButton openTemplateFileButton;
	/** 打开特征模板文件Frame */
	JFrame openTemplateFileFrame;
	/** 特征模板文件选择器 */
	JFileChooser templateFileChooser;
	/** 特征模板文件名称 */
	String templateFileName;
	/** 特征模板文件 */
	FeatureTemplate templateFile;
	boolean templateFileChosen = false;
	int featureNumber = 0;


	/** 打开标识候选文件按钮 */
	JButton openTagCandidateFileButton;
	/** 打开标识候选文件Frame */
	JFrame openTagCandidateFileFrame;
	/** 标识候选文件选择器 */
	JFileChooser tagCandidateFileChooser;
	/** 标识候选文件 */
	String tagCandidateFileName;
	/** 标识候选文件名称 */
	SequenceTagCandidate tagCandidateFile;
	boolean tagCandidateFileChosen = false;
	int tagCandidateNumber = 0;


	/** 打开模型文件按钮 */
	JButton openModelFileButton;
	/** 打开模型文件Frame */
	JFrame openModelFileFrame;
	/** 模型文件选择器 */
	JFileChooser modelFileChooser;
	/** 模型文件名称 */
	String modelFileName;
	/** 模型文件 */
	AveragedPerceptron trainAp;
	boolean modelFileChosen = false;


	/** 打开gold文件按钮 */
	JButton openGoldFileButton;
	/** 打开gold文件Frame */
	JFrame openGoldFileFrame;
	/** gold文件选择器 */
	JFileChooser goldFileChooser;
	/** gold文件名称 */
	String goldFileName;
	/** gold文件 */
	ConllFile goldFile;
	boolean goldFileChosen = false;
	int sentenceNumber = 0;
	int sentenceLength = 0;

	JButton loadFileButton;
	JPanel topPanelTop;

	/** 选择哪个句子 */
	JTextField selectedSentenceTextField;
	/** 选择句子按钮 */
	JButton selectedSentenceButton;
	JPanel topPanelBottom;
	Box top;


	/** 存放表结构 */
	DefaultTableModel sentenceTableModel;
	JTable sentenceTable;
	JScrollPane sentencePane;
	Box middle;

	JButton nextStepButton;

	/** 输出状态栏 */
	JTextArea status;
	JScrollPane bottomPane;

	/** 正在处理的句子 */
	ConllSentence testSentence;
	int currentSeq=0;




	public ErrorAnalysis(){

		openTestFileButton = new JButton("Open Test File");
		openTestFileFrame = new JFrame("Opening Test File");
		testFileChooser = new JFileChooser("/media/4083BE7D790F6BE0/seg/error");
		openTestFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = testFileChooser.showDialog(openTestFileFrame , "open test file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					testFileName = testFileChooser.getSelectedFile().getPath();
					status.append("test file: "+testFileName+" has been chosen\n");
					testFileChosen = true;
				}
			}
		});

		openTemplateFileButton = new JButton("Open Template File");
		openTemplateFileFrame = new JFrame("Opening Template File");
		templateFileChooser = new JFileChooser("/media/4083BE7D790F6BE0/seg/error");
		openTemplateFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = templateFileChooser.showDialog(openTemplateFileFrame , "open template file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					templateFileName = templateFileChooser.getSelectedFile().getPath();
					status.append("template file: "+templateFileName+" has been chosen\n");
					templateFileChosen = true;
				}
			}
		});

		openTagCandidateFileButton = new JButton("Open TagCandidate File");
		openTagCandidateFileFrame = new JFrame("Opening TagCandidate File");
		tagCandidateFileChooser = new JFileChooser("/media/4083BE7D790F6BE0/seg/error");
		openTagCandidateFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = tagCandidateFileChooser.showDialog(openTagCandidateFileFrame , "open Tag Candidate file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					tagCandidateFileName = tagCandidateFileChooser.getSelectedFile().getPath();
					status.append("tag candidate file: "+templateFileName+" has been chosen\n");
					tagCandidateFileChosen = true;
				}
			}
		});

		openModelFileButton = new JButton("Open Model File");
		openModelFileFrame = new JFrame("Opening Model File");
		modelFileChooser = new JFileChooser("/media/4083BE7D790F6BE0/seg/error");
		openModelFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = modelFileChooser.showDialog(openModelFileFrame , "open Model file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					modelFileName = modelFileChooser.getSelectedFile().getPath();
					status.append("model file: "+modelFileName+" has been chosen\n");
					modelFileChosen = true;
				}
			}
		});


		openGoldFileButton = new JButton("Open Gold File");
		openGoldFileFrame = new JFrame("Opening Gold File");
		goldFileChooser = new JFileChooser("/media/4083BE7D790F6BE0/seg/error");
		openGoldFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = goldFileChooser.showDialog(openGoldFileFrame , "open gold file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					goldFileName = goldFileChooser.getSelectedFile().getPath();
					status.append("gold file: "+goldFileName+" has been chosen\n");
					goldFileChosen = true;
				}
			}
		});

		loadFileButton = new JButton("load files");
		loadFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(testFileChosen && templateFileChosen && tagCandidateFileChosen && modelFileChosen && goldFileChosen){
					testFile = new ConllFile();
					status.append("load test file...");
					testFile.readFrom(testFileName, -1);
					status.append("done\n");
					templateFile=new FeatureTemplate();
					status.append("load template file...");
					templateFile.readFromFile(templateFileName);
					status.append("done\n");
					tagCandidateFile = new SequenceTagCandidate();
					status.append("load tag candidate file...");
					tagCandidateFile.readFrom(tagCandidateFileName);
					status.append("done\n");
					trainAp = new AveragedPerceptron();
					status.append("load model file...");
					trainAp.readWeights(modelFileName);
					status.append("done\n");
					goldFile = new ConllFile();
					status.append("load gold file...");
					goldFile.readFrom(goldFileName, -1);
					status.append("done\n");
					if(testFile.getSentenceNumber() == goldFile.getSentenceNumber()){
						sentenceNumber = testFile.getSentenceNumber();
						featureNumber = templateFile.getFeatureNumber();
						tagCandidateNumber = tagCandidateFile.getTagCandidateNumber();
						status.append("There are totol "+testFile.getSentenceNumber()+" sentences\nAll load success\n");
						selectedSentenceTextField.setEnabled(true);
						selectedSentenceButton.setEnabled(true);
					}
					else{
						status.append("The sentence number in testFile "+testFile.getSentenceNumber()+" is different with goldFile "+goldFile.getSentenceNumber()+"\n");
						testFile = null;
						templateFile = null;
						tagCandidateFile = null;
						trainAp = null;
						goldFile = null;
					}
				}
				else if(!testFileChosen){
					status.append("no testFile chosen\n");
				}
				else if(!templateFileChosen){
					status.append("no templateFile chosen\n");
				}
				else if(!tagCandidateFileChosen){
					status.append("no tagCandidateFile chosen\n");
				}
				else if(!modelFileChosen){
					status.append("no modelFile chosen\n");
				}
				else if(!goldFileChosen){
					status.append("no goldFile chosen\n");
				}
			}
		});



		selectedSentenceTextField = new JTextField(20);
		selectedSentenceTextField.setEnabled(false);
		selectedSentenceButton = new JButton("choose sentence");
		selectedSentenceButton.setEnabled(false);
		selectedSentenceButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String sentenceString = selectedSentenceTextField.getText();
				String integerRegex = "^[1-9]\\d*|0$";
				if(sentenceString != null && sentenceString.matches(integerRegex)){
					int currentSentenceNumber = Integer.parseInt(sentenceString);
					if(currentSentenceNumber-1 < sentenceNumber){
						status.append("sentence " + currentSentenceNumber+ " has been choosen\n");
						testSentence = testFile.getSentence(currentSentenceNumber-1);
						sentenceLength = testSentence.getSentenceLength();
						if(sentenceLength == goldFile.getSentence(currentSentenceNumber-1).getSentenceLength()){
							status.append("There are total " + sentenceLength+ " tokens in this sentence, "+featureNumber+" features\n");
							sentenceTableModel.setRowCount(featureNumber+4);
							sentenceTableModel.setColumnCount(sentenceLength+2);
							for(int i=0; i<featureNumber+4; i++)
								for(int j=0; j<sentenceLength+2; j++)
									sentenceTableModel.setValueAt("", i, j);
							sentenceTableModel.setValueAt("sentenceNumber", 0, 0);
							sentenceTableModel.setValueAt("sentence", 1, 0);
							sentenceTableModel.setValueAt("gold", 2, 0);
							for(int i=0;i<featureNumber;i++){
								sentenceTableModel.setValueAt(templateFile.getFeatureString(i), i+3, 0);
							}
							sentenceTableModel.setValueAt("test", featureNumber+3, 0);
							for(int j=0; j<sentenceLength; j++){
								sentenceTableModel.setValueAt(j, 0, j+1);
								sentenceTableModel.setValueAt(goldFile.getSentence(currentSentenceNumber-1).getWords().get(0).get(j), 1, j+1);
								sentenceTableModel.setValueAt(goldFile.getSentence(currentSentenceNumber-1).getWords().get(1).get(j), 2, j+1);
							}
							nextStepButton.setEnabled(true);
						}
						else{
							status.append("sentence length in test file "+sentenceLength+" is different from "
									+goldFile.getSentence(currentSentenceNumber-1).getSentenceLength()+"in gold file\n");
						}
					}
					else{
						status.append("input sentence "+currentSentenceNumber+" is larger than total number "+sentenceNumber+"\n");
					}
				}
			}
		});

		topPanelTop = new JPanel();
		topPanelTop.add(openTestFileButton);
		topPanelTop.add(openTemplateFileButton);
		topPanelTop.add(openTagCandidateFileButton);
		topPanelTop.add(openModelFileButton);
		topPanelTop.add(openGoldFileButton);
		topPanelTop.add(loadFileButton);
		topPanelBottom = new JPanel();
		topPanelBottom.add(selectedSentenceTextField);
		topPanelBottom.add(selectedSentenceButton);
		top = Box.createVerticalBox();
		top.add(topPanelTop);
		top.add(topPanelBottom);

		sentenceTableModel = new DefaultTableModel(5,5);
		sentenceTable = new JTable(sentenceTableModel);
		sentenceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		sentencePane = new JScrollPane(sentenceTable);
		sentencePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		nextStepButton = new JButton("next step");
		nextStepButton.setEnabled(false);
		nextStepButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(currentSeq<sentenceLength){
					if(currentSeq ==0)
						testSentence.predictSignal.clear();
					ArrayList<String> oneFeature=new ArrayList<String>();
					//testSentence.extractTokenFeaturesFromTmpt(oneFeature, templateFile,currentSeq);
					for(int i=0; i<oneFeature.size(); i++)
						sentenceTableModel.setValueAt(oneFeature.get(i), i+3, currentSeq+1);
					double weight=0;
					ArrayList<String> candidateTags;
					if(currentSeq==0)
						candidateTags = tagCandidateFile.getStart();
					else{
						String previousTag = testSentence.predictSignal.get(currentSeq-1);
						candidateTags = tagCandidateFile.getNext(previousTag);
					}
					String tmpResultSignal  = candidateTags.get(0);
					//iterative every possible tag
					for(int k=0;k<candidateTags.size();k++){
						double tmpWeight=trainAp.compSingleWeights(oneFeature, candidateTags.get(k));
						status.append("current tags is "+ candidateTags.get(k) + ", value is " + tmpWeight + "\n");
						if(tmpWeight>weight){
							weight=tmpWeight;
							tmpResultSignal=candidateTags.get(k);
						}
					}
					status.append("final tags is "+ tmpResultSignal + "\n");
					sentenceTableModel.setValueAt(tmpResultSignal, oneFeature.size()+3, currentSeq+1);
					testSentence.predictSignal.add(tmpResultSignal);
					currentSeq++;
				}
				else{
					status.append("The end of the sentence. Please press the select button again\n");
				}
			}
		});

		middle = Box.createVerticalBox();
		middle.add(sentencePane);
		middle.add(nextStepButton);

		status = new JTextArea(5,10);
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		bottomPane = new JScrollPane(status);

		this.setLayout(new BorderLayout());
		this.add(top, BorderLayout.NORTH);
		this.add(middle, BorderLayout.CENTER);
		this.add(bottomPane, BorderLayout.SOUTH);

	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
