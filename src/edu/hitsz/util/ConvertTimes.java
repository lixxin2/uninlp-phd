package edu.hitsz.util;

import java.util.Date;

/**
 * 时间转化类
 * @author Xinxin Li
 * @since Sep 22, 2013
 */
public class ConvertTimes {
	
	public long years,months,days,hours,minutes,seconds,milliSeconds;
	
	public ConvertTimes(){
		years=0;
		months=0;
		days=0;
		hours=0;
		minutes=0;
		seconds=0;
		milliSeconds=0;
	}
	
	/**
	 * convert the time in seconds into format [year,month,day,hour,minute,second]
	 * @param timeInSeconds
	 */
	
	public void convertFromSecond(long timeInSeconds){
		years=timeInSeconds/(365*30*24*3600);
		timeInSeconds-=years*365*30*24*3600;
		months=timeInSeconds/(30*24*3600);
		timeInSeconds-=months*30*24*3600;
		days=timeInSeconds/(24*3600);
		timeInSeconds-=days*24*3600;
		hours=timeInSeconds/3600;
		timeInSeconds-=hours*3600;
		minutes=timeInSeconds/60;
		timeInSeconds-=minutes*60;
		seconds=timeInSeconds;		
	}
	
	/**
	 * convert the time in seconds into format [year,month,day,hour,minute,second,millisecond]
	 * @param timeInMilliSecond
	 */
	public void convertFromMilliSecond(long timeInMilliSecond){
		long timeInSecond=timeInMilliSecond/1000;
		milliSeconds=timeInMilliSecond%1000;
		convertFromSecond(timeInSecond);
	}
		
	
	public void outputTimes(){
		System.out.println("year\tmonth\tday\thour\tminute\tsecond");
		System.out.println(Long.toString(years)+"\t"+Long.toString(months)+"\t"+
				Long.toString(days)+"\t"+Long.toString(hours)+"\t"+
				Long.toString(minutes)+"\t"+Long.toString(seconds));
	}
	
	
	
	public void outputFullTimes(){
		System.out.println("year\tmonth\tday\thour\tminute\tsecond\tmillis\tmicros");
		System.out.println(Long.toString(years)+"\t"+Long.toString(months)+"\t"+
				Long.toString(days)+"\t"+Long.toString(hours)+"\t"+
				Long.toString(minutes)+"\t"+Long.toString(seconds)+"\t"+
				Long.toString(milliSeconds));
	}

	
	static void test(){
		Date mydate=new Date();
		long begin=mydate.getTime();
		System.out.println(begin);
		
		for(long i=0;i<1000000000;i+=1);
		
		Date mydate2=new Date();
		long end=mydate2.getTime();
		System.out.println(end);
		
		long breakTime=end-begin;
		System.out.println(breakTime);		
		
		ConvertTimes newTime= new ConvertTimes();
		newTime.convertFromMilliSecond(breakTime);
		newTime.outputFullTimes();
	}
	
	static void oneExample(String[] args){
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();		
	}
}
