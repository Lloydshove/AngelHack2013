package com.hack.gspot;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class DataManager {

	public static void initializeDbWithFlickrData(String path) {
		File flickrDataStorage = new File(path);
		if(!flickrDataStorage.exists() || !flickrDataStorage.isDirectory())
			throw new RuntimeException("This is not a folder or it's empty");
		
		MongoClient mongoClient = MongoDbConnector.getInstance();
		DB db = mongoClient.getDB("test");
		DBCollection collection = db.getCollection("fdata");
		for (File file: flickrDataStorage.listFiles()) {
			Reader reader = null;
			List<String> dataList = null;
			try{
				reader = new FileReader(file);
				dataList = IOUtils.readLines(reader);
			}
			catch(Exception e){
				System.out.println("Error reading file " + file.getName() + ". Skipping...");
				e.printStackTrace();
				continue;
			}
			finally { IOUtils.closeQuietly(reader); }
			
			List<DBObject> dbObjectList = new ArrayList<DBObject>(dataList.size());
			for (String data : dataList) {
				dbObjectList.add((DBObject) JSON.parse(data));
			}
			
			collection.insert(dbObjectList);
		}
		collection.ensureIndex((DBObject) JSON.parse("{\"loc\":\"2dsphere\"}"));
	}
	
	public static void main(String[] args) {
		initializeDbWithFlickrData("resources");
	}
	
}
