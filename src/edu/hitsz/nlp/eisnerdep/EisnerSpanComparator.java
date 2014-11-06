
package edu.hitsz.nlp.eisnerdep;

import java.util.Comparator;



/**
 * 
 * @author tm
 *
 */
public class EisnerSpanComparator implements Comparator{
	
	public int compare(Object o1,Object o2) {
		EisnerSpan p1=(EisnerSpan)o1;
		EisnerSpan p2=(EisnerSpan)o2; 
		if(p1.weight<p2.weight)
			return 1;
		else
			return 0;
		}
}



