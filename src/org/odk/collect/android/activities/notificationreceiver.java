package org.odk.collect.android.activities;



import org.odk.collect.android.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.sax.StartElementListener;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class notificationreceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		// intent triggered, you can add other intent for other actions
		Intent intent = new Intent(context, farmerquery_feedback_list.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		// this is it, we'll build the notification!
		// in the addAction method, if you don't want any icon, just set the first param to 0
		Notification mNotification = new NotificationCompat.Builder(context)
			
			.setContentTitle("new update!")
			.setContentText("check for feedback in farmer query")
			.setSmallIcon(R.drawable.mpowerlogo)
			.setContentIntent(pIntent)
			.setSound(soundUri)
			
			.addAction(R.drawable.mpowerlogo, "View", pIntent)
			.addAction(0, "Remind", pIntent)
			
			.build();
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
//		(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

		// If you want to hide the notification after it was selected, do the code below
		// myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(0, mNotification);
//		context.startActivity(new Intent(context, MyService.class));
	}

}
