/**
 *
 */
package edu.hitsz.nlp.eisnerdep;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;


/**
 * CoNLL 2009 shared task file format
 * @author tm
 * This is Conll2009 Shared Task's file format:
 * 		1	2	 3		4	  5   6    7     8     9   10	 11		12		13		15	  16
 * 		ID	FORM LEMMA LEMMA POS PPOS FEAT PFEAT HEAD PHEAD DEPREL PDEPREL FILLPRED PRED ARG+
 *
 * 1	The	the	the	DT	DT	_	_	2	2	NMOD	NMOD	_	_	_	_	_	_
 * 2	economy	economy	economy	NN	NN	_	_	4	4	NMOD	NMOD	_	_	A1	_	_	_
 * 		***	*** ***
 * 25	.	.	.	.	.	_	_	5	5	P	P	_	_	_	_	_	_
 *
 * 1	The	the	the	DT	DT	_	_	4	4	NMOD	NMOD	_	_	_	_
 *
 */

public class Conll09Sentence implements Cloneable{

	public int sentenceLength;
	//String�����С���������Բ�ͬ��仯��English=200��Chinese=250��
	public final int senTotalLen=250;
	public String[] form=new String[senTotalLen];
	public String[] lemma=new String[senTotalLen];
	public String[] plemma=new String[senTotalLen];
	public String[] pos=new String[senTotalLen];
	public String[] ppos=new String[senTotalLen];
	//String[] feat=new String[senTotalLen];
	//String[] pfeat=new String[senTotalLen];
	public String[] head=new String[senTotalLen];
	public String[] phead=new String[senTotalLen];
	public String[] deprel=new String[senTotalLen];
	public String[] pdeprel=new String[senTotalLen];
	public String[] predicate=new String[senTotalLen];
	public String[] semantic=new String[senTotalLen];

	public Conll09Sentence(){
		sentenceLength=0;
	}

	/**
	 * �������ѵ���ĸ�ʽ
	 * @param tempSentence ���ݹ�����vector��ÿ��vector��ԱΪһ���ַ��У��������ʼ����Ա�ǵȵ�
	 * @param len ���ݹ�����vector�Ĵ�С
	 */
	public void processTrain(Vector<String> tempSentence,int len){
		sentenceLength=len;
		for(int i=0;i<sentenceLength;i++){
			String[] word=tempSentence.get(i).split("\t");
			form[i]=word[1];
			lemma[i]=word[2];
			plemma[i]=word[3];
			pos[i]=word[4];
			ppos[i]=word[5];
			head[i]=word[8];
			phead[i]=word[9];
			deprel[i]=word[10];
			pdeprel[i]=word[11];
		}
		form[sentenceLength]="ROOT";
		lemma[sentenceLength]="ROOT";
		plemma[sentenceLength]="ROOT";
		pos[sentenceLength]="ROOT";
		ppos[sentenceLength]="ROOT";
	}

	/**
	 * ������ɲ��Եĸ�ʽ
	 * @param tempSentence ���ݹ�����vector��ÿ��vector��ԱΪһ���ַ��У��������ʼ����Ա�ǵȵ�
	 * @param len ���ݹ�����vector�Ĵ�С
	 */
	public void processTest(Vector<String> tempSentence,int len){
		sentenceLength=len;
		for(int i=0;i<sentenceLength;i++){
			String[] word=tempSentence.get(i).split("\t");
			form[i]=word[1];
			lemma[i]=word[2];
			plemma[i]=word[3];
			pos[i]=word[4];
			ppos[i]=word[5];
			head[i]=null;
			phead[i]=null;
			deprel[i]=null;
			pdeprel[i]=null;
		}
		form[sentenceLength]="ROOT";
		lemma[sentenceLength]="ROOT";
		plemma[sentenceLength]="ROOT";
		pos[sentenceLength]="ROOT";
		ppos[sentenceLength]="ROOT";

	}

	/**
	 * �жϵ�һ�������ʾ�Ľڵ��Ƿ��ǵڶ��������ʾ�Ľڵ�ĸ���
	 * @param father ���׽ڵ��λ��
	 * @param son �ӽڵ��λ��
	 * @return
	 */
	public boolean isFather(int father, int son){
		return(Integer.parseInt(head[son])==father+1);
	}

	/**
	 * �жϲ����ʾ�Ľڵ�ĸ��׽ڵ��Ƿ���ROOT
	 * @param son �ӽڵ��λ��
	 * @return
	 */
	public boolean isFatherRoot(int son){
		return(Integer.parseInt(head[son])==0);
	}

	/**
	 * �ж��ַ��Ƿ�Ϊ���֣�ֻ�жϵ�һ���ַ�
	 * @param msg
	 * @return
	 */
	public boolean isNumeric(String msg){
		if(java.lang.Character.isDigit(msg.charAt(0))){
			return true;
			}
		return false;
		}


	/**
	 * �����Ա�۲�ѵ������
	 *
	 */
	public void outputTrainSentence(){
		int i=0;
		for(;i<sentenceLength;i++){
			System.out.println(i+1+"\t"+form[i]+"\t"+lemma[i]+"\t"+plemma[i]+"\t"+pos[i]+"\t"
					+ppos[i]+"\t-\t-\t"+head[i]+"\t"+phead[i]+"\t"+deprel[i]+"\t"+pdeprel[i]);
		}
	}

	/**
	 * �����Ա�۲���Ծ���
	 *
	 */
	public void outputTestSentence(){
		int i=0;
		for(;i<sentenceLength;i++){
			System.out.println(i+1+"\t"+form[i]+"\t"+lemma[i]+"\t"+plemma[i]+"\t"+pos[i]+"\t"
					+ppos[i]);
		}
	}

	/**
	 * �����Ա�۲���Ծ���
	 * @throws IOException
	 *
	 */
	public void storeTrainSentence(FileWriter newWriter) throws IOException{
		int i=0;
		for(;i<sentenceLength;i++){
			newWriter.write(i+1+"\t"+form[i]+"\t"+lemma[i]+"\t"+plemma[i]+"\t"+pos[i]+"\t"
					+ppos[i]+"\t-\t-\t"+head[i]+"\t"+phead[i]+"\t"+deprel[i]+"\t"+pdeprel[i]+"\n");
		}
		newWriter.write("\n");
	}



	//���ͷ�ڵ���������Ա���ѧϰ
	void statHeadForMaxentI(FileWriter newWriter,int sensize,int headsin) throws IOException{
		for(int i=0;i<sentenceLength;i++){
			//ͷ�ڵ�λ���ӽڵ�����
			String headForm=form[i];
			String headPos=pos[i];
			String leftPos= "NULL";
			String leftForm= "NULL";
			String left2Pos= "NULL";
			String left3Pos= "NULL";
			String rightPos= "NULL";
			String rightForm= "NULL";
			String right2Pos= "NULL";
			String right3Pos= "NULL";
			String leftPosheadPos="NULL";
			String rightPosheadPos="NULL";
			String left2PosheadPos="NULL";
			String right2PosheadPos="NULL";
			String left3PosheadPos="NULL";
			String right3PosheadPos="NULL";
			String left2PosleftPosheadPos="NULL";
			String right2PosrightPosheadPos="NULL";
			String leftPosheadPosRightPos="NULL";
			String headPosheadForm="NULL";
			String leftFormheadForm="NULL";
			String rightFormheadForm="NULL";
			String leftPosheadPosheadForm="NULL";
			String rightPosheadPosheadForm="NULL";
			String leftPosheadPosRightPosheadForm="NULL";
			String left2PosLeftPosheadPosheadForm="NULL";
			String right2PosRightPosheadPosheadForm="NULL";
			String tempFeature= "NULL";
			int leftsin=0;
			int rightsin=0;
			//ͷ�ڵ����
			for(int j=0;j<i;j++)
				if(isFather(i,j)){
					leftsin=1;
					break;
				}
			for(int j=i;j<sentenceLength;j++)
				if(isFather(i,j)){
					rightsin=1;
					break;
				}
			if(i>2){
				leftPos=pos[i-1];
				leftForm=form[i-1];
				left2Pos=pos[i-2];
				left3Pos=pos[i-3];
			}else if(i==2){
				leftPos=pos[i-1];
				leftForm=form[i-1];
				left2Pos=pos[i-2];
			}else if(i==1){
				leftPos=pos[0];
				leftForm=form[0];
			}
			//ͷ�ڵ��ұ�
			if(i<sentenceLength-3){
				rightPos=pos[i+1];
				rightForm=form[i+1];
				right2Pos=pos[i+2];
				right3Pos=pos[i+2];
			}else if(i<sentenceLength-2){
				rightPos=pos[i+1];
				rightForm=form[i+1];
				right2Pos=pos[i+2];
			}else if(i==sentenceLength-2){
				rightPos=pos[i+1];
				rightForm=form[i+1];
			}
			leftPosheadPos=leftPos+"-"+headPos;
			rightPosheadPos=rightPos+"-"+headPos;
			left2PosheadPos=left2Pos+"-"+headPos;
			right2PosheadPos=right2Pos+"-"+headPos;
			left3PosheadPos=left3Pos+"-"+headPos;
			right3PosheadPos=right3Pos+"-"+headPos;
			left2PosleftPosheadPos=left2Pos+"-"+leftPosheadPos;
			right2PosrightPosheadPos=right2Pos+"-"+rightPosheadPos;
			leftPosheadPosRightPos=leftPosheadPos+"-"+rightPos;
			headPosheadForm=headPos+"-"+headForm;
			leftFormheadForm=leftForm+"-"+headForm;
			rightFormheadForm=rightForm+"-"+headForm;
			leftPosheadPosheadForm=leftPosheadPos+"-"+headForm;
			rightPosheadPosheadForm=rightPosheadPos+"-"+headForm;
			left2PosLeftPosheadPosheadForm=left2PosleftPosheadPos+"-"+headForm;
			right2PosRightPosheadPosheadForm=right2PosrightPosheadPos+"-"+headForm;
			leftPosheadPosRightPosheadForm=leftPosheadPosRightPos+"-"+headForm;
			String feature1="1:"+headPos;
			String feature2="2:"+leftPosheadPos;
			String feature3="3:"+rightPosheadPos;
			String feature4="4:"+left2PosheadPos;
			String feature5="5:"+right2PosheadPos;
			String feature6="6:"+left3PosheadPos;
			String feature7="7:"+right3PosheadPos;
			String feature8="8:"+left2PosleftPosheadPos;
			String feature9="9:"+right2PosrightPosheadPos;
			String feature10="10:"+leftPosheadPosRightPos;
			String feature11="11:"+headForm;
			String feature12="12:"+leftFormheadForm;
			String feature13="13:"+rightFormheadForm;
			String feature14="14:"+headPosheadForm;
			String feature15="15:"+leftPosheadPosheadForm;
			String feature16="16:"+rightPosheadPosheadForm;
			String feature17="17:"+left2PosLeftPosheadPosheadForm;
			String feature18="18:"+right2PosRightPosheadPosheadForm;
			String feature19="19:"+leftPosheadPosRightPosheadForm;

			if(headsin==1)
				if(leftsin==1||rightsin==1)
					tempFeature="1 "+feature1+" "+feature2+" "+feature3+" "
					+feature4+" "+feature5+" "+feature6+" "+feature7+" "+feature8+" "
					+feature9+" "+feature10+" "+feature11+" "+feature12+" "+feature13+" "
					+feature14+" "+feature15+" "+feature16+" "+feature17+" "+feature18+" "
					+feature19+"\n";
				else
					tempFeature="0 "+feature1+" "+feature2+" "+feature3+" "
					+feature4+" "+feature5+" "+feature6+" "+feature7+" "+feature8+" "
					+feature9+" "+feature10+" "+feature11+" "+feature12+" "+feature13+" "
					+feature14+" "+feature15+" "+feature16+" "+feature17+" "+feature18+" "
					+feature19+"\n";
			else
				tempFeature=feature1+" "+feature2+" "+feature3+" "
				+feature4+" "+feature5+" "+feature6+" "+feature7+" "+feature8+" "
				+feature9+" "+feature10+" "+feature11+" "+feature12+" "+feature13+" "
				+feature14+" "+feature15+" "+feature16+" "+feature17+" "+feature18+" "
				+feature19+"\n";
			newWriter.write(tempFeature);
		}
	}

	//	���ͷ�ڵ�ļ��������Ա��ڲ�ѯ
	void statHeadForMaxentS(FileWriter newWriter,int sensize,int headsin) throws IOException{
		for(int i=0;i<sentenceLength;i++){
			//ͷ�ڵ�λ���ӽڵ�����
			String headForm=form[i];
			String headPos=pos[i];
			String leftPos= "NULL";
			String leftForm= "NULL";
			String left2Pos= "NULL";
			String left3Pos= "NULL";
			String rightPos= "NULL";
			String rightForm= "NULL";
			String right2Pos= "NULL";
			String right3Pos= "NULL";
			String leftPosheadPos="NULL";
			String rightPosheadPos="NULL";
			String left2PosheadPos="NULL";
			String right2PosheadPos="NULL";
			String left3PosheadPos="NULL";
			String right3PosheadPos="NULL";
			String left2PosleftPosheadPos="NULL";
			String right2PosrightPosheadPos="NULL";
			String leftPosheadPosRightPos="NULL";
			String headPosheadForm="NULL";
			String leftFormheadForm="NULL";
			String rightFormheadForm="NULL";
			String leftPosheadPosheadForm="NULL";
			String rightPosheadPosheadForm="NULL";
			String leftPosheadPosRightPosheadForm="NULL";
			String left2PosLeftPosheadPosheadForm="NULL";
			String right2PosRightPosheadPosheadForm="NULL";
			String tempFeature= "NULL";
			int leftsin=0;
			int rightsin=0;
			//ͷ�ڵ����
			for(int j=0;j<i;j++)
				if(isFather(i,j)){
					leftsin=1;
					break;
				}
			for(int j=i;j<sentenceLength;j++)
				if(isFather(i,j)){
					rightsin=1;
					break;
				}
			if(i>2){
				leftPos=pos[i-1];
				leftForm=form[i-1];
				left2Pos=pos[i-2];
				left3Pos=pos[i-3];
			}else if(i==2){
				leftPos=pos[i-1];
				leftForm=form[i-1];
				left2Pos=pos[i-2];
			}else if(i==1){
				leftPos=pos[0];
				leftForm=form[0];
			}
			//ͷ�ڵ��ұ�
			if(i<sentenceLength-3){
				rightPos=pos[i+1];
				rightForm=form[i+1];
				right2Pos=pos[i+2];
				right3Pos=pos[i+2];
			}else if(i<sentenceLength-2){
				rightPos=pos[i+1];
				rightForm=form[i+1];
				right2Pos=pos[i+2];
			}else if(i==sentenceLength-2){
				rightPos=pos[i+1];
				rightForm=form[i+1];
			}
			tempFeature=left3Pos+"-"+left2Pos+"-"+leftPos+"-"+headPos+"-"
				+rightPos+"-"+right2Pos+"-"+right3Pos+"-"+leftForm+"-"
				+headForm+"-"+rightForm+"\n";
			newWriter.write(tempFeature);
		}
	}

	//	���ͷ�ڵ�ļ��������Ա��ڲ�ѯ
	public String statHeadForMaxentS(int i){
		String headForm=form[i];
		String headPos=pos[i];
		String leftPos= "NULL";
		String leftForm= "NULL";
		String left2Pos= "NULL";
		String left3Pos= "NULL";
		String rightPos= "NULL";
		String rightForm= "NULL";
		String right2Pos= "NULL";
		String right3Pos= "NULL";
		String tempFeature= "NULL";
		if(i>2){
			leftPos=pos[i-1];
			leftForm=form[i-1];
			left2Pos=pos[i-2];
			left3Pos=pos[i-3];
		}else if(i==2){
			leftPos=pos[i-1];
			leftForm=form[i-1];
			left2Pos=pos[i-2];
		}else if(i==1){
			leftPos=pos[0];
			leftForm=form[0];
		}
		//ͷ�ڵ��ұ�
		if(i<sentenceLength-3){
			rightPos=pos[i+1];
			rightForm=form[i+1];
			right2Pos=pos[i+2];
			right3Pos=pos[i+2];
		}else if(i<sentenceLength-2){
			rightPos=pos[i+1];
			rightForm=form[i+1];
			right2Pos=pos[i+2];
		}else if(i==sentenceLength-2){
			rightPos=pos[i+1];
			rightForm=form[i+1];
		}
		tempFeature=left3Pos+"-"+left2Pos+"-"+leftPos+"-"+headPos+"-"
			+rightPos+"-"+right2Pos+"-"+right3Pos+"-"+leftForm+"-"
			+headForm+"-"+rightForm;
		return tempFeature;
	}




	//ͳ�ƾ���ͬһ�����ڵ�������ӽڵ��Ҵ�����ͬ�����,�������Ҷ��е����
	void statSameHead(ArrayList headList,int sensize){
		for(int i=0;i<sentenceLength;i++){
			//ͷ�ڵ�λ���ӽڵ�����
			if(i==0){
				String headString=sensize+"-Left---"+pos[i]+"-"+i+"-"+form[i];
				String tempString=headString;
				for(int j=1;j<sentenceLength;j++)
				{
					if(isFather(i,j))
						addSameHead(headList,pos[i]);
					if(isFather(i,j)&&pos[i].equals(pos[j]))
						tempString+=pos[j]+"-"+j+"-"+form[j];
				}
				if(!tempString.equals(headString)){
					addHeadLeftList(headList,pos[i],tempString);
				}
			}
			//ͷ�ڵ�λ���ӽڵ���ұ�
			else if(i==sentenceLength-1){
				String headString=sensize+"-Right---"+pos[i]+"-"+i+"-"+form[i];
				String tempString=headString;
				for(int j=0;j<i;j++)
				{
					if(isFather(i,j))
						addSameHead(headList,pos[i]);
					if(isFather(i,j)&&pos[i].equals(pos[j]))
						tempString+=pos[j]+"-"+j+"-"+form[j];
				}
				if(!tempString.equals(headString))
					addHeadRightList(headList,pos[i],tempString);
			}
			else{
				int leftsin=0;
				int rightsin=0;;
				String rightHeadString=sensize+"-Right---"+pos[i]+"-"+i+"-"+form[i];
				for(int j=0;j<i;j++)
				{
					if(isFather(i,j))
						addSameHead(headList,pos[i]);
					if(isFather(i,j)&&pos[i].equals(pos[j])){
						rightHeadString+=pos[j]+"-"+j+"-"+form[j];
						rightsin=1;
						break;
					}
				}
				String leftHeadString=sensize+"-Left---"+pos[i]+"-"+i+"-"+form[i];
				for(int j=i+1;j<sentenceLength;j++)
				{
					if(isFather(i,j))
						addSameHead(headList,pos[i]);
					if(isFather(i,j)&&pos[i].equals(pos[j])){
						leftHeadString+=pos[j]+"-"+j+"-"+form[j];
						leftsin=1;
						break;
					}
				}
				String midHeadString;
				if(leftsin==1&&rightsin==1){
					midHeadString=sensize+"-Middle---"+pos[i]+"-"+i+"-"+form[i];
					addSameHead(headList,pos[i]);
					for(int j=0;j<sentenceLength;j++)
					{
						if(isFather(i,j))
							addSameHead(headList,pos[i]);
						if(isFather(i,j)&&pos[i].equals(pos[j])){
							midHeadString+=pos[j]+"-"+j+"-"+form[j];
							rightsin=1;
						}
					}
					addHeadMiddleList(headList,pos[i],midHeadString);
				}
				else if(rightsin==1)
					addHeadRightList(headList,pos[i],rightHeadString);
				else if(leftsin==1)
					addHeadLeftList(headList,pos[i],leftHeadString);
			}
		}
	}

	void addSameHead(ArrayList headList,String pos){
		if(headList.size()==0){
			ArrayList<String> newArray=new ArrayList<String>();
			newArray.add(pos);
			newArray.add("0");
			newArray.add("0");
			newArray.add("0");
			headList.add(newArray);
		}
		else{
			int signal=0;
			int k;
			for(k=0;k<headList.size();k++){
				if(pos.equals(((ArrayList<String>) headList.get(k)).get(0))){
					signal=1;
					break;
				}
			}
			if(signal==0){
				ArrayList newArray=new ArrayList();
				newArray.add(pos);
				newArray.add("0");
				newArray.add("0");
				newArray.add("0");
				headList.add(newArray);
			}
		}
	}

	void addHeadLeftList(ArrayList headList,String pos,String tempString){
		int signal=0;
		int k;
		for(k=0;k<headList.size();k++){
			if(pos.equals(((ArrayList<String>) headList.get(k)).get(0))){
				signal=1;
				break;
			}
		}
		if(signal==1){
			int temp=Integer.parseInt(((ArrayList<String>) headList.get(k)).get(1));
			((ArrayList<String>) headList.get(k)).set(1,Integer.toString(temp+1));
			((ArrayList<String>) headList.get(k)).add(tempString);
		}
	}

	void addHeadRightList(ArrayList headList,String pos,String tempString){
		int signal=0;
		int k;
		for(k=0;k<headList.size();k++){
			if(pos.equals(((ArrayList<String>) headList.get(k)).get(0))){
				signal=1;
				break;
			}
		}
		if(signal==1){
			int temp=Integer.parseInt(((ArrayList<String>) headList.get(k)).get(2));
			((ArrayList<String>) headList.get(k)).set(2,Integer.toString(temp+1));
			((ArrayList<String>) headList.get(k)).add(tempString);
		}
	}

	void addHeadMiddleList(ArrayList headList,String pos,String tempString){
		int signal=0;
		int k;
		for(k=0;k<headList.size();k++){
			if(pos.equals(((ArrayList<String>) headList.get(k)).get(0))){
				signal=1;
				break;
			}
		}
		if(signal==1){
			int temp=Integer.parseInt(((ArrayList<String>) headList.get(k)).get(3));
			((ArrayList<String>) headList.get(k)).set(3,Integer.toString(temp+1));
			((ArrayList<String>) headList.get(k)).add(tempString);
		}
	}

	public Object clone(){
		Conll09Sentence o=null;
		try{
			o = (Conll09Sentence)super.clone();
			}
		catch(CloneNotSupportedException e){
			e.printStackTrace();
			}
		return o;
		}

	public static void main(String[] args){

	}

	}


