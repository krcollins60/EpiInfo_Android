package com.epiinfo.droid;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.Display;

public class DeviceManager {

	public static boolean isLargeTablet;
	public static boolean isPhone;
	private static float density;
	private static Display defaultDisplay;
	private static double smallerNumber;
	
	public static double GetPageFactor()
	{
		if (isLargeTablet)
		{
			return 1.6;
		}
		else
		{
			/*if (isNexus7)
			{
				return 2.5;
			}
			else
			{
				return 1.86;
			}*/
			return smallerNumber / 294.4;
		}
	}
	
	public static double GetFontFactor()
	{
		if (isLargeTablet)
		{
			return 1;
		}
		else
		{
			if (isPhone)
			{
				return 0.5;
			}
			else
			{
				return 1.5;
			}
		}
	}
	
	public static float GetDensity(Activity activity)
	{
		if (density > 0)
		{
			return density;
		}
		else
		{
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			density = dm.density;
			return density;
		}
	}
	
	public static void SetOrientation(Activity activity)
	{
		/*if (android.os.Build.MODEL.equals("Nexus 7"))
		{
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			isLargeTablet = false;
			isPhone = false;
			isNexus7 = true;
		}
		else*/
		if (defaultDisplay == null)
		{
			defaultDisplay = activity.getWindowManager().getDefaultDisplay();
			DisplayMetrics dm = new DisplayMetrics();        
			defaultDisplay.getMetrics(dm);
			int height = dm.heightPixels;
			int width = dm.widthPixels;
			double density = dm.density;
		
			smallerNumber = width < height ? width : height;
			isPhone = (smallerNumber / density < 450);// activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
		
			if (smallerNumber < 750 || isPhone)
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				isLargeTablet = false;
			}
			else
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				isLargeTablet = true;
			}
		}
		else
		{
			if (isLargeTablet)
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			else
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
}
