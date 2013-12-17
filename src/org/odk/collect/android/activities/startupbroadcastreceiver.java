package org.odk.collect.android.activities;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class startupbroadcastreceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent in) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context, alarm_invoked_broadcastreceiver.class);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 234324243, intent, 0);
	    AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
	    alarmManager.cancel(pendingIntent);
	    //alarmManager.setTime(SystemClock.currentThreadTimeMillis());
	    Log.v("fantom", ""+ System.currentTimeMillis());
	 
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (3000),
       		 (5*1000), pendingIntent); 	
	}

}
