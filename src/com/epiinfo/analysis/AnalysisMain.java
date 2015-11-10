package com.epiinfo.analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.EpiDbHelper;
import com.epiinfo.droid.FormMetadata;
import com.epiinfo.droid.R;
import com.epiinfo.unc.Constants;
import com.epiinfo.unc.UncEpiSettings;

public class AnalysisMain extends Activity {

	private EpiDbHelper dbHelper;
	
	private void LoadActivity(Class c)
	{
		startActivity(new Intent(this, c));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuItem mnuSearch = menu.add(0, 0, 0, "Add Frequency Gadget");
        mnuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        MenuItem mnuInsert = menu.add(0, 1,1, "Add Means Gadget");
        mnuInsert.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                
        MenuItem mnuSync = menu.add(0, 2,2, "Add 2x2 Gadget");
        mnuSync.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

		PrepareCanvas();
		
        switch(item.getItemId()) {
        case 0:
            AddFrequencyGadget();
            GoToBottom();
            return true;
        case 1:
        	AddMeansGadget();
        	GoToBottom();
        	return true;
        case 2:
        	Add2x2Gadget();
        	GoToBottom();        	
        	return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
	
	private void GoToBottom()
	{
		final ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFCBD1DF);
    	scroller.post(new Runnable() {
    	    @Override
    	    public void run() {
    	        scroller.fullScroll(ScrollView.FOCUS_DOWN);
    	    }
    	});
	}
	
	private void PrepareCanvas()
	{
		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		layout.setVisibility(View.VISIBLE);
		
		LinearLayout logo = (LinearLayout) findViewById(R.id.analysis_logo);
		logo.setVisibility(View.GONE);
		
		ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFCBD1DF);
	}
	
	private void AddMeansGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	
		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		MeansView gadget = new MeansView(this, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}
	
	private void Add2x2Gadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	
		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		TwoByTwoView gadget = new TwoByTwoView(this, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}
	
	private void AddFrequencyGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	
		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		FrequencyView gadget = new FrequencyView(this, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DeviceManager.SetOrientation(this);
		this.setTheme(android.R.style.Theme_Holo_Light);
		
		setContentView(R.layout.analysis);
		
		Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
        	String viewName = extras.getString("ViewName");
        	
        	// v0.9.59
        	Log.d(Constants.LOGTAG, " " + "ViewName = " + viewName + " *********************");
        	// FormMetadata view = new FormMetadata("EpiInfo/Questionnaires/"+ viewName +".xml");
        	FormMetadata view = new FormMetadata("EpiInfo/SyncFiles/"+ viewName +".xml");
        	
        	dbHelper = new EpiDbHelper(this, viewName);
        	dbHelper.open();

        }
        
        ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFFFFFFF);
		
		LinearLayout logo = (LinearLayout) findViewById(R.id.analysis_logo);
		logo.setBackgroundColor(0xFFFFFFFF);
        
	}


}