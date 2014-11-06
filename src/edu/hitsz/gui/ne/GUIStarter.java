package edu.hitsz.gui.ne;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;


public class GUIStarter {

	/** the whole frame*/
	JFrame myJFrame;
	/** Tabbled Pane */
	JTabbedPane myTabbedPane;
	/** myPreprocess class */
	String localDir = "/home/tm/disk/d1/nerc/";
	//MyWebCrawler myWebCrawler = new MyWebCrawler(localDir);
	NamedEntityRecognition myNER = new NamedEntityRecognition(localDir);
	NewWordAndHotWord myNewWordAndHotWord = new NewWordAndHotWord(localDir);
	ErrorAnalysis myErrorAnalysis = new ErrorAnalysis();


	public GUIStarter(){
		myJFrame = new JFrame("面向网络的中文命名实体识别系统");
		myTabbedPane = new JTabbedPane(JTabbedPane.TOP , JTabbedPane.WRAP_TAB_LAYOUT);
		myTabbedPane.addTab("网页获取和清理", null);
		myTabbedPane.addTab("命名实体识别", myNER);
		myTabbedPane.addTab("新词和热点词识别", myNewWordAndHotWord);
		myTabbedPane.addTab("错误分析", myErrorAnalysis);

		myJFrame.add(myTabbedPane, BorderLayout.CENTER);


		myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myJFrame.pack();
		myJFrame.setVisible(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GUIStarter();
	}

}
