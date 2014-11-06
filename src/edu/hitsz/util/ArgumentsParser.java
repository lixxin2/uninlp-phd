package edu.hitsz.util;

import java.util.HashMap;
import java.util.Vector;

/**
 * Arguments for program. These arguments can be read from cmdline or from option file.
 * <p> 参数解析器
 * @author tm
 *
 */
public class ArgumentsParser {
	HashMap options;
	
	public ArgumentsParser(){
		options=new HashMap();
	}

	/**
	 * parser arguments from cmdline. for short arguments 
	 * @param args Arguments from cmdline, starting with '-', one example "-a -b host -ckeke -dhaha".
	 * @param shortArgs Like "ab:c::d::", in which 'a' represents having no parameter, 
	 * 'b' represents the parameter separate with the option,
	 * 'c' and 'd' means that the parameter is close to the option.
	 */
	public void parseCmdLine(String[] args, String shortArgs){
		//parse options from shortArgs
		int shortOptionLength=shortArgs.length();
		if(shortOptionLength < 1){
			System.out.println("There is no option existing");
			System.exit(1);
		}
		for(int i=0;i<shortOptionLength;i++){
			if(Character.isLetter(shortArgs.charAt(i))){
				//[a,                     b,                            c]
				//a:Is a duplicated;      b:[0,1,2] type of shortarg    c:the argument
				Vector newV=new Vector();
				newV.clear();
				//for c::d::
				if(i+2<shortOptionLength&&shortArgs.charAt(i+1)==':'&&shortArgs.charAt(i+2)==':'){
					if(!options.containsKey(shortArgs.charAt(i))){
						newV.add(0);newV.add(2);
						options.put(shortArgs.substring(i,i+1), newV);
						i+=2;
					}
					else{
						System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
					}
				}
				//for b:
				else if(i+1<shortOptionLength&&shortArgs.charAt(i+1)==':'){
					if(!options.containsKey(shortArgs.charAt(i))){
						newV.add(0);newV.add(1);
						options.put(shortArgs.substring(i,i+1), newV);
						i+=1;
					}
					else{
						System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
					}
				}
				//for a
				else{
					if(!options.containsKey(shortArgs.charAt(i))){
						newV.add(0); newV.add(0);
						options.put(shortArgs.substring(i,i+1), newV);
					}
					else{
						System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
					}
				}					
			}		
			else{
				System.out.println("The format of options\""+shortArgs+"\" is wrong");
				System.exit(1);
			}
		}
		//make the args from cmdline suitable with options
		int argNumber=0;
		int totalNumber=args.length;
		if(totalNumber<1){
			System.out.println("Number of cmdline input is less than 1.");
			System.exit(1);
		}
		else{
			for(int i=0;i<totalNumber;i++){
				if(args[i].length()<2){
					System.out.println("one of cmdline input is Wrong");
					System.exit(1);
				}
				//String onePre=args[i].substring(0,1);
				//String twoPre=args[i].substring(0,2);
				String newArg=args[i].substring(1);
				String newOpt=newArg.substring(0,1);
				if(options.containsKey(newOpt)){
					argNumber+=1;
					Vector newVector=(Vector) options.get(newOpt);
					//for a
					if((Integer)newVector.get(0)==0){
						newVector.set(0, 1);
						if((Integer)newVector.get(1)==0){							
							if(newArg.length()==1){
								continue;
							}							
							else{
								System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
								System.exit(1);
							}						
						}
						//for b:
						else if((Integer)newVector.get(1)==1){							
							if(newArg.length()==1){
								if(i+1<totalNumber&&args[i+1].charAt(0)!='-'){
									newVector.add(args[i+1]);								
									i+=1;
									continue;
								}
								else{
									System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
									System.exit(1);
								}
							}							
							else{
								System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
								System.exit(1);
							}							
						}
						//for c::d::
						else if((Integer)newVector.get(1)==2){				
							if(newArg.length()>1){
								newVector.add(newArg.substring(1));							
								continue;							
							}
							else{
								System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
								System.exit(1);
							}							
						}
						//for else
						else{
							System.out.println("one of cmdline input is wrong");
							System.exit(1);
						}
					}
					else{
						System.out.println("the argument\'"+newOpt+"\" is duplicated!");
					}
				}
				//options don't contain the option
				else{
					System.out.println("one of cmdline input not contained in options");
					System.exit(1);
				}				
			}
		}
		//check consistency
		/*
		if(argNumber!=options.size()){
			System.out.println("The number of options and argument is not equal");
			System.exit(1);
		}
		*/		
	}
	
	/**
	 * parser arguments from cmdline. for all arguments 
	 * @param args Arguments from cmdline, starting with '-', one example "-a -b host -ckeke -dhaha".
	 * @param shortArgs Like "ab:c::d::", in which 'a' represents having no parameter, 
	 * 'b' represents the parameter separate with the option,
	 * 'c' and 'd' means that the parameter is close to the option.
	 * @param longArgs Like ["help", "output="],
	 */
	public void parseCmdLine(String[] args, String shortArgs, String[] longArgs){		
		int shortOptionLength=shortArgs.length();
		int longOptionLength=longArgs.length;
		if(shortOptionLength+longOptionLength<1){
			System.out.println("There is no option existing");
			System.exit(1);
		}
		//parse options from shortArgs
		if(shortOptionLength>0){
			for(int i=0;i<shortOptionLength;i++){
				if(Character.isLetter(shortArgs.charAt(i))){
					//[a,                     b,                            c]
					//a:Is a duplicated;      b:[0,1,2] type of shortarg    c:the argument
					Vector newV=new Vector();
					newV.clear();
					//for c::d::
					if(i+2<shortOptionLength&&shortArgs.charAt(i+1)==':'&&shortArgs.charAt(i+2)==':'){
						if(!options.containsKey(shortArgs.charAt(i))){
							newV.add(0);newV.add(2);
							options.put(shortArgs.substring(i,i+1), newV);
							i+=2;
						}
						else{
							System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
						}
					}
					//for b:
					else if(i+1<shortOptionLength&&shortArgs.charAt(i+1)==':'){
						if(!options.containsKey(shortArgs.charAt(i))){
							newV.add(0);newV.add(1);
							options.put(shortArgs.substring(i,i+1), newV);
							i+=1;
						}
						else{
							System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
						}
					}
					//for a
					else{
						if(!options.containsKey(shortArgs.charAt(i))){
							newV.add(0);newV.add(0);
							options.put(shortArgs.substring(i,i+1), newV);
						}
						else{
							System.out.println("the option \""+shortArgs.substring(i,i+1)+"\"is duplicated!");
						}
					}					
				}		
				else{
					System.out.println("The format of options\""+shortArgs+"\" is wrong");
					System.exit(1);
				}
			}
		}
		//parse options from longArgs
		if(longOptionLength>0){	
			for(int i=0;i<longOptionLength;i++)
			{
				//[a,                     b,                            c]
				//a:Is a duplicated;      b:[3,4] type of longarg    c:the argument
				Vector newV=new Vector();
				newV.clear();
				if(!longArgs[i].contains("=")){
					newV.add(0);newV.add(3);
					options.put(longArgs[i], newV);					
				}
				else{
					newV.add(0);newV.add(4);
					options.put(longArgs[i], newV);		
				}				
			}
		}
		//make the args from cmdline suitable with options
		int argNumber=0;
		int totalNumber=args.length;
		if(totalNumber<1){
			System.out.println("Number of cmdline input is less than 1.");
			System.exit(1);
		}
		else{
			for(int i=0;i<totalNumber;i++){
				if(args[i].length()<2){
					System.out.println("one of cmdline input \""+args[i]+"\" is Wrong");
					System.exit(1);
				}
				String onePre=args[i].substring(0,1);
				String twoPre=args[i].substring(0,2);
				//for long options
				if(twoPre.equals("--")){
					if(args[i].length()<4){
						System.out.println("one of cmdline input \""+args[i]+"\" is Wrong");
						System.exit(1);
					}					
					String newArg=args[i].substring(2);
					//for "--train=aaa"
					if(newArg.contains("=")){
						int position=newArg.indexOf("=");
						if(position<2||position+1>=newArg.length()){
							System.out.println("one of cmdline input \""+args[i]+"\" is Wrong");
							System.exit(1);
						}
						String key=newArg.substring(0,position+1);
						String value=newArg.substring(position+1);
						if(options.containsKey(key)){
							argNumber+=1;
							Vector newVector=(Vector) options.get(key);
							newVector.set(0, 1);
							newVector.add(value);
							continue;
						}
						//options don't contain the option
						else{
							System.out.println("one of cmdline input not contained in options");
							System.exit(1);
						}	
					}
					//for "--help"
					else{
						if(options.containsKey(newArg)){
							argNumber+=1;
							Vector newVector=(Vector) options.get(newArg);
							newVector.set(0, 1);
							continue;
						}
					}					
				}
				//for short options
				else if(onePre.equals("-")){
					String newArg=args[i].substring(1);
					String newOpt=newArg.substring(0,1);
					if(options.containsKey(newOpt)){
						argNumber+=1;
						Vector newVector=(Vector) options.get(newOpt);
						//for a
						if((Integer)newVector.get(0)==0){
							newVector.set(0, 1);
							if((Integer)newVector.get(1)==0){							
								if(newArg.length()==1){
									continue;
								}							
								else{
									System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
									System.exit(1);
								}						
							}
							//for b:
							else if((Integer)newVector.get(1)==1){							
								if(newArg.length()==1){
									if(i+1<totalNumber&&args[i+1].charAt(0)!='-'){
										newVector.add(args[i+1]);								
										i+=1;
										continue;
									}
									else{
										System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
										System.exit(1);
									}
								}							
								else{
									System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
									System.exit(1);
								}							
							}
							//for c::d::
							else if((Integer)newVector.get(1)==2){				
								if(newArg.length()>1){
									newVector.add(newArg.substring(1));							
									continue;							
								}
								else{
									System.out.println("format of one of cmdline input \""+newArg+"\" is Wrong");
									System.exit(1);
								}							
							}
							//for else
							else{
								System.out.println("one of cmdline input is wrong");
								System.exit(1);
							}
						}
						else{
							System.out.println("the argument \""+newOpt+"\" is duplicated!");
						}
					}
					//options don't contain the option
					else{
						System.out.println("one of cmdline input not contained in options");
						System.exit(1);
					}		
				}
				else{
					System.out.println("one of cmdline input \""+args[i]+"\" is duplicated!");
					System.exit(1);
				}
			}
		}
		
		//check consistency
		/*
		if(argNumber!=options.size()){
			System.out.println("The number of options and argument is not equal");
			System.exit(1);
		}
		*/		
	}
	
	/**
	 * whether the option from shortArgs or longArgs exists or not 
	 * @param oneOption
	 * @return
	 */
	public boolean containsOption(String oneOption){
		if(options.containsKey(oneOption)){
			Vector newVec=(Vector)options.get(oneOption);
			if((Integer)newVec.get(0)==1)
				return true;
		}
		return false;
	}
		
	/**
	 * whether the option from shortArgs or longArgs has a corresponding argument is cmdline inputing
	 * @param oneOption
	 * @return
	 */
	public boolean containsArgument(String oneOption){
		if(this.containsOption(oneOption)){
			Vector newVec=(Vector) options.get(oneOption);
			if(newVec.size()==3)
				return true;
		}
		return false;
	}
	
	/**
	 * return the corresponding argument for the option from shortArgs or longArgs
	 * @param oneOption
	 * @return
	 */
	public String getArgument(String oneOption){
		String tmp;
		if(this.containsArgument(oneOption)){
			Vector newVec=(Vector) options.get(oneOption);
			tmp=(String) newVec.get(2);
			return tmp;
		}
		else	
			return null;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArgumentsParser newA=new ArgumentsParser();
		String shortArgs="ab:c::d::";
		String[] longArgs={"aa","bb="};
		newA.parseCmdLine(args, shortArgs,longArgs);
		if(newA.containsArgument("bb="))
			System.out.println(newA.getArgument("bb="));
	}

	
	

}
