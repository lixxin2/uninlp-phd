package edu.hitsz.nlp.mstjoint.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;

public class CONLLReader extends DependencyReader{

    public CONLLReader () {
    	
    }

    /**
     * 获取下一个依存实例,CONLL格式,用于训练
     * <p> 其中包含词（form,lemma)，词性(pos,cpos)，头节点，依存关系     * 
     */
    public DependencyInstanceJoint getNext() throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();

		String line = inputReader.readLine();
		while (line != null && !line.equals("")) {
		    lineList.add(line.split("\t"));
		    line = inputReader.readLine();
		    //System.out.println("## "+line);
		} 

		int length = lineList.size();

		if(length == 0) {
		    inputReader.close();
		    return null;
		}

		String[] forms = new String[length+1];		
		String[] pos = new String[length+1];
		String[] deprels = new String[length+1];
		int[] heads = new int[length+1];

		forms[0] = "<root>";	
		pos[0] = "<root-POS>";
		deprels[0] = "ROOT";
		heads[0] = -1;

		for(int i = 0; i < length; i++) {
		    String[] info = lineList.get(i);
		    forms[i+1] = normalize(info[0]);
		    pos[i+1] = info[1];
		    heads[i+1] = Integer.parseInt(info[2]);
		    deprels[i+1] = labeled ? info[3] : "ROOT";
		}
		
		DependencyInstanceJoint instance =
			    new DependencyInstanceJoint(forms, pos, deprels, heads);
		
		// set up the course pos tags as just the first letter of the fine-grained ones
		String[] cpos = new String[length+1];
		cpos[0] = "<root-CPOS>";
		for(int i = 1; i < pos.length; i++)
		    cpos[i] = pos[i].substring(0,1);
		instance.cpostags = cpos;

		// set up the lemmas equals to form
		String[] lemmas = new String[length+1];
		lemmas[0] = "<root-LEMMA>";	
		for(int i = 1; i < forms.length; i++) {
		    lemmas[i] = forms[i];
		}
		instance.lemmas = lemmas;
		instance.feats = new String[0][0];

		return instance;
		
    }

   
  
    
    

    protected boolean fileContainsLabels (String file) throws IOException {
    	
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = in.readLine();
		in.close();

		if(line.trim().split("\\s+").length > 3)
		    return true;
		else
		    return false;
		
    }

}
