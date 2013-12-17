package org.odk.collect.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class alarm_invoked_broadcastreceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		context.startService(new Intent(context, poll_feedback_service.class));
	}

}
