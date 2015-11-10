package com.epiinfo.analysis;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.epiinfo.droid.EpiDbHelper;
import com.epiinfo.droid.Field;
import com.epiinfo.droid.FormMetadata;
import com.epiinfo.droid.R;

public class TwoByTwoView extends RelativeLayout {

	private Context context;
	private EpiDbHelper mDbHelper;
	private LinearLayout outputLayout;
	
	public TwoByTwoView(Context context, EpiDbHelper mDbHelper) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    inflater.inflate(R.layout.analysis_two_by_two, this, true);
		
		this.context = context;
		this.mDbHelper = mDbHelper;
		SetupTwoxTwoGadget();
	}

	private void CalcTwoByTwo()
	{
		Spinner exposureSpinner = (Spinner) findViewById(R.id.twoxtwoExposureField);
		String exposureVar = exposureSpinner.getSelectedItem().toString();
		Spinner outcomeSpinner = (Spinner) findViewById(R.id.twoxtwoOutcomeField);
		String outcomeVar = outcomeSpinner.getSelectedItem().toString();
		
		boolean exposureYN = false;
		if (FormMetadata.GetFieldType(exposureVar) == 11)
		{
			exposureYN = true;
		}
		boolean outcomeYN = false;
		if (FormMetadata.GetFieldType(outcomeVar) == 11)
		{
			outcomeYN = true;
		}
		
		int yy=0;
		int yn=0;
		int ny=0;
		int nn=0;
		
		if (exposureSpinner.getSelectedItemPosition() > 0 && outcomeSpinner.getSelectedItemPosition() > 0)
		{

			Cursor c = mDbHelper.getFrequency(exposureVar, false);
			if (c.moveToFirst())
			{
				do
				{
					String val = c.getString(c.getColumnIndexOrThrow(exposureVar));
				
					Cursor c2 = mDbHelper.getFrequencyWhere(outcomeVar, exposureVar + " = " + val);
					if (c2.moveToFirst())
					{
						do
						{
							if (!exposureYN && !outcomeYN)
							{
								String val2 = c2.getString(c2.getColumnIndexOrThrow(outcomeVar));
								int count2 = c2.getInt(c2.getColumnIndexOrThrow("COUNT(*)"));
								
								if (val.equals("1") && val2.equals("1"))
								{
									yy=count2;
								}
								else if (val.equals("1") && val2.equals("0"))
								{
									yn=count2;
								}
								else if (val.equals("0") && val2.equals("1"))
								{
									ny=count2;
								}
								else if (val.equals("0") && val2.equals("0"))
								{
									nn=count2;
								}
							}
							else if (exposureYN && !outcomeYN)
							{
								String val2 = c2.getString(c2.getColumnIndexOrThrow(outcomeVar));
								int count2 = c2.getInt(c2.getColumnIndexOrThrow("COUNT(*)"));
								
								if (val.equals("1") && val2.equals("1"))
								{
									yy=count2;
								}
								else if (val.equals("1") && val2.equals("0"))
								{
									yn=count2;
								}
								else if (val.equals("2") && val2.equals("1"))
								{
									ny=count2;
								}
								else if (val.equals("2") && val2.equals("0"))
								{
									nn=count2;
								}
							}
							else if (!exposureYN && outcomeYN)
							{
								String val2 = c2.getString(c2.getColumnIndexOrThrow(outcomeVar));
								int count2 = c2.getInt(c2.getColumnIndexOrThrow("COUNT(*)"));
								
								if (val.equals("1") && val2.equals("1"))
								{
									yy=count2;
								}
								else if (val.equals("1") && val2.equals("2"))
								{
									yn=count2;
								}
								else if (val.equals("0") && val2.equals("1"))
								{
									ny=count2;
								}
								else if (val.equals("0") && val2.equals("2"))
								{
									nn=count2;
								}
							}
							else
							{
								String val2 = c2.getString(c2.getColumnIndexOrThrow(outcomeVar));
								int count2 = c2.getInt(c2.getColumnIndexOrThrow("COUNT(*)"));
								
								if (val.equals("1") && val2.equals("1"))
								{
									yy=count2;
								}
								else if (val.equals("1") && val2.equals("2"))
								{
									yn=count2;
								}
								else if (val.equals("2") && val2.equals("1"))
								{
									ny=count2;
								}
								else if (val.equals("2") && val2.equals("2"))
								{
									nn=count2;
								}
							}
						}while(c2.moveToNext());
					}
				}while(c.moveToNext());
			}
			
			TextView txtYY = (TextView) findViewById(R.id.txt2x2YY);
			txtYY.setText(yy + "");
			
			TextView txtYN = (TextView) findViewById(R.id.txt2x2YN);
			txtYN.setText(yn + "");
			
			TextView txtNY = (TextView) findViewById(R.id.txt2x2NY);
			txtNY.setText(ny + "");
			
			TextView txtNN = (TextView) findViewById(R.id.txt2x2NN);
			txtNN.setText(nn + "");
			
			TextView lblH1 = (TextView) findViewById(R.id.lbl2x2H1);
			lblH1.setText((yy + yn) + "");
			
			TextView lblH2 = (TextView) findViewById(R.id.lbl2x2H2);
			lblH2.setText((ny + nn) + "");
			
			TextView lblV1 = (TextView) findViewById(R.id.lbl2x2V1);
			lblV1.setText((yy + ny) + "");
			
			TextView lblV2 = (TextView) findViewById(R.id.lbl2x2V2);
			lblV2.setText((yn + nn) + "");
			
			TextView lblTotal = (TextView) findViewById(R.id.lbl2x2Total);
			lblTotal.setText((yy + yn + ny + nn) + "");
			
			double singleTableStats[] = ExactOR.CalcPoly(yy, yn, ny, nn);
			double oddsRisk[] = OddsAndRisk.MHStats(yy, yn, ny, nn, 0.95);
			
			DecimalFormat formatter = new DecimalFormat("#.####");
			DecimalFormat formatter2 = new DecimalFormat("#.########");
			
			TextView lblOdds = (TextView) findViewById(R.id.lblOdds);
			lblOdds.setText(formatter.format(oddsRisk[0]));
			TextView lblOddsLower = (TextView) findViewById(R.id.lblOddsLower);
			lblOddsLower.setText(formatter.format(oddsRisk[1]));
			TextView lblOddsUpper = (TextView) findViewById(R.id.lblOddsUpper);
			lblOddsUpper.setText(formatter.format(oddsRisk[2]));
			
			TextView lblMLE = (TextView) findViewById(R.id.lblMLE);
			lblMLE.setText(formatter.format(singleTableStats[0]));
			TextView lblMLELower = (TextView) findViewById(R.id.lblMLELower);
			lblMLELower.setText(formatter.format(singleTableStats[3]));
			TextView lblMLEUpper = (TextView) findViewById(R.id.lblMLEUpper);
			lblMLEUpper.setText(formatter.format(singleTableStats[1]));
			
			TextView lblFisherLower = (TextView) findViewById(R.id.lblFisherLower);
			lblFisherLower.setText(formatter.format(singleTableStats[4]));
			TextView lblFisherUpper = (TextView) findViewById(R.id.lblFisherUpper);
			lblFisherUpper.setText(formatter.format(singleTableStats[2]));
			
			TextView lblRisk = (TextView) findViewById(R.id.lblRisk);
			lblRisk.setText(formatter.format(oddsRisk[3]));
			TextView lblRRLower = (TextView) findViewById(R.id.lblRRLower);
			lblRRLower.setText(formatter.format(oddsRisk[4]));
			TextView lblRRUpper = (TextView) findViewById(R.id.lblRRUpper);
			lblRRUpper.setText(formatter.format(oddsRisk[5]));
			
			TextView lblRiskDiff = (TextView) findViewById(R.id.lblRiskDiff);
			lblRiskDiff.setText(formatter.format(oddsRisk[6]));
			TextView lblRDLower = (TextView) findViewById(R.id.lblRDLower);
			lblRDLower.setText(formatter.format(oddsRisk[7]));
			TextView lblRDUpper = (TextView) findViewById(R.id.lblRDUpper);
			lblRDUpper.setText(formatter.format(oddsRisk[8]));
			
			TextView lblChiSqUnc = (TextView) findViewById(R.id.lblChiSqUnc);
			lblChiSqUnc.setText(formatter.format(oddsRisk[9]));			
			TextView lblPUnc = (TextView) findViewById(R.id.lblPUnc);
			lblPUnc.setText(formatter2.format(oddsRisk[10]));			
			TextView lblChiSqMH = (TextView) findViewById(R.id.lblChiSqMH);
			lblChiSqMH.setText(formatter.format(oddsRisk[11]));			
			TextView lblPMH = (TextView) findViewById(R.id.lblPMH);
			lblPMH.setText(formatter2.format(oddsRisk[12]));			
			TextView lblChiSqCor = (TextView) findViewById(R.id.lblChiSqCor);
			lblChiSqCor.setText(formatter.format(oddsRisk[13]));			
			TextView lblPCor = (TextView) findViewById(R.id.lblPCor);
			lblPCor.setText(formatter2.format(oddsRisk[14]));
			
			TextView lblMidP1T = (TextView) findViewById(R.id.lblMidP1T);
			lblMidP1T.setText(formatter2.format(singleTableStats[5]));
			TextView lblFisher1T = (TextView) findViewById(R.id.lblFisher1T);
			lblFisher1T.setText(formatter2.format(singleTableStats[6]));
			TextView lblFisher2T = (TextView) findViewById(R.id.lblFisher2T);
			lblFisher2T.setText(formatter2.format(singleTableStats[7]));
		}
	}
	
	private void SetupTwoxTwoGadget()
	{		
		final TwoByTwoView self = this;
		ImageView closeImage = (ImageView) findViewById(R.id.btnClose);
		closeImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ViewManager)self.getParent()).removeView(self);
			}
		});
		
		Spinner exposureSpinner = (Spinner) findViewById(R.id.twoxtwoExposureField);
		exposureSpinner.setPrompt("Please select a field");
    	
    	String[] stringValues = new String[FormMetadata.BooleanFields.size() + 1];
    	stringValues[0] = "<Select>";
    	for (int x=1;x<=FormMetadata.BooleanFields.size();x++)
    	{
    		stringValues[x] = FormMetadata.BooleanFields.get(x-1).getName();
    	}
    	
    	ArrayAdapter<CharSequence> meansAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
    	meansAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	exposureSpinner.setAdapter(meansAdapter);
		
    	exposureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
        	{                 		
        		//outputLayout = (LinearLayout) analysisDialog.findViewById(R.id.twoxtwoOutput);
        		//outputLayout.removeAllViews();
        			CalcTwoByTwo();
        	}     
        	
        	public void onNothingSelected(AdapterView<?> parent) 
        	{     
        		
        	}
		});
    	
    	Spinner outcomeSpinner = (Spinner) findViewById(R.id.twoxtwoOutcomeField);
    	outcomeSpinner.setPrompt("Please select a field");
    	
    	ArrayAdapter<CharSequence> outcomeAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
    	outcomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	outcomeSpinner.setAdapter(outcomeAdapter);
		
    	outcomeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
        	{                 		
        		//outputLayout = (LinearLayout) analysisDialog.findViewById(R.id.twoxtwoOutput);
        		//outputLayout.removeAllViews();
        			CalcTwoByTwo();
        	}     
        	
        	public void onNothingSelected(AdapterView<?> parent) 
        	{     
        		
        	}
		});
	}	


	
}
