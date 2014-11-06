package edu.hitsz.nlp.transDep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;
import edu.hitsz.nlp.mstjoint.io.DependencyReader;
import edu.hitsz.nlp.mstjoint.io.DependencyWriter;
import gnu.trove.TIntArrayList;

public abstract class TransPipe {

	public Parameters params;
	public TransOptions options;

    public FeatureMap dataMap; //特征map
    public FeatureMap postagMap; //依存关系map
    public FeatureMap deprelMap; //依存关系map
    
	public DependencyReader depReader;
    private DependencyWriter depWriter;
    
    public ArrayList<String> postags; //词性
    public ArrayList<String> deprels; //依存关系数组
    
    public boolean isSegPos = true;
    public boolean isLabeled = false;
	public boolean isDp = true; //是否dynamic programming	
    
    /**
     * 初始化参数,
     * @param options
     * @throws IOException
     */
    public TransPipe(TransOptions options) throws IOException {

    	params = new Parameters();
    	this.options = options;
    	
		dataMap = new FeatureMap();
		postagMap = new FeatureMap();
		deprelMap = new FeatureMap();

		depReader = DependencyReader.createDependencyReader("CONLL", false);
		
		isSegPos = options.isSegPos;
		isLabeled = options.isLabeled;
		isDp = options.isDp;
    }
    
    /**
     * 根据解析类型返回对应的管道
     * @since Mar 1, 2013
     * @param options
     * @return
     * @throws IOException
     */
    public static TransPipe getPipe(TransOptions options) throws IOException {    	
    	if(options.decodingMethod.equals("ArcStandardHuang2010"))
    		return new TransPipeArcStandardHuang2010(options);
    	return new TransPipeArcStandardHuang2010(options);
    		
    }

    

    
    /**
     * 获得文件中句子的数目,读取其中的词性和依存关系
     * @since Mar 28, 2012
     * @param file
     * @return
     * @throws IOException
     */
    public int[] getPosDep(String file) throws IOException {

    	if(isSegPos && isLabeled) {
    	
			System.out.print("Getting sentence number ... ");
			isLabeled = depReader.startReading(file);
			DependencyInstanceJoint instance = depReader.getNext();	
	
			TIntArrayList lengths = new TIntArrayList();
			
			while(instance != null) {
				//词性
				if(isSegPos) {
					String[] postags = instance.postags;
					for(int i = 1; i < postags.length; i++)
				    	postagMap.add(postags[i]);
				}
				//依存关系
				if(isLabeled) {
					String[] deprels = instance.deprels;
				    for(int i = 1; i < deprels.length; i++)
				    	deprelMap.add(deprels[i]);
				}				    
				lengths.add(instance.length());		    
			    instance = null;
			    instance = depReader.getNext();
			}
			
			transPosDep();
			System.out.println("done. There are totally " + lengths.size() + " sentences.");
			
			return lengths.toNativeArray();
    	}
    	else
    		return null;				
    }
    
    /**
     * 关闭特征map和依存关系map,将依存关系从map存到数组
     */
    public void transPosDep() {
		//词性
    	if(isSegPos) {
	    	postags = new ArrayList<String>();
	    	HashMap<String, Integer> postagsMap = postagMap.getMap();
			Iterator<Entry<String, Integer>> iter1 = postagsMap.entrySet().iterator();
			while(iter1.hasNext()){
				Entry<String, Integer> entry =  iter1.next();
				postags.add(entry.getKey());
			}	
    	}
    	//依存
    	if(isLabeled) {
			deprels = new ArrayList<String>();
			if(isLabeled)
				deprels.add("NONE");
			else {
				HashMap<String, Integer> deprelsMap = deprelMap.getMap();
				Iterator<Entry<String, Integer>> iter = deprelsMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, Integer> entry =  iter.next();
					deprels.add(entry.getKey());
				}	
			}
    	}
    }
    
    
    /**
     * 初始化写入文件
     * @param file
     * @throws IOException
     */
    public void initInputFile (String file) throws IOException {
    	depReader.startReading(file);
    }

    /**
     * 初始化输出文件
     * @param file
     * @throws IOException
     */
    public void initOutputFile (String file) throws IOException {
		depWriter =
		    DependencyWriter.createDependencyWriter("CONLL", isLabeled);
		depWriter.startWriting(file);
    }

    /**
     * 输出一个依存句子
     * @param instance
     * @throws IOException
     */
    public void outputInstance (DependencyInstanceJoint instance) throws IOException {
    	depWriter.write(instance);
    }

    /**
     * 输出器关闭
     * @throws IOException
     */
    public void close () throws IOException {
		if (null != depWriter) {
		    depWriter.finishWriting();
		}
    }


    public void add(String feat, DepFeatureVector fv, boolean added) {    	
    	if(added) {
    		int num = dataMap.add(feat);
        	if(num >= 0)
        		fv.add(num);
    	}
    	else {
    		fv.addString(feat);
	    	int num = dataMap.get(feat);
	    	if(num >= 0)
	    		fv.add(num);
    	}    	
    }
    
    /**
     * 模拟gold的执行过程
     * @since Apr 17, 2013
     * @param instance
     * @return
     */
    public abstract List<Pair<TransAction, DepState>> simulateGold(InputSequence goldSequence);
    
    public abstract DepState moveSequentActions(InputSequence goldSequence, List<TransAction> actions, boolean added);
        
    public abstract DependencyInstanceJoint decoding(
    		InputSequence goldSequence, 
    		int K,
    		boolean isTrain,
    		int[] iter) ;
    
    public abstract DynamicFeatures resetDynamicFeatures(DepState state);
	
    public abstract DepFeatureVector getFeatureVector(DepState state,
			TransAction action,
			boolean added);
}
