package com.epiinfo.droid;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoLocation {
	
	private static LocationManager locationManager;
	private static LocationListener locationListener;
	public static Location CurrentLocation;
	
	public static void StopListening()
	{
		locationManager.removeUpdates(locationListener);
	}
	
	 public static void BeginListening(Activity activity)
	    {
	    	try
	    	{
	    	locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);// Define a listener that responds to location updates
	    	locationListener = new LocationListener() 
	    	{    
	    		public void onLocationChanged(Location newLocation) 
	    		{      
	    			if (CurrentLocation == null)
	    			{
	    				CurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    			}
    				if (isBetterLocation(newLocation, CurrentLocation))
    				{
    					CurrentLocation = newLocation;
    				}
	    		}    
	    		public void onStatusChanged(String provider, int status, Bundle extras) {}    
	    		public void onProviderEnabled(String provider) {}    
	    		public void onProviderDisabled(String provider) {}  
	    	};
	    	// Register the listener with the Location Manager to receive location updates
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	    	//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	    	}
	    	catch (Exception ex)
	    	{
	    		//Alert(ex.toString());
	    	}
	    }  	
	    
	    private static final int TWO_MINUTES = 1000 * 60 * 2;
	    
	    /** Determines whether one Location reading is better than the current Location fix  
	     * * @param location  The new Location that you want to evaluate  
	     * * @param currentBestLocation  The current Location fix, to which you want to compare the new one  
	     * */
	    private static boolean isBetterLocation(Location location, Location currentBestLocation) 
	    {    
	    	if (currentBestLocation == null) 
	    	{        
	    		// A new location is always better than no location        
	    		return true;    
	    	}    
	    	// Check whether the new location fix is newer or older    
	    	long timeDelta = location.getTime() - currentBestLocation.getTime();    
	    	boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;    
	    	boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;    
	    	boolean isNewer = timeDelta > 0;    
	    	// If it's been more than two minutes since the current location, use the new location    
	    	// because the user has likely moved    
	    	if (isSignificantlyNewer) 
	    	{        
	    		return true;    
	    		// If the new location is more than two minutes older, it must be worse    
	    	} 
	    	else if (isSignificantlyOlder) 
	    	{        
	    		return false;    
	    	}    
	    	// Check whether the new location fix is more or less accurate    
	    	int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());    
	    	boolean isLessAccurate = accuracyDelta > 0;    
	    	boolean isMoreAccurate = accuracyDelta < 0;    
	    	boolean isSignificantlyLessAccurate = accuracyDelta > 200;    
	    	// Check if the old and new location are from the same provider    
	    	boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());    
	    	// Determine location quality using a combination of timeliness and accuracy    
	    	if (isMoreAccurate) 
	    	{        
	    		return true;    
	    	} 
	    	else if (isNewer && !isLessAccurate) 
	    	{        
	    		return true;    
	    	} 
	    	else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
	    	{        
	    		return true;    
	    	}    
	    	return false;
	    }
	    
	    /** Checks whether two providers are the same */
	    private static boolean isSameProvider(String provider1, String provider2) 
	    {    
	    	if (provider1 == null) 
	    	{      
	    		return provider2 == null;    
	    	}    
	    	return provider1.equals(provider2);
	    }
	
}
