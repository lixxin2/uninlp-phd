package edu.hitsz.web.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class WgetWebCrawler {

	/**
	 * 抓包，
	 * @param url
	 * @param deep
	 * @param outDir
	 */
	public static void crawl(String url, int deep, String outDir){
		String webCmd = "wget -r -F -l "+deep+" "+url+" -P "+outDir;
		Process process;
		try{
			System.out.println("exec: "+webCmd);
			process = Runtime.getRuntime().exec(webCmd);
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String str = null;
			while ((str = read.readLine()) != null) {
				System.out.println(str);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void crawl(ArrayList<String> urls, int deep, String outDir){
		for(int i=0; i<urls.size(); i++)
			crawl(urls.get(i), deep, outDir);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WgetWebCrawler.crawl("http://news.sohu.com", 0, "/home/tm/Downloads/web");
	}

}
