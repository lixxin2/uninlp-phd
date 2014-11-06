package edu.hitsz.nlp.transDep;

public class TransAction {
	
	public String m_action;	
	public String m_pos;
	
	public static final TransAction NotAvailable = new TransAction("NA");
	public static final TransAction End = new TransAction("E");; 
	
	private TransAction(String s)
	{		
		String[] p = s.split("-");
		m_action = p.length > 1 ? p[0] : s;
		m_pos = p.length > 1 ? p[1] : null;
	}
	
	public static TransAction shift() {
		return new TransAction("Shift");
	}
	
	public static TransAction reduceLeft(String deprel) {
		return new TransAction("ReduceLeft-"+deprel);
	}
	
	public static TransAction reduceRight(String deprel) {
		return new TransAction("ReduceRight-"+deprel);
	}
	
	
	public TransAction getAction(String actionName) {
		return new TransAction(actionName);
	}
		
	public String getActionName() {
		return m_pos == null ? m_action : m_action +"-"+ m_pos;
	}
	
	public boolean isShift() {
		if(m_action.equals("Shift"))
			return true;
		else
			return false;
	}
	
	public boolean isReduceLeft() {
		if(m_action.equals("ReduceLeft"))
			return true;
		else
			return false;
	}
	
	public boolean isReduceRight() {
		if(m_action.equals("ReduceRight"))
			return true;
		else
			return false;
	}

	public boolean shallowEquals(TransAction a)
	{
		return m_action.equals(a.m_action);
	}
	
	@Override
	public String toString() {		
		return m_action;		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TransAction))
			return false;
		TransAction a = (TransAction)obj;
		return m_action.equals(a.m_action);
	}
	
	@Override
	public int hashCode() {
		return m_action.hashCode() * 17 + 1;
	}
	
	
	
	
}
