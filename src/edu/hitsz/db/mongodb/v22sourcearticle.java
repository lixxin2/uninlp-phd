package edu.hitsz.db.mongodb;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * 处理v22数据，属于mongdb数据库形式。
 * <p> bson文件首先转化为mongdb数据库
 * @author Xinxin Li
 * @since Aug 18, 2012
 */
public class v22sourcearticle {

	public static void main(String[] args) throws MongoException, IOException {
		
		Mongo m = new Mongo();
		
		for (String s : m.getDatabaseNames()) {
            System.out.println(s);
        }
		
		DB db = m.getDB( "v22source" );
		
		Set<String> colls = db.getCollectionNames();

		for (String s : colls) {
		    System.out.println(s);
		}
		
		DBCollection coll = db.getCollection("v22sourcearticle");
		
		System.out.println(coll.getCount());
		
		
		/*
		DBObject myDoc = coll.findOne();
		Object obj = myDoc.get("content");
		System.out.println(myDoc);
		System.out.println(obj);
		*/
		
		String dir = "/media/主分区/v22";
		
		//PrintWriter outwriter = new PrintWriter(
		//		new BufferedWriter(
		//		new FileWriter(dir + File.separator + "0")));
		
		FileWriter outwriter = new FileWriter(dir + File.separator + "0");
		
		int count=0;
		DBCursor cursor = coll.find();
        try {
        	
            while(cursor.hasNext()) {
            	
            	DBObject myDoc = cursor.next();
        		Object contentObj = myDoc.get("content");
        		//Object thumbnial_srcOjb = myDoc.get("thumbnail_src");
        		//Object titleObj = myDoc.get("title");
            	//Object urlObj = myDoc.get("url");
        		//System.out.println(myDoc);
        		//System.out.println(urlObj);
                //System.out.println(contentObj);
                //
                if(count%10000 == 0) {
                	System.out.println(count);
                	outwriter.close();
            		//outwriter = new PrintWriter(
            		//		new BufferedWriter(
            		//		new FileWriter(dir + File.separator + Integer.toString(count+10000)))); 
                	outwriter = new FileWriter(dir + File.separator + Integer.toString(count));
            	}
                outwriter.write(contentObj+"\n");
                count++;
            }
            outwriter.close();
        } finally {
            cursor.close();
        }
        
        

		
		
	}
	
}
