package edu.hitsz.java.charset;

public class IsGB18030 {
	
	public   static   boolean   isCS(String   str){
        if(null==str)   return   false;
        if(str.trim()== " ")   return   false;
        byte[]   bytes = str.getBytes();
        if(bytes.length <2)
        	return   false;
        byte   aa=(byte)0xB0;
        byte   bb=(byte)0xF7;
        byte   cc=(byte)0xA1;
        byte   dd=(byte)0xFE;
        if(bytes[0] >=aa   &&   bytes[0] <=bb){ //高字节
         if(bytes[1] <cc   ||   bytes[1]   >   dd){ //低字节
          return   false;
         }
         return   true;
        }
        return   false;
	}
	
	public static void main(String[] args) {
		
		System.out.println(isCS("载"));
		System.out.println(isCS("載"));
		
		
		
	}
	
	

}
