package com.epiinfo.droid;

import java.util.LinkedList;

import android.app.Activity;

public class AppManager {

	private static LinkedList<Activity> activities;
	
	public static void Started(Activity activity)
	{
		if (activities == null)
		{
			activities = new LinkedList<Activity>();
		}
		activities.add(activity);
		if (activities.size() == 1)
		{
			GeoLocation.BeginListening(activity);
		}
	}
	
	public static void Closed(Activity activity)
	{
		if (activities.contains(activity))
		{
			activities.remove(activity);
		}
		if (activities.size() == 0)
		{
			GeoLocation.StopListening();
		}
	}
	
}
