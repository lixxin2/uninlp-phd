package edu.hitsz.nlp.coref;

import java.util.ArrayList;

public class NamedEntityNode {
	public String label;
	public int start;
	public int end;
	
	/**
	 * get ArrayList of namedEntity from columns of named entity structure
	 *  (LOC *
	 *       *
	 *       *)
	 * @param namedEntity
	 * @param namedEntityColumn
	 */
	public static void getFromColumn(ArrayList<NamedEntityNode> namedEntity, ArrayList<String> namedEntityColumn){
		int length = namedEntityColumn.size();
		int i=0;
		while(i<length){
			if(namedEntityColumn.get(i).startsWith("(")){
				if(namedEntityColumn.get(i).endsWith(")")){
					NamedEntityNode newNamedEntityNode = new NamedEntityNode();
					newNamedEntityNode.label = namedEntityColumn.get(i).substring(1,namedEntityColumn.get(i).length()-1);
					newNamedEntityNode.start = i;
					newNamedEntityNode.end = i;
					namedEntity.add(newNamedEntityNode);
				}
				else{
					NamedEntityNode newNamedEntityNode = new NamedEntityNode();
					newNamedEntityNode.label = namedEntityColumn.get(i).substring(1,namedEntityColumn.get(i).length()-1);
					newNamedEntityNode.start = i;
					while( i < length &&  !namedEntityColumn.get(i).endsWith(")"))
						i++;
					newNamedEntityNode.end = i;
					namedEntity.add(newNamedEntityNode);
				}
			}					
			i++;
		}
	}
	
}
