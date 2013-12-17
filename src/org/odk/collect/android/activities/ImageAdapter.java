package org.odk.collect.android.activities;



import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import de.robert_heim.animation.ActivitySwitcher;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class ImageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private static final int[] ids = { };
	Context con;
	public ImageAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		con = context;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_first, null);

			Button next = (Button)convertView.findViewById(R.id.nextpanelviewflow);
			next.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					MainMenuActivity.viewFlow.snapToScreen(1);

				}
			});
			Button sendformdata = (Button)convertView.findViewById(R.id.sendformdata);
			sendformdata.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Collect.getInstance().getActivityLogger()
					.logAction(this, "uploadForms", "click");
					Intent i = new Intent(con,
							InstanceUploaderList.class);
					con.startActivity(i);
				}
			});
			Button reviewformdata = (Button)convertView.findViewById(R.id.Reviewformdata);
			reviewformdata.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Collect.getInstance().getActivityLogger()
					.logAction(this, "editSavedForm", "click");
					Intent i = new Intent(con,
							InstanceChooserList.class);
					con.startActivity(i);
				}
			});
			Button farmerquerybutton = (Button)convertView.findViewById(R.id.farmerquerybutton);
			farmerquerybutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					callformactivity("Farmer Query System",con);
		
				}
			});
			
			
			Button saooquerybutton = (Button)convertView.findViewById(R.id.saooreportingform);
			saooquerybutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					callformactivity("SAOO Reporting Form",con);
					
				}
			});
			Button fieldchallenge = (Button)convertView.findViewById(R.id.fieldchallengebutton);
			fieldchallenge.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					callformactivity("Field Challenge Gathering",con);
					
				}
			});
		}
		if(position == 1){
			convertView = mInflater.inflate(R.layout.layout_second, null);
		}
		//		((ImageView) convertView.findViewById(R.id.imgView)).setImageResource(ids[position]);
		return convertView;
	}
	public void callformactivity(String formname,Context con){
						long idFormsTable = 3;
				String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
				Cursor c = ((Activity) con).managedQuery(FormsColumns.CONTENT_URI, null, null, null, sortOrder);
				c.moveToFirst();
				while (c.isAfterLast() == false) 
				{
					for(int i = 0;i < c.getColumnNames().length;i++){
						if(c.getColumnName(i).equalsIgnoreCase("displayName")){
							if(c.getString(i).equalsIgnoreCase(formname)){
							c.moveToLast();
							}
						}
						if(c.getColumnName(i).equalsIgnoreCase("_id")){
							idFormsTable =  Long.parseLong(c.getString(i));
						}
					}
					c.moveToNext();
				}
				Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);
				Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", formUri.toString());
				String action = ((Activity) con).getIntent().getAction();
				if (Intent.ACTION_PICK.equals(action)) {
					// caller is waiting on a picked form
					((Activity) con).setResult(Activity.RESULT_OK, new Intent().setData(formUri));
				} else {
					// caller wants to view/edit a form, so launch formentryactivity
					animatedStartActivity(new Intent(Intent.ACTION_EDIT, formUri));
				}
//				((Activity) con).finish();
			
	}
	private void animatedStartActivity(final Intent intent) {
		// we only animateOut this activity here.
		// The new activity will animateIn from its onResume() - be sure to implement it.
//				final Intent intent = new Intent(con, Activity2.class);
//		 disable default animation for new intent
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				ActivitySwitcher.animationOut(((Activity) con).findViewById(R.id.container), ((Activity) con).getWindowManager(), new ActivitySwitcher.AnimationFinishedListener() {
					@Override
					public void onAnimationFinished() {
						System.gc();
						con.startActivity(intent);
					}
				});
	}

}
