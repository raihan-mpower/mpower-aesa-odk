package com.mpower.database;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;

import com.mpower.model.Querydata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class feedbackdatabase extends SQLiteOpenHelper {
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "mpower_database";

	// Contacts table name
	private static final String TABLE = "querydata";

	// Contacts Table Columns names
	String  farmerID = "farmerID";
	String  farmerName = "farmerName";
	String  queryID = "queryID";
	String  comment = "comment";
	String  datetime = "datetime";
	String  problemtype = "problemtype";
	String  problemcrop = "problemcrop";
	String  farmerPhone = "farmerPhone";
	String  farmeraddress = "farmeraddress";
	String  seen = "seen";
	String shown = "shown";



	public feedbackdatabase(Context context) {
		super(context,DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
				+ queryID + " INTEGER PRIMARY KEY," + farmerName + " TEXT,"+ farmerPhone + " TEXT,"+ farmeraddress + " TEXT,"+ farmerID + " TEXT,"
				+ comment + " TEXT," +datetime + " TEXT," +problemtype + " TEXT," +seen + " TEXT," +problemcrop + " TEXT,"+shown + " TEXT)";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	//	addquery()
	// Adding new contact
	public void addQuerydata(Querydata qd) {
//		String  farmerID = "farmerID";
//		String  farmerName = "farmerName";
//		String  queryID = "queryID";
//		String  comment = "comment";
//		String  datetime = "datetime";
//		String  problemtype = "problemtype";
//		String  problemcrop = "problemcrop";
//		String  farmerPhone = "farmerPhone";
//		String  farmeraddress = "farmeraddress";

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(farmerID, qd.getfarmerID()); // Contact Name
		values.put(farmerName, qd.getfarmerName());
		values.put(queryID, qd.getqueryID());
		values.put(comment, qd.getcomment());
		values.put(datetime, qd.getdatetime());
		values.put(problemtype, qd.getproblemtype());
		values.put(problemcrop, qd.getproblemcrop());
		values.put(farmerPhone, qd.getfarmerPhone());
		values.put(farmeraddress, qd.getfarmeraddress());
		values.put(seen, ""+qd.isSeen());
		values.put(shown, "false");
		// Inserting Row
		db.insert(TABLE, null, values);
		db.close(); // Closing database connection
	}
	public int updateseen(Querydata qd) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(seen, "true");
//	    values.put(KEY_PH_NO, contact.getPhoneNumber());
	 
	    // updating row
	    return db.update(TABLE, values, queryID + " = ?",
	            new String[] { String.valueOf(qd.getqueryID()) });
	}
	public int updateshown() {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(shown, "true");
//	    values.put(KEY_PH_NO, contact.getPhoneNumber());
	 
	    // updating row
	    return db.update(TABLE, values, shown + " = ?",
	            new String[] { "false" });
	}
	  public List<Querydata> getAllquerydata() {
		  
		  
//					+ queryID + " INTEGER PRIMARY KEY," + farmerName + 
//					" TEXT,"+ farmerPhone + " TEXT,"+ farmeraddress + 
//					" TEXT,"+ farmerID + " TEXT,"
//					+ comment + " TEXT," +datetime + " TEXT,"
//					+problemtype + " TEXT," +seen + " TEXT," +problemcrop + " TEXT)";
//		  
		  
	        List<Querydata> qdList = new ArrayList<Querydata>();
	        // Select All Query
	        String selectQuery = "SELECT  * FROM " + TABLE;
	 
	        SQLiteDatabase db = this.getWritableDatabase();
	        Cursor cursor = db.rawQuery(selectQuery, null);
	 
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	Querydata qd = new Querydata();
	            	qd.setqueryID(cursor.getString(0));
	            	qd.setfarmerName(cursor.getString(1));
	            	qd.setfarmerPhone(cursor.getString(2));
	            	qd.setfarmeraddress(cursor.getString(3));
	            	qd.setfarmerID(cursor.getString(4));
	            	qd.setcomment(cursor.getString(5));
	            	qd.setdatetime(cursor.getString(6));
	            	qd.setproblemtype(cursor.getString(7));
	            	qd.setSeen(Boolean.parseBoolean(cursor.getString(8)));
	            	qd.setproblemcrop(cursor.getString(9));
//	                contact.setID(Integer.parseInt(cursor.getString(0)));
//	                contact.setName(cursor.getString(1));
//	                contact.setPhoneNumber(cursor.getString(2));
	                // Adding contact to list
	                qdList.add(qd);
	            } while (cursor.moveToNext());
	        }
	        try{
	        	cursor.close();
	        }catch(Exception e){
	        }
	        // return contact list
	        return qdList;
	    }
	  public int updatecomments(Querydata qd) {
		    SQLiteDatabase db = this.getWritableDatabase();
		 
		    ContentValues values = new ContentValues();
		    values.put(comment, qd.getcomment());
//		    values.put(KEY_PH_NO, contact.getPhoneNumber());
		 
		    // updating row
		    return db.update(TABLE, values, queryID + " = ?",
		            new String[] { String.valueOf(qd.getqueryID()) });
		}
	  public void checkandinsert(Querydata qdnew){
		 List <Querydata> qdlist = getAllquerydata();
		 if(querydata_is_in_list(qdlist, qdnew)){
			 if(querydata_comment_is_in_list(qdlist, qdnew)){
				 updatecomments(qdnew);
			 }
		 }else{
			 addQuerydata(qdnew);
		 }
	  }
	  public boolean querydata_is_in_list(List<Querydata> qdlist,Querydata qdnew){
		  for(int i = 0;i<qdlist.size();i++){
			  if(qdnew.getqueryID().equalsIgnoreCase(qdlist.get(i).getqueryID())){
				  return true;
			  }
		  }
		return false;
		  
	  }
	  public boolean querydata_comment_is_in_list(List<Querydata> qdlist,Querydata qdnew){
		  for(int i = 0;i<qdlist.size();i++){
			  if(qdnew.getqueryID().equalsIgnoreCase(qdlist.get(i).getqueryID())){
				 if( qdnew.getcomment().equalsIgnoreCase(qdlist.get(i).getcomment())){
					  return true;
				  }
			  }
		  }
		return false;
		  
	  }
	  public int returnunseenmessages(){
		  int count = 0;
		  List <Querydata> qdlist = getAllquerydata();
		  for(int i = 0;i<qdlist.size();i++){
			  if(qdlist.get(i).isSeen()){
				  
			  }else{
				  count++;
			  }
		  }
		  return count;
	  }
	  public int returnunshownmessages(){
		  int count = 0;
		  String selectQuery = "SELECT  * FROM " + TABLE;
			 
	        SQLiteDatabase db = this.getWritableDatabase();
	        Cursor cursor = db.rawQuery(selectQuery, null);
	 
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	if(cursor.getString(10).equalsIgnoreCase("false")){
	            		count++;
	            	}
//	                contact.setID(Integer.parseInt(cursor.getString(0)));
//	                contact.setName(cursor.getString(1));
//	                contact.setPhoneNumber(cursor.getString(2));
	                // Adding contact to list
	                
	            } while (cursor.moveToNext());
	        }
	 
		  return count;
	  }

}
