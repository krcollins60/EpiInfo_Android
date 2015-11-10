package com.epiinfo.droid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.epiinfo.etc.DateButton;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.Rule_Context;

public class FormLayoutManager {
    
	private Activity container;
	private static int fieldCounter;
	private TextView currentDateField;
	private LinkedList<Integer> imageFieldIds;
	private Hashtable<View,String> clickCheckCodes;
	private LinkedList<Integer> requiredViewIds;
	private Hashtable<View,Integer> lengthChecks;
	private Hashtable<View,Double[]> ranges;
	private Hashtable<View,String> patterns;
	public Hashtable<String,View> controlsByName;
	private int formHeight;
	private int formWidth;
	private Rule_Context Context;
	public ScrollView scroller;
	private View executingView;
	private Drawable pageBackground;
	
	public FormLayoutManager(Activity container, int formHeight, int formWidth, ScrollView scroller, ViewGroup layout, Rule_Context pProcessor)
	{
		this.Context = pProcessor;
		this.container = container;
		imageFieldIds = new LinkedList<Integer>();
		requiredViewIds = new LinkedList<Integer>();
		lengthChecks = new Hashtable<View,Integer>();
		ranges = new Hashtable<View,Double[]>();
		patterns = new Hashtable<View,String>();
		clickCheckCodes = new Hashtable<View,String>();
		controlsByName = new Hashtable<String,View>();
		fieldCounter = -1;
		this.formHeight = formHeight;
		this.formWidth = formWidth;
		this.scroller = scroller;
		
		pageBackground = container.getResources().getDrawable(R.drawable.editor_rectangle);
		
		InitForm(layout);
	}
	
	private void InitForm(ViewGroup layout)
	{
		if (!DeviceManager.isPhone)
		{
			AddPageBreaks(layout);
		}
		for (int x=0;x<FormMetadata.Fields.size();x++)
		{
			Field field = FormMetadata.Fields.get(x);
			
   			String checkCode = "";
			if (FormMetadata.CheckCode.contains("Field " + field.getName() + "\n"))
			{
				int index = FormMetadata.CheckCode.indexOf("Field " + field.getName() + "\n");
				String rest = FormMetadata.CheckCode.substring(index);
				int index2 = rest.indexOf("End-Field");
				checkCode = rest.substring(0, index2 + 9);
			}
   			if (field.getType().equals("2"))
   			{
   				field.setId(AddHeader(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldFontSize(), field.getFieldFontStyle(), field.getFieldWidth(), field.getPagePosition()));
   			}
   			else if (field.getType().equals("21"))
   			{
   				field.setId(AddGroup(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldFontSize(), field.getFieldWidth(), field.getPagePosition(), true));
   			}
   			else if (field.getType().equals("5"))
			{
   				field.setId(AddNumericFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getLower(), field.getUpper(), field.getPattern(), field.getIsReadOnly(), checkCode + System.getProperty("line.separator")));
			}
   			else if (field.getType().equals("7"))
			{
   				field.setId(AddDateField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize()));
			}
   			else if (field.getType().equals("8"))
			{
   				field.setId(AddTimeField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize()));
			}
			else if (field.getType().equals("10"))
			{
				field.setId(AddCheckBox(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("11"))
			{
				field.setId(AddYesNoField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("12"))
			{
				field.setId(AddOptionField(layout, field.getPrompt(), field.getListValues(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("13"))
			{
				field.setId(AddButtonField(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("14"))
			{
				field.setId(AddImageField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize()));
			}
			else if (field.getType().equals("17") || field.getType().equals("19"))
			{
				field.setId(AddDropDownField(layout, field.getPrompt(), field.getListValues(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("3"))
			{
				field.setId(AddTextFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getIsRequired(), field.getIsReadOnly(), true, field.getMaxLength(), checkCode + System.getProperty("line.separator")));
			}
			else
			{
				field.setId(AddTextFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getIsRequired(), field.getIsReadOnly(), false, field.getMaxLength(), checkCode + System.getProperty("line.separator")));
			}
		}
		
		if (DeviceManager.isPhone)
		{
			View v1 = new View(container);
			v1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 40));
			v1.setBackgroundColor(0x00FFFFFF);
			layout.addView(v1);
		}
	}
	
	private void AddPageBreaks(ViewGroup myLayout)
	{
			for (int counter = 0; counter < FormMetadata.PageCount; counter++)
			{
				View v1 = new View(container);
				RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(formWidth - 20, formHeight - 20);
				params1.leftMargin = 10;
				params1.topMargin = (formHeight * counter) + 10;
				v1.setLayoutParams(params1);
				v1.setBackgroundDrawable(pageBackground);
				myLayout.addView(v1);
			}
	}
	
	public LinkedList<Integer> GetImageFieldIds()
	{
		return imageFieldIds;
	}
	
	public Activity getContainer()
	{
		return container;
	}
	
	public void ScrollTo(int y)
	{
		scroller.scrollTo(0, y);
	}
	
	public View GetExecutingView()
	{
		return executingView;
	}
	
	public View GetView(String pName)
	{
		if(controlsByName.containsKey(pName.toLowerCase()))
		{
			return controlsByName.get(pName.toLowerCase());
		}
		else
		{
			
			return null;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {

    }

	private TextView AddLabel(ViewGroup myLayout, String text, double x, double y, int pagePosition, double promptFontSize)
    {
    	TextView tv = new TextView(container);
    	tv.setText(text);
    	if (DeviceManager.isPhone)
    	{
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    		params.topMargin = 10;
    		tv.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		tv.setLayoutParams(params);
    	}
		if (promptFontSize > 0)
		{
			float calcSize = (float)(promptFontSize * 1.8);
			if (DeviceManager.isPhone)
			{
				tv.setTextSize(16);
			}
			else
			{
				tv.setTextSize((float)(promptFontSize * 1.8));
			}
		}
    	myLayout.addView(tv);
    	return tv;
    }
	
	public int AddHeader(ViewGroup myLayout, String text, double x, double y, double fieldFontSize, String fieldFontStyle, double fieldWidth, int pagePosition)
    {
		fieldCounter++;
		
    	TextView tv = new TextView(container);
    	 
    	tv.setText(text);
    	tv.setId(fieldCounter);
    	if (DeviceManager.isPhone)
    	{
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    		params.topMargin = 5;
    		params.bottomMargin = 10;
    		tv.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), LayoutParams.WRAP_CONTENT);
    		params2.leftMargin = (int)Math.round(formWidth * x);
    		params2.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		tv.setLayoutParams(params2);
    	}
		float calcSize = (float)(fieldFontSize * 1.8);
		if (DeviceManager.isPhone)
		{
			tv.setTextSize(20);
		}
		else
		{
			tv.setTextSize((float)(fieldFontSize * 1.8));
		}

		if (fieldFontStyle.toLowerCase().equals("bold"))
		{
			tv.setTypeface(null, Typeface.BOLD);
		}
    	myLayout.addView(tv);
    	
    	return fieldCounter;
    }
	
	public int AddGroup(ViewGroup myLayout, String text, double x, double y, double fieldFontSize, double fieldWidth, int pagePosition, boolean assignId)
    {
		if (assignId)
		{
			fieldCounter++;
		}
		
		View v1 = new View(container);
		if (DeviceManager.isPhone)
    	{
			View v2 = new View(container);
			v2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 15));
			myLayout.addView(v2);
    		v1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2));
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), 2);
    		params1.leftMargin = (int)Math.round(formWidth * x);
    		params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight) + (fieldFontSize * 1.8) + 3);
    		v1.setLayoutParams(params1);
    	}
		v1.setBackgroundColor(0xFF42638c);
				
    	TextView tv = new TextView(container);
    	 
    	tv.setText(text);
    	if (assignId)
    	{
    		tv.setId(fieldCounter);
    	}
   		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params2.leftMargin = (int)Math.round(formWidth * x);
    	params2.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
		tv.setLayoutParams(params2);
		
		float calcSize = (float)(fieldFontSize * 1.8);
		if (DeviceManager.isPhone)
		{
			tv.setTextSize(20);
		}
		else
		{
			tv.setTextSize((float)(fieldFontSize * 1.8));
		}
		tv.setTextColor(0xFF42638c);
		tv.setTypeface(null, Typeface.BOLD);
    	myLayout.addView(tv);
    	myLayout.addView(v1);
    	
    	return fieldCounter;
    }
	
	public int AddImageField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize)
	{
		fieldCounter++;
		AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		
		LinearLayout horzLayout1 = new LinearLayout(container);
		horzLayout1.setOrientation(0);
		RelativeLayout.LayoutParams hostParams;
		
		if (DeviceManager.isPhone)
		{
			hostParams = new RelativeLayout.LayoutParams((int) (300 * DeviceManager.GetDensity(container) + 0.5f),(int) (450 * DeviceManager.GetDensity(container) + 0.5f));
		}
		else
		{
			hostParams = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth),(int)Math.round(formHeight * fieldHeight));
			hostParams.leftMargin = (int)Math.round(formWidth * x);
			hostParams.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
		}
		horzLayout1.setLayoutParams(hostParams);
		horzLayout1.setGravity(Gravity.CENTER);
		myLayout.addView(horzLayout1);
		
		LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		childParams.bottomMargin = 4;
		childParams.leftMargin = 4;
		childParams.rightMargin = 4;
		childParams.topMargin = 4;
		
		LinearLayout horzLayout2 = new LinearLayout(container);
		horzLayout2.setOrientation(0);
		horzLayout2.setLayoutParams(childParams);
		horzLayout2.setGravity(Gravity.CENTER);
		horzLayout2.setBackgroundColor(0xFFFFFFFF);
		horzLayout1.addView(horzLayout2);
		
		LinearLayout horzLayout3 = new LinearLayout(container);
		horzLayout3.setOrientation(0);
		horzLayout3.setLayoutParams(childParams);
		horzLayout3.setGravity(Gravity.CENTER);
		horzLayout3.setBackgroundColor(0xFF000000);
		horzLayout2.addView(horzLayout3);
		
		ImageView iv = new ImageView(container);
		iv.setId(fieldCounter);
		iv.setImageResource(com.epiinfo.droid.R.drawable.camera);
		iv.setScaleType(ScaleType.CENTER);
		iv.setLayoutParams(childParams);
		iv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((RecordEditor)container).StartCamera((ImageView) v);
			}
		});
				
		horzLayout3.addView(iv);
		imageFieldIds.add(fieldCounter);
		return fieldCounter;
	}
	
	public int AddTimeField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		
		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (DeviceManager.isPhone)
    	{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
    		horzLayout.setLayoutParams(params1);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    		params1.leftMargin = (int)Math.round(formWidth * x);
    		params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		horzLayout.setLayoutParams(params1);
    	}
    	
		myLayout.addView(horzLayout);
		
		EditText edt = new EditText(container);
		edt.setInputType(InputType.TYPE_CLASS_DATETIME);
		edt.setEnabled(false);
		edt.setId(fieldCounter);
		if (DeviceManager.isPhone)
    	{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
    		edt.setLayoutParams(weightedParams);
    	}
    	else
    	{
    		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
    		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
    		edt.setLayoutParams(params2);
    	}
		horzLayout.addView(edt);
		
		DateButton btn = new DateButton(container);
		btn.setBackgroundResource(com.epiinfo.droid.R.drawable.btn_clock);
		btn.setScaleType(ScaleType.CENTER);
		btn.setLayoutParams(new LayoutParams(40, 40));
		btn.setTextField(edt);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentDateField = ((DateButton)v).getTextField();
				container.showDialog(1);				
			}
		});
		
		
		horzLayout.addView(btn);
		
		final EditText myTimeField = edt;
		
		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(com.epiinfo.droid.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams(25,25));
		eraseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myTimeField.setText("");
				
			}
		});
		horzLayout.addView(eraseButton);

		final ImageButton myEraseButton = eraseButton;
		
		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					myEraseButton.setVisibility(View.VISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});

		
		controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);
		
		return fieldCounter;
	}
	
	public int AddDateField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		
		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (DeviceManager.isPhone)
    	{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
    		horzLayout.setLayoutParams(params1);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    		params1.leftMargin = (int)Math.round(formWidth * x);
    		params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		horzLayout.setLayoutParams(params1);
    	}
    	
		myLayout.addView(horzLayout);
    	
		
		EditText edt = new EditText(container);
		edt.setInputType(InputType.TYPE_CLASS_DATETIME);
		edt.setEnabled(false);
		edt.setId(fieldCounter);
		edt.setTag(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase());
		if (DeviceManager.isPhone)
    	{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
    		edt.setLayoutParams(weightedParams);
    	}
    	else
    	{
    		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
    		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
    		edt.setLayoutParams(params2);
    	}
		
		horzLayout.addView(edt);
		
		DateButton btn = new DateButton(container);
		btn.setBackgroundResource(com.epiinfo.droid.R.drawable.btn_calendar);
		btn.setScaleType(ScaleType.CENTER);
		btn.setLayoutParams(new LayoutParams(40, 40));
		btn.setTextField(edt);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentDateField = ((DateButton)v).getTextField();
				container.showDialog(0);				
			}
		});
		
		
		horzLayout.addView(btn);
		
		final EditText myDateField = edt;
		
		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(com.epiinfo.droid.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams(25,25));
		eraseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myDateField.setText("");
				
			}
		});
		horzLayout.addView(eraseButton);
		
		final ImageButton myEraseButton = eraseButton;
		
		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					myEraseButton.setVisibility(View.VISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});
		
		controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);
		
		return fieldCounter;
	}
    
	private int AddTextField(ViewGroup myLayout, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, String checkCodeAfter, boolean isRequired, boolean isReadOnly, boolean upper, int maxLength)
    {
		fieldCounter++;
    	EditText txt = new EditText(container);
    	txt.setSingleLine();
    	txt.setImeOptions(EditorInfo.IME_ACTION_DONE);
    	if (upper)
    	{
    		txt.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
    	}
    	int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
    	if (DeviceManager.isPhone)
    	{
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
    		params.bottomMargin = 35;
    		txt.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		txt.setLayoutParams(params);
    	}
		
    	txt.setId(fieldCounter);
    	if (isRequired)
    	{
    		requiredViewIds.add(fieldCounter);    		
    	}    	
    	if (isReadOnly)
    	{
    		txt.setEnabled(false);
    	}
    	if (maxLength > 0)
    	{
    		lengthChecks.put(txt, maxLength);
    	}
    	
    	myLayout.addView(txt);
    	controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), txt);
    	clickCheckCodes.put(txt, checkCodeAfter);
    	    	
    	txt.addTextChangedListener(new TextWatcher()
    	{
    		public void afterTextChanged(Editable s) { 
                // Nothing 
            } 
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
                // Nothing 
            } 
            public void onTextChanged(CharSequence s, int start, int before, int count) { 
                try
                {
                	if (lengthChecks.containsKey(executingView))
                	{
                		if (((EditText)executingView).getText().length() > lengthChecks.get(executingView))
                		{
                			((EditText)executingView).setText(((EditText)executingView).getText().subSequence(0, lengthChecks.get(executingView)));
                		}
                	}
                }
                catch (Exception ex)
                {
                	
                }

            }
    	}
    	);
    	

    		
		String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();

		final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);
    		
    	txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				
				
				executingView = v;
				if (!hasFocus)
				{
					String clickCheckCode = clickCheckCodes.get(v);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule != null)AfterRule.Execute();
					if (requiredViewIds.contains(v.getId()))
					{
						if (((EditText)v).getText().equals(""))
						{
							((EditText)v).setError("This is a required field.");
						}
						else
						{
							((EditText)v).setError(null);
						}
					}
				}
				else
				{
					if(BeforeRule != null) BeforeRule.Execute();
				}
			}
		});
    	
    	return fieldCounter;
    }
	
	public int AddTextFieldWithPrompt(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, boolean isRequired, boolean isReadOnly, boolean upper, int maxLength, String checkCodeAfter)
	{
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		int id = AddTextField(myLayout,x,y,fieldWidth,fieldHeight,pagePosition,checkCodeAfter, isRequired, isReadOnly, upper, maxLength);
		controlsByName.put(FormMetadata.Fields.get(id).getName().toLowerCase() + "|prompt", label);
		return id;
	}
    
	private int AddNumericField(ViewGroup myLayout, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, double lower, double upper, String pattern, boolean isReadOnly, String checkCodeAfter)
    {
		fieldCounter++;
    	EditText txt = new EditText(container);
    	txt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	txt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        
    	int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
    	if (DeviceManager.isPhone)
    	{
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
    		params.bottomMargin = 35;
    		txt.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		txt.setLayoutParams(params);
    	}
		
		txt.setId(fieldCounter);
        myLayout.addView(txt);
        controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), txt);
        ranges.put(txt,new Double[]{lower, upper});
        if (pattern.contains("#"))
        {
        	patterns.put(txt, pattern);
        }
        if (isReadOnly)
    	{
    		txt.setEnabled(false);
    	}
        clickCheckCodes.put(txt, checkCodeAfter);
        
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

        txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				
				
				if (!hasFocus)
				{
					executingView = v;
					
					if (ranges.containsKey(v))
					{
						try
						{
							double val = Double.parseDouble(((EditText)v).getText().toString());
							double lower = ranges.get(v)[0];
							double upper = ranges.get(v)[1];
							if (val < lower || val > upper)
							{
								((EditText)v).setText("");
								((RecordEditor)getContainer()).Alert("Value is not in the given range. Minimum Range: " + lower + " Maximum Range: " + upper);
								return;
							}
						}
						catch (Exception ex)
						{
							//
						}
					}
					
					if (patterns.containsKey(v))
					{
						try
						{
							String pattern = patterns.get(v);
							String javaPattern = "";
							if (pattern.contains("."))
							{
								String left = pattern.split("\\.")[0];
								String right = pattern.split("\\.")[1];
								int leftOccur = CountOccurrences(left,'#');
								int rightOccur = CountOccurrences(right,'#');
								javaPattern = "\\d{1," + leftOccur + "}.\\d{" + rightOccur + "}";
							}
							else
							{
								int occur = CountOccurrences(pattern,'#');
								javaPattern = "\\d{0," + occur + "}";
							}
							if (!Pattern.matches(javaPattern, ((EditText)v).getText().toString()))
							{
								((EditText)v).setText("");
								((RecordEditor)getContainer()).Alert("Value does not match the given pattern: " + pattern);
								return;
							}
							
						}
						catch (Exception ex)
						{
							//
						}
					}
					
					String clickCheckCode = clickCheckCodes.get(v);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule!=null) AfterRule.Execute();
				}
				else
				{
					if(BeforeRule!=null) BeforeRule.Execute();
				}
			}
		});
        
        return fieldCounter;
    }
	
	private int CountOccurrences(String myString, char find) 
	{     
		int count = 0;     
		for (int i=0; i < myString.length(); i++)     
		{         
			if (myString.charAt(i) == find)         
			{              
				count++;         
			}     
		}     
		return count; 
	}
	
	public int AddNumericFieldWithPrompt(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double lower, double upper, String pattern, boolean isReadOnly, String checkCodeAfter)
	{
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);		
		int id = AddNumericField(myLayout,x,y,fieldWidth,fieldHeight,pagePosition, lower, upper, pattern, isReadOnly, checkCodeAfter);
		controlsByName.put(FormMetadata.Fields.get(id).getName().toLowerCase() + "|prompt", label);
		return id;
	}
    
	public int AddCheckBox(ViewGroup myLayout, String text, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, double fontSize, String checkCodeClick)
    {
		fieldCounter++;
    	CheckBox cbx = new CheckBox(container);
        cbx.setText(text);
        if (DeviceManager.isPhone)
    	{
    		cbx.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), (int)Math.round(formHeight * fieldHeight));
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		cbx.setLayoutParams(params);
    		if (fontSize > 0)
    		{
    			cbx.setTextSize((float)(fontSize * 1.8));
    		}
    	}
		
        cbx.setId(fieldCounter);
        myLayout.addView(cbx);
        controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), cbx);
        clickCheckCodes.put(cbx, checkCodeClick);
        
        
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		
        final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);
        
        cbx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
        		executingView = buttonView;
                String clickCheckCode = clickCheckCodes.get(buttonView);
                //processor.Execute(clickCheckCode, "Click");  
                if(ClickRule!=null) ClickRule.Execute();
            }

        });
        return fieldCounter;
    }
	
	public int AddOptionField(ViewGroup myLayout, String prompt, LinkedList<String> listValues, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, String checkCodeAfter)
    {
		fieldCounter++;
		
		AddGroup(myLayout, prompt,x,y,promptFontSize, fieldWidth, pagePosition, false);
		LinearLayout layout1 = new LinearLayout(container);
		RadioGroup layout2 = new RadioGroup(container);
		layout2.setOrientation(RadioGroup.HORIZONTAL);
		layout2.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int)Math.round(formWidth * x);
		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight) + 30);
		layout1.setLayoutParams(params);		
		layout1.setId(fieldCounter);
		myLayout.addView(layout1);
		controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), layout1);    	
		
		
		
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		for (int i=0;i<listValues.size();i++)
		{
			RadioButton btn = new RadioButton(container);
			btn.setId(((fieldCounter + 1) * 10000) + i);
			btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			btn.setText(listValues.get(i));
			layout2.addView(btn);
			clickCheckCodes.put(btn, checkCodeAfter);
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					String clickCheckCode = clickCheckCodes.get(v);
    				//processor.Execute(clickCheckCode, "After");
    				if(AfterRule!=null) AfterRule.Execute();
				}
			});
			
/*			btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if (isChecked)
					{
						String clickCheckCode = clickCheckCodes.get(buttonView);
	    				processor.Execute(clickCheckCode, "After");
					}
					
				}
			});*/
		}
		layout1.addView(layout2);
    	
    	return fieldCounter;
    }
	
	public int AddDropDownField(ViewGroup myLayout, String prompt, LinkedList<String> listValues, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, String checkCodeAfter)
    {
		fieldCounter++;
		
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		    	
		Spinner spinner = new Spinner(container);
		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (DeviceManager.isPhone)
    	{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
    		params.bottomMargin = 35;
    		spinner.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		spinner.setLayoutParams(params);
    	}
    	spinner.setId(fieldCounter);
    	spinner.setPrompt(prompt);
    	
    	String[] stringValues = new String[listValues.size()];
    	stringValues = listValues.toArray(stringValues);
    	
    	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.simple_spinner_item, stringValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	myLayout.addView(spinner);
    	controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), spinner);
    	controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);
    	
        clickCheckCodes.put(spinner, checkCodeAfter);
        
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

        
    	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    		@Override
    		public void onItemSelected(AdapterView<?> parent, View v,   int pos, long id) {
    			if (pos > 0)
    			{
    				
    				executingView = v;
    				String clickCheckCode = clickCheckCodes.get(parent);
    				//processor.Execute(clickCheckCode, "After");
    				if(AfterRule !=null) AfterRule.Execute();
    				
    			}
    			
    			
    		}
    		
    		@Override
    		public void onNothingSelected(AdapterView<?> parent)
    		{
    			
    		}
		});
    	
    	return fieldCounter;
    }
	
	public int AddYesNoField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, String checkCodeAfter)
    {
		fieldCounter++;
		
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
    	
		Spinner spinner = new Spinner(container);
		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (DeviceManager.isPhone)
    	{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
    		params.bottomMargin = 35;
    		spinner.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		spinner.setLayoutParams(params);
    	}
		spinner.setId(fieldCounter);
    	spinner.setPrompt(prompt);
    	
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(container, R.array.yn_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	myLayout.addView(spinner);
    	controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), spinner);
    	controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);
    	clickCheckCodes.put(spinner, checkCodeAfter);
    	
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

    	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    		@Override
    		public void onItemSelected(AdapterView<?> parent, View v,   int pos, long id) {
    			if (pos > 0)
    			{
    				executingView = v;
    				String clickCheckCode = clickCheckCodes.get(parent);
    				//processor.Execute(clickCheckCode, "After");
    				
    				if(AfterRule!=null) AfterRule.Execute();
    			}
    		}
    		
    		@Override
    		public void onNothingSelected(AdapterView<?> parent)
    		{
    			
    		}
		});
    	
    	return fieldCounter;
    }
	
	public int AddButtonField(ViewGroup myLayout, String text, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, String checkCodeClick)
    {
		fieldCounter++;
    	Button btn = new Button(container);
    	btn.setText(text);
    	if (DeviceManager.isPhone)
    	{
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
    		params.bottomMargin = 35;
    		btn.setLayoutParams(params);
    	}
    	else
    	{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), (int)Math.round(formHeight * fieldHeight));
    		params.leftMargin = (int)Math.round(formWidth * x);
    		params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
    		btn.setLayoutParams(params);
    	}
		btn.setId(fieldCounter);
        myLayout.addView(btn);
        controlsByName.put(FormMetadata.Fields.get(fieldCounter).getName().toLowerCase(), btn);
        clickCheckCodes.put(btn, checkCodeClick);
        
        
        String FieldName = FormMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);

        
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				executingView = v;
                String clickCheckCode = clickCheckCodes.get(v);
                //processor.Execute(clickCheckCode, "Click");
                if(ClickRule != null) ClickRule.Execute();
			}
		});
        
        return fieldCounter;
    }
    
	public void AddSpinner(RelativeLayout myLayout, String[] items, double x, double y)
    {
    	Spinner spinner = new Spinner(container);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.simple_spinner_dropdown_item, items);
    	spinner.setAdapter(adapter);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.leftMargin = (int)Math.round(formWidth * x);
		params.topMargin = (int)Math.round(formHeight * y);
		spinner.setLayoutParams(params);
    	myLayout.addView(spinner);
    }
	
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			//String date = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
			//String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
			DateFormat dateFormat = DateFormat.getDateInstance();
			Calendar cal = GregorianCalendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);
			currentDateField.setText(dateFormat.format(cal.getTime()));
			
		}
	};
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {

			Calendar datetime = Calendar.getInstance();
		    datetime.set(Calendar.HOUR_OF_DAY, hour);
		    datetime.set(Calendar.MINUTE, minute);
			
		    String time = new SimpleDateFormat("h:mm a").format(datetime.getTime());

			currentDateField.setText(time);
			
		}
	};
	
	public Dialog onCreateDialog(int id)
	{
		if (id == 0)
		{
			return new DatePickerDialog(container,mDateSetListener,Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}
		else if (id == 1)
		{
			Date now = new Date();
			return new TimePickerDialog(container,mTimeSetListener,now.getHours(),now.getMinutes(),false);
		}
		return null;
	}
	
}