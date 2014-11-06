package edu.hitsz.nlp.mstjoint.io;

import java.io.IOException;

import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;


public class CONLLWriter extends DependencyWriter{

	 public CONLLWriter (boolean labeled) {
			this.labeled = labeled;
		 }

	 public void write(DependencyInstanceJoint instance) throws IOException {

		for (int i=0; i<instance.length(); i++) {
		    //writer.write(Integer.toString(i+1));                writer.write('\t');
		    writer.write(instance.forms[i]);                    writer.write('\t');
		    //writer.write(instance.forms[i]);                    writer.write('\t');
		    //writer.write(instance.forms[i]);                    writer.write('\t');
		    writer.write(instance.postags[i]);                  writer.write('\t');
		    //writer.write(instance.postags[i]);                  writer.write('\t');
		    //writer.write("-");                                  writer.write('\t');
		    //writer.write("-");                                  writer.write('\t');
		    //writer.write(Integer.toString(instance.heads[i]));  writer.write('\t');
		    writer.write(Integer.toString(instance.heads[i]));  writer.write('\t');
		    
		    if(instance.deprels != null)
		    	writer.write(instance.deprels[i]);                  
		    else 
		    	writer.write("-");
		    /*
		    writer.write('\t');
		    if(instance.deprels != null)
		    	writer.write(instance.deprels[i]);                  
		    else 
		    	writer.write("-");
		    writer.write('\t');
		    writer.write("-\t-");
		    */
		    writer.newLine();		    
		}
		writer.newLine();

	 }

	
	
}
