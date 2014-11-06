package edu.hitsz.gui.ne;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.hitsz.nlp.nerc.NERInstance;
import edu.hitsz.nlp.nerc.NERTrain;
import edu.hitsz.nlp.segmentation.CallStanfordSegmentor;

public class NamedEntityRecognition extends JPanel{

	/** 打开测试文件按钮 */
	JButton inputTextButton;
	JPanel topPanelTop;
	JFrame openFileFrame;
	JFileChooser fileChooser;

	JTextArea inputText;
	JScrollPane inputTextPane;

	JButton OutNERButton;
	JPanel bottomPanelTop;
	JTextArea OutNER;
	JScrollPane OutNERPane;
	Box top;

	String rawFile;

	public NamedEntityRecognition(final String localDir){
		inputTextButton = new JButton("打开文本");
		topPanelTop = new JPanel();
		topPanelTop.add(inputTextButton);

		openFileFrame = new JFrame("Opening raw File");
		fileChooser = new JFileChooser("/home/tm/disk/d1/nerc/clean");
		inputTextButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int result = fileChooser.showDialog(openFileFrame , "open template file");
				if(result == JFileChooser.APPROVE_OPTION)
				{
					rawFile = fileChooser.getSelectedFile().getPath();
					StringBuffer outBuffer = new StringBuffer();
					try{
						File inFile = new File(rawFile);
						BufferedReader reader = new BufferedReader(new FileReader(inFile));
						String line = null;
						while((line = reader.readLine()) != null){
							outBuffer.append(line+"\n");
						}
						reader.close();
					}
					catch(IOException e){
						e.printStackTrace();
					}
					inputText.setText(outBuffer.toString());
				}
			}
		});


		inputText = new JTextArea(10,20);
		inputText.setLineWrap(true);
		inputText.setWrapStyleWord(true);
		inputTextPane = new JScrollPane(inputText);


		OutNERButton = new JButton("命名实体识别");
		bottomPanelTop = new JPanel();
		bottomPanelTop.add(OutNERButton);

		OutNERButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String segFileName = localDir + "result/1-seg";
				CallStanfordSegmentor.segment("pku", rawFile, "UTF-8", segFileName, 0);
				String charFileName = localDir+"result/1-char";
				NERTrain.testWord2CharacterWithoutPOS(segFileName, charFileName);
				String resultFileName = localDir+"result/1-result";
				String finalFileName = localDir+"result/1-final";
				NERTrain.callChineseNER(charFileName, resultFileName, finalFileName);
				String senFileName = localDir + "result/1-sen";
				String nerFileName = localDir + "result/1-ner";
				String finalSentence = NERInstance.character2Sentence(finalFileName, senFileName, nerFileName, "4");
				OutNER.setText(finalSentence);
			}
		});


		OutNER = new JTextArea(20,30);
		OutNER.setLineWrap(true);
		OutNER.setWrapStyleWord(true);
		OutNERPane = new JScrollPane(OutNER);



		top = Box.createVerticalBox();
		top.add(topPanelTop);
		top.add(inputTextPane);
		top.add(bottomPanelTop);
		top.add(OutNERPane);


		this.setLayout(new BorderLayout());
		this.add(top, BorderLayout.NORTH);
	}







	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub




	}

}
