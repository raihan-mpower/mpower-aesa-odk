package org.odk.collect.android.activities;



import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;

import com.mpower.database.feedbackdatabase;
import com.mpower.model.Querydata;
import com.mpower.util.HttpRequest;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

public class poll_feedback_service extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		(new AsyncTask(){

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				String status_url = "http://ongeza.ap01.aws.af.cm/index.php/ongezacontroller/getfarmerquery";
				try {
					String result_data = HttpRequest.GetText(HttpRequest
							.getInputStreamForGetRequest(status_url));
					JSONArray jsonarray = new JSONArray(result_data);
					for(int i = 0;i<jsonarray.length();i++){
						Querydata qd = Querydata.create_obj_from_json(jsonarray.getJSONObject(i).toString());
						feedbackdatabase fdb = new feedbackdatabase(poll_feedback_service.this);
						fdb.checkandinsert(qd);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return null;
			}
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				feedbackdatabase fdb = new feedbackdatabase(poll_feedback_service.this);
				if(fdb.returnunshownmessages()>0){
					sendBroadcast(new Intent(poll_feedback_service.this, notificationreceiver.class));
					fdb.updateshown();
				}
				super.onPostExecute(result);
			}
			
		}).execute();
		return startId;
		
	};

}
