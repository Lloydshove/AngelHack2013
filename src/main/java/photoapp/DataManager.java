package photoapp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class DataManager {
	
	private final static int NUMBER_OF_SPOTS = 5;
	private final static int NUMBER_OF_PHOTOS = 10;
	private final static int SPOT_STEP = 10;

	private final static String GEO_QUERY = "{\"loc\":{\"$geoWithin\":{\"$box\":[[%f,%f],[%f,%f]]}}}";

	private static DBCollection getCollection(){
		MongoClient mongoClient = MongoDbConnector.getInstance();
		DB db = mongoClient.getDB("test");
		return db.getCollection("fdata");
	}
	
	public static void initializeDbWithFlickrData(String path) {
		File flickrDataStorage = new File(path);
		if(!flickrDataStorage.exists() || !flickrDataStorage.isDirectory())
			throw new RuntimeException("This is not a folder or it's empty");
		
		DBCollection collection = getCollection();
		for (File file: flickrDataStorage.listFiles((FileFilter) new SuffixFileFilter("json", IOCase.INSENSITIVE))) {
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
		collection.ensureIndex((DBObject) JSON.parse("{\"id\":1}"), (DBObject) JSON.parse("{\"unique\":true,\"dropDups\":true}"));
		collection.ensureIndex((DBObject) JSON.parse("{\"loc\":\"2dsphere\"}"));
	}
	
	public static List<Spot> getSpots(double[][] coords) {
        System.out.println(new Date());
		DBCollection collection = getCollection();
		double west = coords[0][0];
		double south = coords[0][1];
		double east = coords[1][0];
		double north = coords[1][1];
		double xstep = (east - west)/SPOT_STEP;
		double ystep = (north - south)/SPOT_STEP;
		List<Spot> spotList = new ArrayList<Spot>();
		
		for(int i=0; i<SPOT_STEP; i++){
			for(int j=0; j<SPOT_STEP; j++){
				double westNew = ((((east - west)/SPOT_STEP) * j) + west);
				double southNew = ((((north - south)/SPOT_STEP) * i) + south);
				double eastNew = westNew + xstep;
				double northNew = southNew + ystep;

				DBCursor cursor = collection.find((DBObject) JSON.parse(
						String.format(GEO_QUERY, westNew, southNew, eastNew, northNew)));

				int count = cursor.count();

				String photos = cursor.limit(NUMBER_OF_PHOTOS).toArray().toString();
				if(count > 0)
					spotList.add(new Spot(((eastNew-westNew)/2)+westNew, ((northNew-southNew)/2)+southNew, count, photos));
			}
		}
		
		if(!spotList.isEmpty()){
			Collections.sort(spotList);
		}

        System.out.println(new Date());
		return spotList.size() > NUMBER_OF_SPOTS ? spotList.subList(0, NUMBER_OF_SPOTS) : spotList;
	}
		
	public static void main(String[] args) {
		double[][] ds = new double[][]{{103.804396,1.234918}, {103.859327,1.265724}}; // Sentosa
		initializeDbWithFlickrData("./generated_data");
		System.out.println(getSpots(ds));
	}
	
}
