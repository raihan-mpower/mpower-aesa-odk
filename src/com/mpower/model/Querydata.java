package com.mpower.model;
import org.json.JSONException;

import org.json.JSONObject;
public class Querydata { 
	String  farmerID;
	String  farmerName;
	String  queryID;
	String  comment;
	String  datetime;
	String  problemtype;
	String  problemcrop;
	String  farmerPhone;
	String  farmeraddress;
	boolean seen;
	public boolean isSeen() {
		return seen;
	}
	public void setSeen(boolean seen) {
		this.seen = seen;
	}
	public String getfarmerID() { 

		return farmerID;

	}
	public void setfarmerID(String farmerID) {

		this.farmerID = farmerID;

	}
	public String getfarmerName() { 

		return farmerName;

	}
	public void setfarmerName(String farmerName) {

		this.farmerName = farmerName;

	}
	public String getqueryID() { 

		return queryID;

	}
	public void setqueryID(String queryID) {

		this.queryID = queryID;

	}
	public String getcomment() { 

		return comment;

	}
	public void setcomment(String comment) {

		this.comment = comment;

	}
	public String getdatetime() { 

		return datetime;

	}
	public void setdatetime(String datetime) {

		this.datetime = datetime;

	}
	public String getproblemtype() { 

		return problemtype;

	}
	public void setproblemtype(String problemtype) {

		this.problemtype = problemtype;

	}
	public String getproblemcrop() { 

		return problemcrop;

	}
	public void setproblemcrop(String problemcrop) {

		this.problemcrop = problemcrop;

	}
	public String getfarmerPhone() { 

		return farmerPhone;

	}
	public void setfarmerPhone(String farmerPhone) {

		this.farmerPhone = farmerPhone;

	}
	public String getfarmeraddress() { 

		return farmeraddress;

	}
	public void setfarmeraddress(String farmeraddress) {

		this.farmeraddress = farmeraddress;

	}
	public static Querydata create_obj_from_json(String json){

		JSONObject jobject = null;

		Querydata querydata = new Querydata();

		try {

			jobject = new JSONObject(json);
			querydata.setfarmerID(jobject.getString("farmerID"));
			querydata.setfarmerName(jobject.getString("farmerName"));
			querydata.setqueryID(jobject.getString("queryID"));
			querydata.setcomment(jobject.getString("comment"));
			querydata.setdatetime(jobject.getString("datetime"));
			querydata.setproblemtype(jobject.getString("problemtype"));
			querydata.setproblemcrop(jobject.getString("problemcrop"));
			querydata.setfarmerPhone(jobject.getString("farmerPhone"));
			querydata.setfarmeraddress(jobject.getString("farmeraddress"));
		} catch (JSONException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		return querydata;

	}





}
