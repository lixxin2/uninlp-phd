package edu.hitsz.nlp.transDep;

public class DynamicFeatures {
	
	protected int numFeatures;
	protected String[] features;
	protected int hash;
	
	public DynamicFeatures(String[] features)
	{
		numFeatures = features.length;
		this.features = features;
		hash = hashCode();		
	}	

	@Override
	public String toString() {	
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < features.length; ++i)
		{
			sb.append(features[i]);
			sb.append(' ');
		}
		return sb.toString();
	}
	
	@Override 
	public DynamicFeatures clone() {
		return new DynamicFeatures(features);
	}

	@Override
	public boolean equals(Object obj)
	{
		// check the equality of the type
		if (obj == null || !(obj instanceof DynamicFeatures))
			return false;
		DynamicFeatures atoms = (DynamicFeatures)obj;

		// return hash == atoms.hash;

		// check the equality of features
		for (int i = 0; i < features.length; ++i)
		{
			String s1 = features[i];
			String s2 = atoms.features[i];
			if ((s1 == null ^ s1 == null) || s1 != null && !s1.equals(s2)) return false;
		}
		
		return true;
	}

	@Override
	public int hashCode()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < features.length; ++i)
		{
			sb.append(features[i]);
			sb.append(' ');
		}
		return sb.toString().hashCode();
	}
}
