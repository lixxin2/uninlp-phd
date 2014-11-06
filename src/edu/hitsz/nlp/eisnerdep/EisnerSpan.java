/**
 *
 */
package edu.hitsz.nlp.eisnerdep;

import java.util.ArrayList;




/**
 * Eisner's papaer in 2000: Bilexical Grammars and their cubic-time parsing algorithms
 * Span�Ķ��塢�������ϲ������ӵ�
 * @author lee
 */
public class EisnerSpan implements Cloneable{

	public double weight;
	public int simple;
	public int left;
	public int right;
	//��ڵ�����ӽڵ�״̬�����==left����ʾû�����ӽڵ�
	public int leftQ;
	//�ҽڵ�����ӽڵ�״̬�����==right����ʾû�����ӽڵ�
	public int rightQ;
	//��ڵ��Ƿ��и��ڵ��ڣ�left��right��
	public int leftB;
	//�ҽڵ��Ƿ��и��ڵ��ڣ�left��right��
	public int rightB;
	public ArrayList<Integer> head;
	public StatMap nmap;
	//value of constant used for computation of lambda
	public final int cons=5;
	public final double weightThres=-1000;

	/*
	public EisnerSpan(ConllSentence sen,StatMap map){
		head=new ArrayList<Integer>();
		for(int i=0;i<sen.sentenceLength+1;i++)
			head.add(i);
		nmap=new StatMap();
		nmap=map;
		weight=0;
	}
	*/

	public EisnerSpan(Conll09Sentence sen){
		head=new ArrayList<Integer>();
		weight=0;
	}


	/**
	 * ��ʼ��span[i��i+1]
	 * @param sen
	 * @param i
	 * @return
	 */
	public EisnerSpan seed(Conll09Sentence sen, int i){
		EisnerSpan spanN=new EisnerSpan(sen);
		spanN.simple=1;
		spanN.left=i;
		spanN.right=i+1;
		//�ӽڵ�
		spanN.leftQ=spanN.left;
		spanN.rightQ=spanN.right;
		//�Ƿ��и��ڵ�
		spanN.leftB=0;
		spanN.rightB=0;
		spanN.weight=0;
		spanN.head.add(i);
		spanN.head.add(i+1);
		return spanN;
	}


	/**
	 * ����spanA��spanB�����ز����spanN������޷���ϣ�����null
	 * @param sen
	 * @param spanA
	 * @param spanB
	 * @return
	 */
	public EisnerSpan combine(Conll09Sentence sen,EisnerSpan spanA,EisnerSpan spanB,StatMap nmap){
		if(spanA.simple!=1||spanA.weight<weightThres||spanB.weight<weightThres){
			//System.out.println("simple is 0");
			return null;
		}
		EisnerSpan spanN=new EisnerSpan(sen);
		//����м�ڵ���������ӽڵ㶼����Span�������������ӽڵ㶼����Span�����Ҹ��ڵ�������Span�е�һ��
		if((spanA.rightQ<=spanA.right)&&(spanB.leftQ>=spanB.left)&&
				((spanA.rightB^spanB.leftB)==1)){
			spanN.left=spanA.left;
			spanN.right=spanB.right;
			//����spanN��weight
			double tempWeight=spanA.weight+spanB.weight;
			double weightLeftStop,weightRightStop;
			if(spanA.rightQ<spanA.right){
				weightLeftStop=Math.log10(compWeightLeftStop(sen,spanA.right,spanA.rightQ,nmap));
				tempWeight+=weightLeftStop;
			}
			if(spanB.leftQ>spanB.left){
				weightRightStop=Math.log10(compWeightLeftStop(sen,spanA.right,spanA.rightQ,nmap));
				tempWeight+=weightRightStop;
			}
			spanN.weight=tempWeight;
			//���������
			spanN.leftQ=spanA.leftQ;
			spanN.rightQ=spanB.rightQ;
			spanN.leftB=spanA.leftB;
			spanN.rightB=spanB.rightB;
			spanN.simple=0;
			//����spanN��head
			for(int i=0;i<(spanA.head.size()-1);i++){
				spanN.head.add(spanA.head.get(i));
			}
			if(spanA.rightB==1)
				spanN.head.add(spanA.head.get(spanA.head.size()-1));
			else if((spanB.leftB==1))
				spanN.head.add(spanB.head.get(0));
			for(int i=1;i<spanB.head.size();i++){
				spanN.head.add(spanB.head.get(i));
			}
			if(spanN.weight<weightThres||!spanN.isSpan()){
				//System.out.println("weight<weightThres or it's not a span");
				return null;
			}
			return spanN;
		}
		else{
			//System.out.println("don't suit for combine");
			return null;
		}

	}

	/**
	 * �ж��Ƿ���Բ������ҽڵ�������
	 * @param sen
	 * @param span
	 * @return
	 */
	public EisnerSpan noLink(Conll09Sentence sen){
		EisnerSpan spanN=new EisnerSpan(sen);
		spanN.simple=simple;
		spanN.left=left;
		spanN.right=right;
		spanN.leftQ=leftQ;
		spanN.rightQ=rightQ;
		spanN.leftB=leftB;
		spanN.rightB=rightB;
		spanN.weight=weight;
		for(int i=0;i<head.size();i++){
			spanN.head.add(i, head.get(i));
		}
		if(spanN.isTwoHead(sen)||!spanN.isWholeSentence(sen))
			return null;
		return spanN;
	}

	/**
	 * �ж��Ƿ�������������ӣ�����ڵ����ҽڵ�ĸ��ڵ�
	 * @param sen
	 * @param span
	 * @return
	 */
	public EisnerSpan linkLeft(Conll09Sentence sen,StatMap nmap) {
		if(leftB==1||rightB==1||right==sen.sentenceLength)
			return null;
		EisnerSpan spanN=new EisnerSpan(sen);
		spanN.simple=1;
		spanN.left=left;
		spanN.right=right;
		spanN.weight=weight+Math.log10(compWeightRight(sen,nmap));
		if(spanN.weight<weightThres)
			return null;
		spanN.leftQ=right;
		spanN.rightQ=rightQ;
		spanN.leftB=leftB;
		spanN.rightB=1;
		for(int i=0;i<head.size();i++)
			spanN.head.add(i, head.get(i));
		spanN.head.set(head.size()-1,left);
		if(spanN.isTwoHead(sen)||!spanN.isWholeSentence(sen))
			return null;
		return spanN;
	}

	/**
	 * �ж��Ƿ�������������ӣ����ҽڵ�����ڵ�ĸ��ڵ�
	 * @param sen
	 * @param span
	 * @return
	 */
	public EisnerSpan linkRight(Conll09Sentence sen,StatMap nmap) {
		if(leftB==1||rightB==1)
			return null;
		EisnerSpan spanN=new EisnerSpan(sen);
		spanN.simple=1;
		spanN.left=left;
		spanN.right=right;
		spanN.weight=weight+Math.log10(compWeightLeft(sen,nmap));
		if(spanN.weight<weightThres)
			return null;
		spanN.leftQ=leftQ;
		spanN.rightQ=left;
		spanN.leftB=1;
		spanN.rightB=rightB;
		for(int i=0;i<head.size();i++)
			spanN.head.add(head.get(i));
		spanN.head.set(0,right);
		if(spanN.isTwoHead(sen)||!spanN.isWholeSentence(sen))
			return null;
		return spanN;
	}

	public EisnerSpan linkInf(Conll09Sentence sen) {
		EisnerSpan spanN=new EisnerSpan(sen);
		spanN.weight=weightThres-1;
		return spanN;
	}


	/**
	 * �жϵ�ǰ[left,right]�Ƿ�Ϊһ��span
	 * @return
	 */
	public boolean isSpan()
	{
		if(right==left+1)
			return true;
		for(int i=1;i<head.size()-1;i++){
			if(head.get(i)<left||head.get(i)>right||head.get(i)==left+i)
				return false;
		}
		return true;
	}

	/**
	 * �жϵ�ǰ[left,right]�Ƿ�Ϊһ��simple span
	 * @return
	 */
	public boolean isSimple()
	{
		return((right==left+1)||(head.get(0)==right)||(head.get(head.size()-1)==left));
	}

	/**
	 * ����ҽڵ�ΪROOT�����������ڵ�ָ��ROOT�ڵ㣬�򷵻�False
	 * @param sen
	 * @return
	 */
	public boolean isTwoHead(Conll09Sentence sen)
	{
		if(sen.pos[right]=="ROOT"){
			int headNum=0;
			for(int i=0;i<head.size()-1;i++){
				if(head.get(i)==right)
					headNum++;
				if(headNum>1)
					return true;
			}
			return false;
		}
		else
			return false;
	}

	/**
	 * �жϵ��ﵽ���������ʱ���Ƿ�ÿ���ʶ��и��׽ڵ㡣���У�����true��û�У�����false
	 * @param sen
	 * @return
	 */
	public boolean isWholeSentence(Conll09Sentence sen)
	{
		if(left==0&&right==sen.sentenceLength){
			int headRoot=0;
			for(int i=0;i<sen.sentenceLength;i++){
				if(head.get(i)==i)
					return false;
				if(head.get(i)==sen.sentenceLength)
					headRoot++;
			}
			if(headRoot!=1)
				return false;
			return true;
		}
		else
			return true;
	}

	/**
	 * computer the probability of adding a Stop node to the leftmost of the father node
	 * ����ϲ����м�ڵ������Stop�ĸ���
	 * @param sen
	 * @param father
	 * @param leftson
	 * @return
	 */
	public double compWeightLeftStop(Conll09Sentence sen,int father,int leftson,StatMap nmap){
		if(father==leftson)
			return 1;
		String curTagL=sen.pos[father]+"-LEFT";
		String curWord=sen.form[father]+"+"+curTagL;
		String leftStop="LEFTSTOP";
		String preTag=sen.pos[leftson];
		String preTagcurWord=preTag+"+"+curWord;
		String leftStopPreTagcurWord=leftStop+"+"+preTagcurWord;
		String preTagCurTag=preTag+"+"+curTagL;
		String leftStopPreTagCurTag=leftStop+"+"+preTagCurTag;
		double preTagCurWordNum=nmap.getParentNum(preTagcurWord);
		double preTagCurwordFreq=nmap.getParentDiv("tl,w,t-l");
		double leftStopPreTagcurWordNum=nmap.getSonNum(leftStopPreTagcurWord);
		double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
		double leftStopPreTagCurTagNum=nmap.getSonNum(leftStopPreTagCurTag);
		double lambda1;
		double tempWeight;
		if (preTagCurWordNum==0)
			tempWeight=leftStopPreTagCurTagNum/preTagCurTagNum;
		else{
			lambda1=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurwordFreq);
			tempWeight=lambda1*leftStopPreTagcurWordNum/preTagCurWordNum+(1-lambda1)*leftStopPreTagCurTagNum/preTagCurTagNum;
		}
		return tempWeight;
	}

	/**
	 * computer the probability of adding a Stop node to the rightmost of the father node
	 * @param sen
	 * @param father
	 * @param leftson
	 * @return
	 */
	public double compWeightRightStop(Conll09Sentence sen,int father,int rightson, StatMap nmap){
		if(father==rightson)
			return 1;
		String curTagR=sen.pos[father]+"-RIGHT";
		String curWord=sen.form[father]+"+"+curTagR;
		String rightStop="RIGHTSTOP";
		String preTag=sen.pos[rightson];
		String preTagcurWord=preTag+"+"+curWord;
		String rightStopPreTagcurWord=rightStop+"+"+preTagcurWord;
		String preTagCurTag=preTag+"+"+curTagR;
		String rightStopPreTagCurTag=rightStop+"+"+preTagCurTag;
		double preTagCurWordNum=nmap.getParentNum(preTagcurWord);
		double preTagCurwordFreq=nmap.getParentDiv("tr,w,t-r");
		double rightStopPreTagcurWordNum=nmap.getSonNum(rightStopPreTagcurWord);
		double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
		double rightStopPreTagCurTagNum=nmap.getSonNum(rightStopPreTagCurTag);
		double lambda1;
		double tempWeight;
		if (preTagCurWordNum==0)
			tempWeight=rightStopPreTagCurTagNum/preTagCurTagNum;
		else{
			lambda1=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurwordFreq);
			tempWeight=lambda1*rightStopPreTagcurWordNum/preTagCurWordNum+(1-lambda1)*rightStopPreTagCurTagNum/preTagCurTagNum;
		}
		return tempWeight;
	}

	/**
	 * computer the probability of adding a right son node on leftmost node
	 * ����������ڵ�������ӽڵ�ĸ���
	 * @param sen
	 * @return
	 */
	public double compWeightRight(Conll09Sentence sen, StatMap nmap){
		//if the leftmost node doesn't have any son in its right
		String preTag = null;
		String curTagR=sen.pos[left]+"-RIGHT";
		String curWord=sen.form[left]+"+"+curTagR;
		//���ԭ��û�����ӽڵ�
		if(leftQ==left){
			//����P(RightStart|w,t-r)
			String rightStart="RIGHTSTART";
			String rightStartCurWord=rightStart+"+"+curWord;
			String rightStartCurTag=rightStart+"+"+curTagR;
			double curWordNum=nmap.getParentNum(curWord);
			double curWordFreq=nmap.getParentDiv("w,t-r");
			double rightStartCurWordNum=nmap.getSonNum(rightStartCurWord);
			double curTagRNum=nmap.getParentNum(curTagR);
			double rightStartCurTagNum=nmap.getSonNum(rightStartCurTag);
			double lambda1;
			double weightStart;
			if (curWordNum==0)
				weightStart=rightStartCurTagNum/curTagRNum;
			else{
				lambda1=curWordNum/(curWordNum+cons*curWordFreq);
				weightStart=lambda1*rightStartCurWordNum/curWordNum+(1-lambda1)*rightStartCurTagNum/curTagRNum;
			}
			preTag=rightStart;
			//����P(tr|RightStart,w,t-r)
			String preTagCurTag=preTag+"+"+curTagR;
			String preTagCurWord=preTag+"+"+curWord;
			String rightTag=sen.pos[right];
			String rightForm=sen.form[right];
			String rightTagPreTagCurWord=rightTag+"+"+preTagCurWord;
			String rightTagPreTagCurTag=rightTag+"+"+preTagCurTag;
			String rightWordPreTagCurWord=rightForm+"+"+rightTagPreTagCurWord;
			String rightWordPreTagCurTag=rightForm+"+"+rightTagPreTagCurTag;
			String rightWord=rightForm+"+"+rightTag;
			double preTagCurWordNum=nmap.getParentNum(preTagCurWord);
			double preTagCurWordFreq=nmap.getParentDiv("RIGHTSTART,w,t-r");
			double rightTagPreTagCurWordNum=nmap.getSonNum(rightTagPreTagCurWord);
			double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
			double rightTagPreTagCurTagNum=nmap.getSonNum(rightTagPreTagCurTag);
			double lambda2;
			double weightRightPos;
			if(preTagCurWordNum==0)
				weightRightPos=rightTagPreTagCurTagNum/preTagCurTagNum;
			else{
				lambda2=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurWordFreq);
				weightRightPos=lambda2*rightTagPreTagCurWordNum/preTagCurWordNum+
				(1-lambda2)*rightTagPreTagCurTagNum/preTagCurTagNum;
			}
			//����P(wr|tr,RightStart,w,t-r))
			rightTagPreTagCurWordNum=nmap.getParentNum(rightTagPreTagCurWord);
			double rightTagPreTagCurWordFreq=nmap.getParentDiv("tr,RIGHTSTART,w,t-r");
			double rightWordPreTagCurWordNum=nmap.getSonNum(rightWordPreTagCurWord);
			rightTagPreTagCurTagNum=nmap.getParentNum(rightTagPreTagCurTag);
			double rightTagPreTagCurTagFreq=nmap.getParentDiv("tr,RIGHTSTART,t-r");
			double rightWordPreTagCurTagNum=nmap.getSonNum(rightWordPreTagCurTag);
			double rightTagNum=nmap.getParentNum(rightTag);
			double rightWordNum=nmap.getSonNum(rightWord);
			double lambda3,lambda4;
			double weightRightForm;
			if(rightTagPreTagCurWordNum==0)
				if(rightTagPreTagCurTagNum==0)
					weightRightForm=rightWordNum/rightTagNum;
				else{
					lambda4=rightTagPreTagCurTagNum/(rightTagPreTagCurTagNum+cons*rightTagPreTagCurTagFreq);
					weightRightForm=lambda4*rightWordPreTagCurTagNum/rightTagPreTagCurTagNum+(1-lambda4)*rightWordNum/rightTagNum;
				}
			else{
				lambda3=rightTagPreTagCurWordNum/(rightTagPreTagCurWordNum+cons*rightTagPreTagCurWordFreq);
				lambda4=rightTagPreTagCurTagNum/(rightTagPreTagCurTagNum+cons*rightTagPreTagCurTagFreq);
				weightRightForm=lambda3*rightWordPreTagCurWordNum/rightTagPreTagCurWordNum
				+(1-lambda3)*(lambda4*rightWordPreTagCurTagNum/rightTagPreTagCurTagNum+(1-lambda4)*rightWordNum/rightTagNum);
			}
			double tempWeight=weightStart*weightRightPos*weightRightForm;
			return tempWeight;
		}
		else if(leftQ>left){
			preTag=sen.pos[leftQ];
			String preTagCurTag=preTag+"+"+curTagR;
			String preTagCurWord=preTag+"+"+curWord;
			String rightTag=sen.pos[right];
			String rightForm=sen.form[right];
			String rightTagPreTagCurWord=rightTag+"+"+preTagCurWord;
			String rightTagPreTagCurTag=rightTag+"+"+preTagCurTag;
			String rightWordPreTagCurWord=rightForm+"+"+rightTagPreTagCurWord;
			String rightWordPreTagCurTag=rightForm+"+"+rightTagPreTagCurTag;
			String rightWord=rightForm+"+"+rightTag;
			double preTagCurWordNum=nmap.getParentNum(preTagCurWord);
			double preTagCurWordFreq=nmap.getParentDiv("tr,w,t-r");
			double rightTagPreTagCurWordNum=nmap.getSonNum(rightTagPreTagCurWord);
			double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
			double rightTagPreTagCurTagNum=nmap.getSonNum(rightTagPreTagCurTag);
			double lambda1;
			double weightRightPos;
			if(preTagCurWordNum==0)
				weightRightPos=rightTagPreTagCurTagNum/preTagCurTagNum;
			else{
				lambda1=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurWordFreq);
				weightRightPos=lambda1*rightTagPreTagCurWordNum/preTagCurWordNum+
				(1-lambda1)*rightTagPreTagCurTagNum/preTagCurTagNum;
			}
			rightTagPreTagCurWordNum=nmap.getParentNum(rightTagPreTagCurWord);
			double rightTagPreTagCurWordFreq=nmap.getParentDiv("tr,tr,w,t-r");
			double rightWordPreTagCurWordNum=nmap.getSonNum(rightWordPreTagCurWord);
			rightTagPreTagCurTagNum=nmap.getParentNum(rightTagPreTagCurTag);
			double rightTagPreTagCurTagFreq=nmap.getParentDiv("tr,tr,t-r");
			double rightWordPreTagCurTagNum=nmap.getSonNum(rightWordPreTagCurTag);
			double rightTagNum=nmap.getParentNum(rightTag);
			double rightWordNum=nmap.getSonNum(rightWord);
			double weightRightForm;
			double lambda2;
			double lambda3;
			if(rightTagPreTagCurWordNum==0){
				if(rightTagPreTagCurTagNum==0)
					weightRightForm=rightWordNum/rightTagNum;
				else{
					lambda3=rightTagPreTagCurTagNum/(rightTagPreTagCurTagNum+cons*rightTagPreTagCurTagFreq);
					weightRightForm=lambda3*rightWordPreTagCurTagNum/rightTagPreTagCurTagNum+
							(1-lambda3)*rightWordNum/rightTagNum;
				}
			}
			else{
				lambda2=rightTagPreTagCurWordNum/(rightTagPreTagCurWordNum+cons*rightTagPreTagCurWordFreq);
				lambda3=rightTagPreTagCurTagNum/(rightTagPreTagCurTagNum+cons*rightTagPreTagCurTagFreq);
				weightRightForm=lambda2*rightWordPreTagCurWordNum/rightTagPreTagCurWordNum+
				(1-lambda2)*(lambda3*rightWordPreTagCurTagNum/rightTagPreTagCurTagNum+
						(1-lambda3)*rightWordNum/rightTagNum);
			}
			double tempWeight=weightRightPos*weightRightForm;
			return tempWeight;
		}
		return 1;

	}

	/**
	 * computer the probability of adding a right son node on leftmost node
	 * �������һ�����ӽڵ�ĸ���
	 * @param sen
	 * @return
	 */
	public double compWeightLeft(Conll09Sentence sen, StatMap nmap){
		if(sen.form[right]=="ROOT"){
			String rootForm="ROOT";
			String curTag=sen.pos[left];
			String curForm=sen.form[left];
			String curWord=curForm+"+"+curTag;
			String curTagRootForm=curTag+"+"+rootForm;
			String curWordRootForm=curWord+"+"+rootForm;
			//�洢P(t|Root)��tempSonMap,tmpParentMap,sonParentHash
			double rootFormNum=nmap.getParentNum("ROOT");
			double curTagRootFormNum=nmap.getSonNum(curTagRootForm);
			double weightPos=curTagRootFormNum/rootFormNum;
			//�洢P(w|t,Root)��tempSonMap,tmpParentMap,sonParentHash
			curTagRootFormNum=nmap.getParentNum(curTagRootForm);
			double curTagRootFormFreq=nmap.getParentDiv("t-l,ROOT");
			double curWordRootFormNum=nmap.getSonNum(curWordRootForm);
			double curWordNum=nmap.getSonNum(curWord);
			double curTagNum=nmap.getParentNum(curTag);
			double weightForm;
			double lambda;
			if(curTagRootFormNum==0)
				weightForm=curWordNum/curTagNum;
			else{
				lambda=curTagRootFormNum/(curTagRootFormNum+cons*curTagRootFormFreq);
				weightForm=lambda*curWordRootFormNum/curTagRootFormNum
				+(1-lambda)*curWordNum/curTagNum;
			}
			double tempWeight=weightPos*weightForm;
			return tempWeight;
		}
		String preTag = null;
		String curTagL=sen.pos[right]+"-LEFT";
		String curWord=sen.form[right]+"+"+curTagL;
		//���ԭ�����ҽڵ�û�����ӽڵ�
		//if the leftmost node doesn't have any son in its right
		if(rightQ==right){
			String leftStart="LEFTSTART";
			String leftStartCurWord=leftStart+"+"+curWord;
			String leftStartCurTag=leftStart+"+"+curTagL;
			double curWordNum=nmap.getParentNum(curWord);
			double curWordFreq=nmap.getParentDiv("w,t-l");
			double leftStartCurWordNum=nmap.getSonNum(leftStartCurWord);
			double curTagRNum=nmap.getParentNum(curTagL);
			double leftStartCurTagNum=nmap.getSonNum(leftStartCurTag);
			double weightStart;
			double lambda1;
			if (curWordNum==0)
				weightStart=leftStartCurTagNum/curTagRNum;
			else{
				lambda1=curWordNum/(curWordNum+cons*curWordFreq);
				weightStart=lambda1*leftStartCurWordNum/curWordNum+(1-lambda1)*leftStartCurTagNum/curTagRNum;
			}
			preTag=leftStart;
			String preTagCurTag=preTag+"+"+curTagL;
			String preTagCurWord=preTag+"+"+curWord;
			String leftTag=sen.pos[left];
			String leftForm=sen.form[left];
			String leftTagPreTagCurWord=leftTag+"+"+preTagCurWord;
			String leftTagPreTagCurTag=leftTag+"+"+preTagCurTag;
			String leftWordPreTagCurWord=leftForm+"+"+leftTagPreTagCurWord;
			String leftWordPreTagCurTag=leftForm+"+"+leftTagPreTagCurTag;
			String leftWord=leftForm+"+"+leftTag;
			double preTagCurWordNum=nmap.getParentNum(preTagCurWord);
			double preTagCurWordFreq=nmap.getParentDiv("LEFTSTART,w,t-l");
			double leftTagPreTagCurWordNum=nmap.getSonNum(leftTagPreTagCurWord);
			double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
			double leftTagPreTagCurTagNum=nmap.getSonNum(leftTagPreTagCurTag);
			double weightLeftPos;
			double lambda2;
			if(preTagCurWordNum==0)
				weightLeftPos=leftTagPreTagCurTagNum/preTagCurTagNum;
			else{
				lambda2=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurWordFreq);
				weightLeftPos=lambda2*leftTagPreTagCurWordNum/preTagCurWordNum+
				(1-lambda2)*leftTagPreTagCurTagNum/preTagCurTagNum;
			}
			leftTagPreTagCurWordNum=nmap.getParentNum(leftTagPreTagCurWord);
			double leftTagPreTagCurWordFreq=nmap.getParentDiv("tl,LEFTSTART,w,t-l");
			double leftWordPreTagCurWordNum=nmap.getSonNum(leftWordPreTagCurWord);
			leftTagPreTagCurTagNum=nmap.getParentNum(leftTagPreTagCurTag);
			double leftTagPreTagCurTagFreq=nmap.getParentDiv("tl,LEFTSTART,t-l");
			double leftWordPreTagCurTagNum=nmap.getSonNum(leftWordPreTagCurTag);
			double leftTagNum=nmap.getParentNum(leftTag);
			double leftWordNum=nmap.getSonNum(leftWord);
			double weightLeftForm;
			double lambda3,lambda4;
			if(leftTagPreTagCurWordNum==0){
				if(leftTagPreTagCurTagNum==0)
					weightLeftForm=leftWordNum/leftTagNum;
				else{
					lambda4=leftTagPreTagCurTagNum/(leftTagPreTagCurTagNum+cons*leftTagPreTagCurTagFreq);
					weightLeftForm=lambda4*leftWordPreTagCurTagNum/leftTagPreTagCurTagNum+
								(1-lambda4)*leftWordNum/leftTagNum;
				}
			}
			else{
				lambda3=leftTagPreTagCurWordNum/(leftTagPreTagCurWordNum+cons*leftTagPreTagCurWordFreq);
				lambda4=leftTagPreTagCurTagNum/(leftTagPreTagCurTagNum+cons*leftTagPreTagCurTagFreq);
				weightLeftForm=lambda3*leftWordPreTagCurWordNum/leftTagPreTagCurWordNum+
					(1-lambda3)*(lambda4*leftWordPreTagCurTagNum/leftTagPreTagCurTagNum+
							(1-lambda4)*leftWordNum/leftTagNum);
			}
			double tempWeight=weightStart*weightLeftPos*weightLeftForm;
			return tempWeight;
		}
		else if(rightQ<right){
			preTag=sen.pos[rightQ];
			String preTagCurTag=preTag+"+"+curTagL;
			String preTagCurWord=preTag+"+"+curWord;
			String leftTag=sen.pos[left];
			String leftForm=sen.form[left];
			String leftTagPreTagCurWord=leftTag+"+"+preTagCurWord;
			String leftTagPreTagCurTag=leftTag+"+"+preTagCurTag;
			String leftWordPreTagCurWord=leftForm+"+"+leftTagPreTagCurWord;
			String leftWordPreTagCurTag=leftForm+"+"+leftTagPreTagCurTag;
			String leftWord=leftForm+"+"+leftTag;
			double preTagCurWordNum=nmap.getParentNum(preTagCurWord);
			double preTagCurWordFreq=nmap.getParentDiv("tl,w,t-l");
			double leftTagPreTagCurWordNum=nmap.getSonNum(leftTagPreTagCurWord);
			double preTagCurTagNum=nmap.getParentNum(preTagCurTag);
			double leftTagPreTagCurTagNum=nmap.getSonNum(leftTagPreTagCurTag);
			double lambda1;
			double weightLeftPos;
			if(preTagCurWordNum==0)
				weightLeftPos=leftTagPreTagCurTagNum/preTagCurTagNum;
			else{
				lambda1=preTagCurWordNum/(preTagCurWordNum+cons*preTagCurWordFreq);
				weightLeftPos=lambda1*leftTagPreTagCurWordNum/preTagCurWordNum+
				(1-lambda1)*leftTagPreTagCurTagNum/preTagCurTagNum;
			}
			leftTagPreTagCurWordNum=nmap.getParentNum(leftTagPreTagCurWord);
			double leftTagPreTagCurWordFreq=nmap.getParentDiv("tl,tl,w,t-l");
			double leftWordPreTagCurWordNum=nmap.getSonNum(leftWordPreTagCurWord);
			leftTagPreTagCurTagNum=nmap.getParentNum(leftTagPreTagCurTag);
			double leftTagPreTagCurTagFreq=nmap.getParentDiv("tl,tl,t-l");
			double leftWordPreTagCurTagNum=nmap.getSonNum(leftWordPreTagCurTag);
			double leftTagNum=nmap.getParentNum(leftTag);
			double leftWordNum=nmap.getSonNum(leftWord);
			double lambda2;
			double lambda3;
			double weightLeftForm;
			if(leftTagPreTagCurWordNum==0)
				if(leftTagPreTagCurTagNum==0)
					weightLeftForm=leftWordNum/leftTagNum;
				else{
					lambda3=leftTagPreTagCurTagNum/(leftTagPreTagCurTagNum+cons*leftTagPreTagCurTagFreq);
					weightLeftForm=lambda3*leftWordPreTagCurTagNum/leftTagPreTagCurTagNum+
								(1-lambda3)*leftWordNum/leftTagNum;
				}
			else{
				lambda2=leftTagPreTagCurWordNum/(leftTagPreTagCurWordNum+cons*leftTagPreTagCurWordFreq);
				lambda3=leftTagPreTagCurTagNum/(leftTagPreTagCurTagNum+cons*leftTagPreTagCurTagFreq);
				weightLeftForm=lambda2*leftWordPreTagCurWordNum/leftTagPreTagCurWordNum+
				(1-lambda2)*(lambda3*leftWordPreTagCurTagNum/leftTagPreTagCurTagNum+
						(1-lambda3)*leftWordNum/leftTagNum);
			}
			double tempWeight=weightLeftPos*weightLeftForm;
			return tempWeight;
		}
		return 1;
	}

	public void outputSpan(Conll09Sentence sen){
		if(this!=null){
			System.out.println("weight "+weight+","+"left "+left+","+" right"+right
					+","+"leftQ "+leftQ+","+"rightQ "+rightQ+","+"leftB "+leftB
					+","+"rightB "+rightB+","+"simple "+simple+"\nHead of the span:");
			for(int i=0;i<head.size();i++)
				System.out.print(head.get(i)+"\t");
			System.out.println();
		}
		else
			System.out.println("EisnerSpan is emtpy!");

	}



	public Object clone() throws CloneNotSupportedException {
		EisnerSpan o=null;
		try{
			o = (EisnerSpan)super.clone();
			o.head=(ArrayList)head.clone();
			}
		catch(CloneNotSupportedException e){
			e.printStackTrace();
			}
		return o;
		}

}
