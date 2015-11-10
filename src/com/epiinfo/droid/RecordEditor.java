package com.epiinfo.droid;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.epiinfo.droid.R.drawable;
import com.epiinfo.interpreter.CheckCodeEngine;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.ICheckCodeHost;
import com.epiinfo.interpreter.IInterpreter;
import com.epiinfo.interpreter.Rule_Context;
import com.epiinfo.interpreter.VariableCollection;
import com.epiinfo.unc.Constants;
import com.epiinfo.unc.UncEpiSettings;
import com.google.zxing.integration.android.IntentIntegrator;

public class RecordEditor extends Activity implements ICheckCodeHost 
{
	
	private Long mRowId;
	private String globalRecordId;
	private Dialog locationDialog;
	private Dialog barcodeDialog;
	private FormLayoutManager layoutManager;
	private Spinner latSpinner;
	private Spinner longSpinner;
	private Spinner barSpinner;
	private EditText latField;
	private EditText longField;
	private EditText barField;
	private ImageView currentImageView;
	private String currentImageFileName;
	private RecordEditor self;
	private Activity parent;
	private ScrollView scroller;
	private Rule_Context Context;
	

    private void CreateFields(ViewGroup layout, Rule_Context pContext) {
    	
    	layoutManager = new FormLayoutManager(this, FormMetadata.Height, FormMetadata.Width, scroller, layout, pContext);
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	for (int x=0; x<layoutManager.GetImageFieldIds().size(); x++)
    	{
    		ImageView iv = (ImageView)findViewById(layoutManager.GetImageFieldIds().get(x));
    		if (iv.getTag() != null)
    		{
    			outState.putString("ImageFileName" + iv.getId(), (String)iv.getTag());
    		}
    	}
    	if (currentImageFileName != null)
    	{
    		outState.putString("CurrentImageFileName", currentImageFileName);
    	}
    	if (currentImageView != null)
    	{
    		outState.putInt("CurrentImageViewId", currentImageView.getId());
    	}
    	if (barField != null)
    	{
    		outState.putInt("CurrentBarcodeFieldId", barField.getId());
    	}
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle inState)
    {
    	super.onRestoreInstanceState(inState);
    	for (int x=0; x<layoutManager.GetImageFieldIds().size(); x++)
    	{
    		if (inState.containsKey("ImageFileName" + layoutManager.GetImageFieldIds().get(x)))
    		{
        		ImageView iv = (ImageView)findViewById(layoutManager.GetImageFieldIds().get(x));
        		SetImage(iv,inState.getString("ImageFileName" + layoutManager.GetImageFieldIds().get(x)));
    		}
    	}
    	if (inState.containsKey("CurrentImageFileName"))
    	{
    		currentImageFileName = inState.getString("CurrentImageFileName");
    	}
    	if (inState.containsKey("CurrentImageViewId"))
    	{
    		currentImageView = (ImageView)findViewById(inState.getInt("CurrentImageViewId"));
    	}
    	if (inState.containsKey("CurrentBarcodeFieldId"))
    	{
    		barField = (EditText)findViewById(inState.getInt("CurrentBarcodeFieldId"));
    	}
    }
    
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// v0.9.57
		UncEpiSettings.pointQuestionnaireSaved = false;
		
		// #UNC Start 22May2015
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// #UNC End
		// DeviceManager.SetOrientation(this);
		this.setTheme(android.R.style.Theme_Holo_Light);
		
		Log.d(Constants.LOGTAG, " " + "################# FormMetadata.Width = " + FormMetadata.Width);
		Log.d(Constants.LOGTAG, " " + "################# DeviceManager.isLargeTablet = " + DeviceManager.isLargeTablet);
		//if (FormMetadata.Width > 800 && !DeviceManager.isLargeTablet)
		//{
			Log.d(Constants.LOGTAG, " " + "################# RecordEditor use record_edit_alt");
			DeviceManager.isPhone = true;
			setContentView(R.layout.record_edit_alt);
		//}
		//else
		//{
			//Log.d(Constants.LOGTAG, " " + "################# RecordEditor use record_edit");
			//DeviceManager.isPhone = false;
			//setContentView(R.layout.record_edit);
		//}
		AppManager.Started(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.EditorLayout);
		
		scroller = (ScrollView) findViewById(R.id.EditorScroller);    
		scroller.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);     
		scroller.setFocusable(true);     
		scroller.setFocusableInTouchMode(true);     
		scroller.setOnTouchListener(new View.OnTouchListener() 
		{         
			@Override         
			public boolean onTouch(View v, MotionEvent event) 
			{             
				v.requestFocusFromTouch();             
				return false;         
			}     
		}); 
		
		//CheckCodeProcessor checkCodeProcessor = new CheckCodeProcessor(getAssets());
		//this.Context = new CheckCodeEngine(getAssets()).PreCompile(FormMetadata.CheckCode);
		this.Context = FormMetadata.Context;
		if(this.Context != null)
		{
			this.Context.CheckCodeInterface = this;
		}
		this.CreateFields(layout, this.Context);
		//checkCodeProcessor.SetLayoutManager(layoutManager);
		

		
		/*
		try
		{
			if (FormMetadata.CheckCode.contains("END-DEFINEVARIABLES"))
			{
				int index = FormMetadata.CheckCode.indexOf("DEFINEVARIABLES") + 15;
				String rest = FormMetadata.CheckCode.substring(index);
				int index2 = rest.indexOf("END-DEFINEVARIABLES");
				String varDefs = rest.substring(0, index2);// + 19);
				
				VariableCollection.Initialize(varDefs);
				//new CheckCodeProcessor(controlHelper).Execute(pageCheckCode + System.getProperty("line.separator"), "");
			}
			if (FormMetadata.CheckCode.contains("End-Page"))
			{
				int index = FormMetadata.CheckCode.indexOf("Page [");
				String rest = FormMetadata.CheckCode.substring(index);
				int index2 = rest.indexOf("End-Page");
				String pageCheckCode = rest.substring(0, index2 + 8);
				
				//checkCodeProcessor.Execute(pageCheckCode + System.getProperty("line.separator"), "Before");
			}
		}
		catch (Exception ex)
		{
			
		}*/
		
		mRowId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mRowId = extras.getLong(EpiDbHelper.KEY_ROWID);
			globalRecordId = extras.getString(EpiDbHelper.GUID);
			for (int x=0;x<FormMetadata.Fields.size();x++)
			{
				if (Constants.LOGS_ENABLED7) {
					Log.d(Constants.LOGTAG, " " + " onCreate() fieldName=" + FormMetadata.Fields.get(x).getName() + "  Type=" + FormMetadata.Fields.get(x).getType() + "&&&&&&&&&&&&");
				}	
					
				if (!FormMetadata.Fields.get(x).getType().equalsIgnoreCase("2") && !FormMetadata.Fields.get(x).getType().equalsIgnoreCase("21"))
   				{
					if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("5"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						double value = extras.getDouble(fieldName);
						TextView txt = (TextView)layout.findViewById(x);
						if (value == Double.POSITIVE_INFINITY)
						{
							txt.setText("");
						}
						else
						{
							if (value == Math.floor(value))
							{
								txt.setText(((int)value) + "");
							}
							else
							{
								txt.setText(value + "");
							}
						}
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("10"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						int rawValue = extras.getInt(fieldName);
						boolean value = (rawValue != 0);
						CheckBox chk = (CheckBox)layout.findViewById(x);
						chk.setChecked(value);
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("11") || FormMetadata.Fields.get(x).getType().equalsIgnoreCase("17") || FormMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						int value = extras.getInt(fieldName);
						Spinner spn = (Spinner)layout.findViewById(x);
						spn.setSelection(value);
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("12"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						int rawValue = extras.getInt(fieldName);
						
						LinearLayout step1 = (LinearLayout)layout.findViewById(x);
	    				RadioGroup step2 = (RadioGroup)step1.getChildAt(0);
						step2.check(rawValue);
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("14"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						String fileName = extras.getString(fieldName);
						ImageView iv = (ImageView)layout.findViewById(x);
						if (!fileName.equalsIgnoreCase(""))
						{
							SetImage(iv,fileName);
						}
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("7"))
					{
						View temp =  this.layoutManager.GetView(FormMetadata.Fields.get(x).getName());//layout.findViewById(FormMetadata.Fields.get(x).getId());
						TextView txt = (TextView)temp.findViewWithTag(FormMetadata.Fields.get(x).getName().toLowerCase());
						
						String value =  extras.getString(FormMetadata.Fields.get(x).getName());	
						if (Constants.LOGS_ENABLED7) {
							Log.d(Constants.LOGTAG, " " + " onCreate() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					    	Log.d(Constants.LOGTAG, " " + " TextView=" + txt.getText().toString() + "  value=" + value);
					    }
						txt.setText(value);
					}
					else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("13"))
					{
//
					}
					else
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						String value = extras.getString(fieldName);
						TextView txt = (TextView)layout.findViewById(x);
						txt.setText(value);
					}
   				}
			}
		}
		// #UNC - start v0.9.48
		else {
			// no params in intent bundle so it must be a record Create
			// find the Cluster and Survey fields, and insert the Cluster & Point values
			for (int x=0;x<FormMetadata.Fields.size();x++)
			{
				if (Constants.LOGS_ENABLED7) {
					Log.d(Constants.LOGTAG, " " + " onCreate() fieldName=" + FormMetadata.Fields.get(x).getName() + "  Type=" + FormMetadata.Fields.get(x).getType() + "&&&&&&&&&&&&");
				}	
					
				if (!FormMetadata.Fields.get(x).getType().equalsIgnoreCase("2") && !FormMetadata.Fields.get(x).getType().equalsIgnoreCase("21"))
   				{
					if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("5"))
					{
						String fieldName = FormMetadata.Fields.get(x).getName();
						if (fieldName.equals("Cluster") || (fieldName.equals("CLUSTER"))) {
							if (Constants.LOGS_ENABLED7) {
								Log.d(Constants.LOGTAG, " " + " onCreate() Found Cluster field " + "&&&&&&&&&&&&");
							}	
							TextView txt = (TextView)layout.findViewById(x);
							txt.setText(UncEpiSettings.selectedPointItem.getClusterId());
						}
						else if ((fieldName.equals("Survey")) || (fieldName.equals("SURVEY")) || 
								 (fieldName.equals("Point"))   || (fieldName.equals("POINT"))) {
							if (Constants.LOGS_ENABLED7) {
								Log.d(Constants.LOGTAG, " " + " onCreate() Found Survey field " + "&&&&&&&&&&&&");
							}	
							TextView txt = (TextView)layout.findViewById(x);
							txt.setText(UncEpiSettings.selectedPointItem.getPointId());
						}
					}
   				}
			}
			UncEpiSettings.selectedPointItem.name = "";  // reset
			
		}
		// #UNC - end
		
		EnterRule CheckCode = this.Context.GetCommand("level=record&event=before&identifier=");
		if(CheckCode != null)
		{
			CheckCode.Execute();
		}
		
		CheckCode = this.Context.GetCommand("level=page&event=before&identifier=" + FormMetadata.PageName[0]);
		if(CheckCode != null)
		{
			CheckCode.Execute();
		}
		
		

		
	}
	
	// #UNC Start 22May2015
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + "RecordEditor onConfigurationChanged");
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	// #UNC End
		
	@Override
    public void onRestart()
    {
    	super.onRestart();
    	AppManager.Started(this);
    }
	
    @Override
    public void onStop()
    {
    	AppManager.Closed(this);
    	super.onStop();
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem mnuSave = menu.add(5000, 9001, 1, R.string.menu_save);
        mnuSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mnuSave.setIcon(drawable.content_save);
        
        MenuItem mnuLocate = menu.add(5000, 9002, 0, R.string.menu_locate);
        mnuLocate.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mnuLocate.setIcon(drawable.location);
        
        MenuItem mnuBarcode = menu.add(5000, 9003, 2, R.string.menu_barcode);
        mnuBarcode.setIcon(drawable.barcode);
        
        return true;
    }		
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case 9001:
            save(); 
            return true;
        case 9002:
        	showDialog(5);
        	return true;
        case 9003:
        	showDialog(6);
        	return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
   
    
    
    private void save()
    {
		ViewGroup layout = (ViewGroup) findViewById(R.id.EditorLayout);

    	Bundle bundle = new Bundle();
		
    	for (int x=0;x<FormMetadata.Fields.size();x++)
		{
    		if (!FormMetadata.Fields.get(x).getType().equalsIgnoreCase("2") && !FormMetadata.Fields.get(x).getType().equalsIgnoreCase("21"))
			{
    			if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("5"))
    			{
    				if (((TextView)layout.findViewById(x)).getText().toString().equalsIgnoreCase(""))
    				{
    					//bundle.putDouble(EpiView.Fields.get(x).getName(), 0);
    					bundle.putDouble(FormMetadata.Fields.get(x).getName(), Double.POSITIVE_INFINITY);
    				}
    				else
    				{
    					bundle.putDouble(FormMetadata.Fields.get(x).getName(), Double.parseDouble(((TextView)layout.findViewById(x)).getText().toString()));
    				}
    			}
    			else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("10"))
    			{
    				bundle.putInt(FormMetadata.Fields.get(x).getName(), ((CheckBox)layout.findViewById(x)).isChecked() ? 1 : 0);
    			}
    			else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("11") || FormMetadata.Fields.get(x).getType().equalsIgnoreCase("17") || FormMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
    			{
    				bundle.putInt(FormMetadata.Fields.get(x).getName(), ((Spinner)layout.findViewById(x)).getSelectedItemPosition());
    			}
    			else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("12"))
    			{
    				LinearLayout step1 = (LinearLayout)layout.findViewById(x);
    				RadioGroup step2 = (RadioGroup)step1.getChildAt(0);
    				int step3 = step2.getCheckedRadioButtonId();
    				bundle.putInt(FormMetadata.Fields.get(x).getName(), step3);
    			}
    			else if (FormMetadata.Fields.get(x).getType().equalsIgnoreCase("14"))
    			{
    				if (((ImageView)layout.findViewById(x)).getTag() == null)
    				{
    					bundle.putString(FormMetadata.Fields.get(x).getName(), "");
    				}
    				else
    				{
    					bundle.putString(FormMetadata.Fields.get(x).getName(), ((ImageView)layout.findViewById(x)).getTag().toString());
    				}
    			}
    			else if(FormMetadata.Fields.get(x).getType().equalsIgnoreCase("7")) // dateField
    			{
    				
    				//String dateString = "2001/03/09"; 
    				View temp = this.layoutManager.GetView(FormMetadata.Fields.get(x).getName());
    				String dateString = ((EditText) temp.findViewWithTag(FormMetadata.Fields.get(x).getName().toLowerCase())).getText().toString();
    				bundle.putString(FormMetadata.Fields.get(x).getName(), dateString);
    			}
    			else if(FormMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
    			{
    					Object result = ((Spinner)layout).getSelectedItem();
    					if(result != null)
    					{
    						result = result.toString().split("-")[0];
    					}
    			}
    			else
    			{
    				bundle.putString(FormMetadata.Fields.get(x).getName(), ((TextView)layout.findViewById(x)).getText().toString());
    			}
			}
		}
    	if (mRowId != null)
    	{
    		bundle.putLong(EpiDbHelper.KEY_ROWID, mRowId);
    		bundle.putString(EpiDbHelper.GUID, globalRecordId);
    	}
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		UncEpiSettings.pointQuestionnaireSaved = true;
		finish();
    }

	public void Alert(String message)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message)       
    	.setCancelable(false)       
    	.setPositiveButton("OK", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) 
    		{                
    			dialog.cancel();           
    		}       
    		});
    	builder.create();
    	builder.show();
    }
	
	public void StartCamera(ImageView v)
	{
		currentImageView = v;
		//Intent i = new Intent(this, CameraPreview.class);
		//startActivityForResult(i,0);
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		currentImageFileName = String.format("/sdcard/Download/EpiInfo/Images/%d.jpg", System.currentTimeMillis());

		Uri fileName = Uri.fromFile(new File(currentImageFileName));
	    
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileName); // set the image file name

	    // start the image capture Intent
	    startActivityForResult(intent, 0);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    		
    		if (resultCode == RESULT_OK) {
    			new BitmapProcessor().execute(currentImageFileName, currentImageView);
            }  
    		
    }
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation

		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);
		if (width > height)
		{
			matrix.postRotate(90);
		}
		// recreate the new Bitmap

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;

		}
	
	private void SetImage(ImageView iv, String fileName)
	{
		new ImageDecoder().execute(fileName, iv);
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == 5)
		{
			return showLocationDialog();
		}
		if (id == 6)
		{
			return showBarcodeDialog();
		}
		else
		{
			return layoutManager.onCreateDialog(id);
		}
	}
	
	private Dialog showBarcodeDialog()
	{		
		barcodeDialog = new Dialog(this);
        barcodeDialog.setTitle("Barcode Settings");
        barcodeDialog.setContentView(R.layout.barcode_dialog);
        barcodeDialog.setCancelable(true);
        
        barSpinner = (Spinner) barcodeDialog.findViewById(R.id.cbxBarcodeField);
    	barSpinner.setPrompt("Please select a field that will receive the barcode value.");
    	
    	String[] stringValues = new String[FormMetadata.TextFields.size()];
    	for (int x=0;x<FormMetadata.TextFields.size();x++)
    	{
    		stringValues[x] = FormMetadata.TextFields.get(x).getId() + ":" + FormMetadata.TextFields.get(x).getName();
    	}
    	
    	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	barSpinner.setAdapter(adapter);        
    	self = this;
    	    	
    	Button btnSet = (Button) barcodeDialog.findViewById(R.id.btnSet);
    	btnSet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int barFieldId = Integer.parseInt(barSpinner.getSelectedItem().toString().split(":")[0]);
				barField = (EditText) findViewById(barFieldId);
				IntentIntegrator.initiateScan(self);
				barcodeDialog.dismiss();				
			}
		});
        
        return barcodeDialog;
	}
	
	public void DisplayPDF(String fileName)
	{
		File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File file = new File(filePath, "/EpiInfo/Questionnaires/" + fileName);
		
		if(file.exists())              
		{                 
			Uri path = Uri.fromFile(file);                  
			Intent pdfIntent = new Intent(Intent.ACTION_VIEW);                 
			pdfIntent.setDataAndType(path, "application/pdf");                 
			pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                  
			try                 
			{                     
				startActivity(pdfIntent);                 
			}                 
			catch(Exception e)                 
			{                     
				//
			}             
		} 
	}
	
	private Dialog showLocationDialog()
	{		
		locationDialog = new Dialog(this);
        locationDialog.setTitle("GPS Settings");
        locationDialog.setContentView(R.layout.loc_dialog);
        locationDialog.setCancelable(true);
        
        latSpinner = (Spinner) locationDialog.findViewById(R.id.cbxLatitude);
    	latSpinner.setPrompt("Please select a field that represents latitude");
    	
    	String[] stringValues = new String[FormMetadata.NumericFields.size()];
    	for (int x=0;x<FormMetadata.NumericFields.size();x++)
    	{
    		stringValues[x] = FormMetadata.NumericFields.get(x).getId() + ":" + FormMetadata.NumericFields.get(x).getName();
    	}
    	
    	ArrayAdapter<CharSequence> latAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
        latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	latSpinner.setAdapter(latAdapter);        
    	
    	longSpinner = (Spinner) locationDialog.findViewById(R.id.cbxLongitude);
    	longSpinner.setPrompt("Please select a field that represents longitude");
    	
    	ArrayAdapter<CharSequence> longAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
        longAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	longSpinner.setAdapter(longAdapter);
    	
    	Button btnSet = (Button) locationDialog.findViewById(R.id.btnSet);
    	btnSet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int latFieldId = Integer.parseInt(latSpinner.getSelectedItem().toString().split(":")[0]);
				int longFieldId = Integer.parseInt(longSpinner.getSelectedItem().toString().split(":")[0]);
				
				latField = (EditText) findViewById(latFieldId);
				longField = (EditText) findViewById(longFieldId);
				
				try
				{
				latField.setText(GeoLocation.CurrentLocation.getLatitude() + "");
				longField.setText(GeoLocation.CurrentLocation.getLongitude() + "");
				}
				catch (Exception ex)
				{
					Alert("Could not receive GPS signals to determine current position. Please ensure that you have unobstructed view of sky and try again.");
				}
				//showDialog(0);
				
				locationDialog.dismiss();
				
			}
		});
        
        return locationDialog;
	}

	@Override
	public boolean Register(IInterpreter enterInterpreter) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean IsExecutionEnabled() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean IsSuppressErrorsEnabled() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Assign(String pName, Object pValue) 
	{
        //if (String.IsNullOrEmpty(pName)) return false;

        boolean result = false;

        Field field = FormMetadata.GetFieldByName(pName);
		View temp = this.layoutManager.GetView(pName);
		temp = temp.findViewById(field.getId());
        

        if (!field.getType().equalsIgnoreCase("2") && !field.getType().equalsIgnoreCase("21"))
		{
			if (field.getType().equalsIgnoreCase("5"))
			{
				TextView txt = (TextView)temp;
				if(!pValue.toString().equalsIgnoreCase(""))
				{
					double value = 0.0f;
					
					try
					{
						value = Double.parseDouble(pValue.toString());
						if (value == Double.POSITIVE_INFINITY)
						{
							txt.setText("");
						}
						else
						{
							if (value == Math.floor(value))
							{
								txt.setText(((int)value) + "");
							}
							else
							{
								txt.setText(value + "");
							}
						}
					}
					catch(Exception ex)
					{
						// do nothing for now
						txt.setText("");
					}
				}
			}
			else if (field.getType().equalsIgnoreCase("10"))
			{
				CheckBox chk = (CheckBox) temp;
				if(pValue instanceof Boolean)
				{
					chk.setChecked((Boolean)pValue);
				}
				else if(pValue instanceof Number)
				{
					double rawValue = Double.parseDouble(pValue.toString());
					boolean value = (rawValue != 0.0f);
					chk.setChecked(value);
				}
				else
				{
					chk.setChecked(false);
				}
			}
			else if (field.getType().equalsIgnoreCase("11") || field.getType().equalsIgnoreCase("17") || field.getType().equalsIgnoreCase("19"))
			{
				Spinner spn = (Spinner)temp;
				try
				{
					int value = Integer.parseInt(pValue.toString());
					//Spinner spn = (Spinner)layout.findViewById(x);
					spn.setSelection(value);
				}
				catch(Exception ex)
				{
					// do nothing for now
					spn.setSelection(-1);
				}
			}
			else if (field.getType().equalsIgnoreCase("12"))
			{
				int rawValue = Integer.parseInt(pValue.toString());
				
				//LinearLayout step1 = (LinearLayout)layout.findViewById(x);
				LinearLayout step1 = (LinearLayout)temp;
				RadioGroup step2 = (RadioGroup)step1.getChildAt(0);
				step2.check(rawValue);
			}
			else if (field.getType().equalsIgnoreCase("14"))
			{
				String fileName = pValue.toString();
				//ImageView iv = (ImageView)layout.findViewById(x);
				ImageView iv = (ImageView)temp;
				if (!fileName.equalsIgnoreCase(""))
				{
					SetImage(iv,fileName);
				}
			}
			else
			{
				//String fieldName = field.getName();
				//String value = extras.getString(fieldName);
				//TextView txt = (TextView)layout.findViewById(x);
				TextView txt = (TextView)temp;
				txt.setText(pValue.toString());
			}
		}

        

        return result;
	}

	@Override
	public boolean Geocode(String address, String latName, String longName) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void AutoSearch(String[] pIdentifierList, String[] pDisplayList,
			boolean pAlwaysShow) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Clear(String[] pIdentifierList) 
	{
		for(int i = 0; i < pIdentifierList.length; i++)
		{
			String s  = pIdentifierList[i];
			this.Assign(s, null);
		}
		
	}

	@Override
	public void Dialog(String pTextPrompt, String pTitleText) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Dialog(String pTextPrompt, Object pVariable, String pListType,
			String pTitleText) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean Dialog(String text, String caption, String mask,
			String modifier, Object input) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object GetValue(String pName) 
	{
		Object result = null;
		
		Field field = FormMetadata.GetFieldByName(pName);
		View temp = this.layoutManager.GetView(pName);
		if(temp != null)
		{
			if (temp instanceof EditText)
			{
				
				result = ((EditText)temp).getText().toString();
				if(field.getType().equalsIgnoreCase("5") && !result.toString().equalsIgnoreCase("")) // numeric
				{

					try
					{
						double val = Double.parseDouble(((EditText)temp).getText().toString());
						result = val;
					}
					catch(Exception ex)
					{
						// do nothing for now
					}
				}
			}
			else if(temp instanceof CheckBox)
			{
				
				result = ((CheckBox)temp).isChecked();
			}
			else if(temp instanceof Spinner)
			{
				result = ((Spinner)temp).getSelectedItem();
				if(result != null)
				{
					
					if(field.getType().equalsIgnoreCase("19")) // separated by dash
					{
						result = result.toString().split("-")[0];
					}
					if(field.getType().equals("11"))
					{
						result = result.toString().equalsIgnoreCase("yes") ? true : false;
					}
				}
			}
			else if(field.getType().equalsIgnoreCase("7")) // dateField
			{
				
				//String dateString = "2001/03/09"; 
				temp =  this.layoutManager.GetView(field.getName());//layout.findViewById(FormMetadata.Fields.get(x).getId());
				String dateString = ((EditText) (TextView)temp.findViewWithTag(field.getName().toLowerCase())).getText().toString();
			    
			    try
			    {
			    	DateFormat dateFormat = DateFormat.getDateInstance();
			    	Date convertedDate = dateFormat.parse(dateString); 
					result = convertedDate;
			    }
			    catch(Exception ex)
			    {
			    	result = null;
			    }
			}
			else
			{
				
				result = VariableCollection.GetValue(pName);
			}
		}
		
		
		return result;
	}

	@Override
	public void GoTo(String pDestination) 
	{
		View view = this.layoutManager.GetView(pDestination);

		layoutManager.ScrollTo(view.getTop() - 350);

		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();		
	}

	@Override
	public void Hide(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{
			
		}
		else
		{
		
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				control.setVisibility(View.INVISIBLE);
				
				try
				{
					View controlPrompt = this.layoutManager.GetView(s + "|prompt");
					controlPrompt.setVisibility(View.INVISIBLE);
				}
				catch (Exception ex)
				{
					
				}
			}
		}
		
	}

	@Override
	public void Highlight(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{
			
		}
		else
		{
		
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				control.setBackgroundColor(Color.YELLOW);
			}
		}
		
	}

	@Override
	public void UnHighlight(String[] pNameList, boolean pIsAnExceptList)
	{
		if(pIsAnExceptList)
		{
			
		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				control.setBackgroundColor(Color.WHITE);
			}
		}
		
	}

	@Override
	public void Enable(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{
			
		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				control.setEnabled(true);
			}
		}
		
	}
	
	@Override
	public void Clear(String[] pNameList, boolean pIsAnExceptList) 
	{
		
		if(pIsAnExceptList)
		{
			
		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				if (control instanceof Spinner)
				{
					((Spinner)control).setSelection(0);
				}
				else if (control instanceof CheckBox)
				{
					((CheckBox)control).setChecked(false);
				}
				else if (control instanceof TextView)
				{
					((TextView)control).setText("");
				}
			}
		}
		
	}

	@Override
	public void Disable(String[] pNameList, boolean pIsAnExceptList) 
	{
		
		if(pIsAnExceptList)
		{
			
		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				// #UNC - Start
				if (control != null)
				// #UNC - End
					control.setEnabled(false);
			}
		}
		
	}

	@Override
	public void NewRecord() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int RecordCount() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void UnHide(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{
			
		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				control.setVisibility(View.VISIBLE);
				
				try
				{
					View controlPrompt = this.layoutManager.GetView(s + "|prompt");
					controlPrompt.setVisibility(View.VISIBLE);
				}
				catch (Exception ex)
				{
					
				}
			}
		}
		
	}
	
	@Override
	public void ExecuteUrl(String url)
	{
		Uri uriUrl = Uri.parse(url);
		startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
	}
	
	@Override
	public void CaptureCoordinates(String latFieldName, String longFieldName)
	{
		EditText latitudeField = (EditText)this.layoutManager.GetView(latFieldName);
		EditText longitudeField = (EditText)this.layoutManager.GetView(longFieldName);

		try
		{
			latitudeField.setText(GeoLocation.CurrentLocation.getLatitude() + "");
			longitudeField.setText(GeoLocation.CurrentLocation.getLongitude() + "");
		}
		catch (Exception ex)
		{
			Alert("GPS coordinates could not be determined. Please try again.");
		}
	}

	@Override
	public void Quit() 
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBackPressed()
	{
		
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Exit the form?")       
	    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
	    	.setCancelable(true)       
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
	    	{           
	    		public void onClick(DialogInterface dialog, int id) 
	    		{                
	    			dialog.cancel();           
	    			RecordEditor.super.onBackPressed();
	    		}
	    		});
	    	builder.create();
	    	builder.show();
	}
	
	
	private class BitmapProcessor extends AsyncTask<Object,Void, ImageView>
    {
		
		private String fileName;

		@Override
		protected ImageView doInBackground(Object... params) {
			
			fileName = "";
			
			try
			{
				fileName = params[0].toString();
				BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
				bitmapOptions.inSampleSize = 4;
				Bitmap captured = BitmapFactory.decodeFile(fileName, bitmapOptions);
				Bitmap resized;
				if (captured.getWidth() > captured.getHeight())
				{
					resized = getResizedBitmap(captured,600,800);
				}
				else
				{
					resized = getResizedBitmap(captured,800,600);
				}    			
				FileOutputStream out = new FileOutputStream(fileName);
				resized.compress(Bitmap.CompressFormat.JPEG, 50, out);			
			}
			catch (Exception ex)
			{
				
			}
			return (ImageView)params[1];
		}
		
		@Override
        protected void onPostExecute(ImageView view) {
            
			if (!fileName.equals(""))
			{
				SetImage(view, fileName);
			}
			else
			{
				Alert("Photograph was not captured. Please try again.");
			}
        }
    	
    }
	
	private class ImageDecoder extends AsyncTask<Object,Void, Bitmap>
    {
		
		private String fileName;
		private ImageView view;

		@Override
		protected Bitmap doInBackground(Object... params) {
			
			fileName = params[0].toString();
			view = (ImageView)params[1];
			
			return BitmapFactory.decodeFile(fileName);
		}
		
		@Override
        protected void onPostExecute(Bitmap img) {
            
			try
			{
				view.setImageBitmap(img);
				view.setScaleType(ScaleType.FIT_XY);
				view.setTag(fileName);
			}
			catch (Exception ex)
			{
				//
			}
			
        }
    	
    }

}