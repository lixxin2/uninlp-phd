package edu.hitsz.gui.sequence;

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
	Preprocess myPreprocess = new Preprocess();;
	ErrorAnalysis myErrorAnalysis = new ErrorAnalysis();
	
	
	public GUIStarter(){
		myJFrame = new JFrame("UniML - Unified Machine Learning");
		myTabbedPane = new JTabbedPane(JTabbedPane.TOP , JTabbedPane.WRAP_TAB_LAYOUT);	
		myTabbedPane.addTab("Preprocess", myPreprocess);
		myTabbedPane.addTab("Error Analysis", myErrorAnalysis);
		
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
