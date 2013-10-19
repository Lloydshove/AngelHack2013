package photoapp;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;

public class MongoDbConnector {
	
	private static MongoClient mongoClient;
	
	private MongoDbConnector(){
	}
	
	public static MongoClient getInstance(){
		if(mongoClient == null){
			synchronized (MongoDbConnector.class) {
				try {
					mongoClient = new MongoClient( "localhost" , 27017 );
				}
				catch (UnknownHostException e) {
					System.out.println("Error creating db connection");
					e.printStackTrace();
				}
			}
		}
		return mongoClient;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(mongoClient != null){
			synchronized (mongoClient) {
				try{
					mongoClient.close();
				}
				catch(Exception e){
					System.out.println("Error closing connection");
				}
			}
		}
		super.finalize();
	}
}
