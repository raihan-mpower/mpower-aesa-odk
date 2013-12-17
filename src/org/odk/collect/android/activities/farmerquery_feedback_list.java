package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mpower.database.feedbackdatabase;
import com.mpower.model.Querydata;
import com.mpower.util.HttpRequest;

import de.robert_heim.animation.ActivitySwitcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class farmerquery_feedback_list extends SherlockActivity implements OnQueryTextListener {
	private PullToRefreshListView mPullRefreshListView;
	refereshlistadapter rfa;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.refreshlist);
		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar2));
		getSupportActionBar().setTitle("");
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		//	mPullRefreshListView.set
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});
		rfa = new refereshlistadapter(this);
		mPullRefreshListView.setAdapter(rfa);
		rfa.notifyDataSetChanged();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean isLight = true;
//			SampleList.THEME == R.style.Theme_Sherlock_Light;
		LayoutInflater inflater = (LayoutInflater)this.getSystemService
				(Context.LAYOUT_INFLATER_SERVICE);
	
		View view = inflater.inflate(R.layout.iconbadge,null);
		TextView txt = (TextView)(view.findViewById(R.id.textOne));

		feedbackdatabase fdb = new feedbackdatabase(this);
		txt.setText(""+fdb.returnunseenmessages());
		menu.add(0, 0, 0, "notification").setActionView(view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
//		 menu.add("settings")
//         .setIcon(isLight ? R.drawable.ic_menu_manage : R.drawable.ic_menu_manage)
//         .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//		this.getMenuInflater().inflate(R.menu.main, menu);
//		menu.
//		MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = new SearchView(this);
        menu.add("Search")
        .setIcon(isLight ? R.drawable.abs__ic_search : R.drawable.abs__ic_search)
        .setActionView(mSearchView)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		mSearchView.setOnQueryTextListener(this);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case 0:
//			Collect.getInstance()
//					.getActivityLogger()
//					.logAction(this, "onOptionsItemSelected",
//							"MENU_PREFERENCES");
//			Intent ig = new Intent(this, PreferencesActivity.class);
//			startActivity(ig);
//			return true;
//		
//			
//		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void overridePendingTransition(int enterAnim, int exitAnim) {
		// TODO Auto-generated method stub
		super.overridePendingTransition(0, 0);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		ActivitySwitcher.animationIn(findViewById(R.id.refreshcontainer), getWindowManager());
		System.gc();
		super.onResume();
		invalidateOptionsMenu();
	}
	class refereshlistadapter extends BaseAdapter{
		String category = "";
		public List <Querydata> querydatas;
		private static final String IMAGE_CACHE_DIR = "listthumb";

		public refereshlistadapter(Context context) {
			feedbackdatabase fdb = new feedbackdatabase(context);
			querydatas = fdb.getAllquerydata();



			//			final DisplayMetrics displayMetrics = new DisplayMetrics();
			//			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			//			final int height = 150;
			//			final int width = 150;
			//			final int longest = (height > width ? height : width);


		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return querydatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(final int position, View convertView, ViewGroup arg2) 
		{
			View v = null;
			//
			//		if (convertView == null) 
			//		{

			if(!querydatas.get((querydatas.size()-1) -position).isSeen()){
				final LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.unseenlistviewrow, null);
				TextView titletext = (TextView)v.findViewById(R.id.titletext);
				titletext.setText(querydatas.get((querydatas.size()-1) -position).getproblemtype());
				TextView additionaltext = (TextView)v.findViewById(R.id.additionaltext);
				additionaltext.setText(querydatas.get((querydatas.size()-1) -position).getproblemcrop());
			}else{
				final LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.seenlistviewrow, null);
				TextView titletext = (TextView)v.findViewById(R.id.titletext);
				titletext.setText(querydatas.get((querydatas.size()-1) -position).getproblemtype());
				TextView additionaltext = (TextView)v.findViewById(R.id.additionaltext);
				additionaltext.setText(querydatas.get((querydatas.size()-1) -position).getproblemcrop());
	
			}
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					feedbackdatabase fdb = new feedbackdatabase(farmerquery_feedback_list.this);
					fdb.updateseen(querydatas.get((querydatas.size()-1) -position));
					querydatas = fdb.getAllquerydata();
					showdialog_details(querydatas.get((querydatas.size()-1) -position));
					notifyDataSetChanged();
					invalidateOptionsMenu();
				}

				
			});
			//		}
			//		else
			//		{
			//			v = convertView;
			//		}




			return v;
		}

	}
	private void showdialog_details(Querydata querydata) {
		Dialog dialog = new Dialog(farmerquery_feedback_list.this);
		// TODO Auto-generated method stub
//		dialog .setTitle("");

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.feedback_details_dialog);
    	dialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
//    	dialog.getWindow().set
    	dialog.show();
	}
	private class GetDataTask extends AsyncTask{



		//	@Override
		//	protected void onPostExecute(String[] result) {
		//		
		//
		//		// Call onRefreshComplete when the list has been refreshed.
		//		mPullRefreshListView.onRefreshComplete();
		//
		//		super.onPostExecute(result);
		//	}
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			rfa.querydatas = (new feedbackdatabase(farmerquery_feedback_list.this)).getAllquerydata();
			rfa.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();
			invalidateOptionsMenu();
			super.onPostExecute(result);
		}
		@Override
		protected Object doInBackground(Object... arg0) {
			// TODO Auto-generated method stub

			String status_url = "http://ongeza.ap01.aws.af.cm/index.php/ongezacontroller/getfarmerquery";
			try {
				String result_data = HttpRequest.GetText(HttpRequest
						.getInputStreamForGetRequest(status_url));
				JSONArray jsonarray = new JSONArray(result_data);
				for(int i = 0;i<jsonarray.length();i++){
					Querydata qd = Querydata.create_obj_from_json(jsonarray.getJSONObject(i).toString());
					feedbackdatabase fdb = new feedbackdatabase(farmerquery_feedback_list.this);
					fdb.checkandinsert(qd);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			return null;
		}
	}
	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		if(!(query.equals(null)||query.equalsIgnoreCase(""))){
		List <Querydata> searchqd = new ArrayList<Querydata>();
		for(int i = 0;i<rfa.querydatas.size();i++){
			if(rfa.querydatas.get(i).getfarmerName().contains(query)
				||	rfa.querydatas.get(i).getfarmerPhone().contains(query)
				||	rfa.querydatas.get(i).getfarmeraddress().contains(query)
				||	rfa.querydatas.get(i).getfarmerID().contains(query)
				||	rfa.querydatas.get(i).getproblemcrop().contains(query)
				||	rfa.querydatas.get(i).getproblemtype().contains(query)
				||	rfa.querydatas.get(i).getqueryID().contains(query)
				||  query.contains(rfa.querydatas.get(i).getfarmerName())
				||	query.contains(rfa.querydatas.get(i).getfarmerPhone())
				||	query.contains(rfa.querydatas.get(i).getfarmeraddress())
				||	query.contains(rfa.querydatas.get(i).getfarmerID())
				||	query.contains(rfa.querydatas.get(i).getproblemcrop())
				||	query.contains(rfa.querydatas.get(i).getproblemtype())
				||	query.contains(rfa.querydatas.get(i).getqueryID())
					){
				searchqd.add(rfa.querydatas.get(i));
			}
			
		}
		rfa.querydatas = searchqd;
		rfa.notifyDataSetChanged();
		}else{
			feedbackdatabase fdb = new feedbackdatabase(this);
			rfa.querydatas = fdb.getAllquerydata();
			rfa.notifyDataSetChanged();
		}
		return false;
	}
	@Override
	public boolean onQueryTextChange(String query) {
		// TODO Auto-generated method stub
		if(!(query.equals(null)||query.equalsIgnoreCase(""))){
		List <Querydata> searchqd = new ArrayList<Querydata>();
		for(int i = 0;i<rfa.querydatas.size();i++){
			if(rfa.querydatas.get(i).getfarmerName().contains(query)
				||	rfa.querydatas.get(i).getfarmerPhone().contains(query)
				||	rfa.querydatas.get(i).getfarmeraddress().contains(query)
				||	rfa.querydatas.get(i).getfarmerID().contains(query)
				||	rfa.querydatas.get(i).getproblemcrop().contains(query)
				||	rfa.querydatas.get(i).getproblemtype().contains(query)
				||	rfa.querydatas.get(i).getqueryID().contains(query)
				||  query.contains(rfa.querydatas.get(i).getfarmerName())
				||	query.contains(rfa.querydatas.get(i).getfarmerPhone())
				||	query.contains(rfa.querydatas.get(i).getfarmeraddress())
				||	query.contains(rfa.querydatas.get(i).getfarmerID())
				||	query.contains(rfa.querydatas.get(i).getproblemcrop())
				||	query.contains(rfa.querydatas.get(i).getproblemtype())
				||	query.contains(rfa.querydatas.get(i).getqueryID())
					){
				searchqd.add(rfa.querydatas.get(i));
			}
			
		}
		rfa.querydatas = searchqd;
		rfa.notifyDataSetChanged();
		}else{
			feedbackdatabase fdb = new feedbackdatabase(this);
			rfa.querydatas = fdb.getAllquerydata();
			rfa.notifyDataSetChanged();
		}
		return false;
	}
}
