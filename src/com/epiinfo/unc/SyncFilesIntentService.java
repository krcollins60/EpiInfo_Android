package com.epiinfo.unc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;

public class SyncFilesIntentService extends IntentService {

	private static final String CLASSTAG = SyncFilesIntentService.class.getSimpleName();

	// 09Jun2015 v0.9.52 Add support for database
	SQLiteDatabase epiDB = null;
	  
	
	public SyncFilesIntentService() {
        super("SyncFilesIntentService");
    }
	
	
	@Override
	public void onHandleIntent(Intent intent) {
		if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ++++++++++++++++++++++++++++++");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " onHandleIntent - Enter");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ++++++++++++++++++++++++++++++");
		}
		
		/*************************
		// v0.9.61 - check if we are really now online
		SystemClock.sleep(2000); // Wait 2 seconds to allow network to fully come online
		if (!UncEpiSettings.IsNetworkAvailable(getBaseContext())) {
			if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " -----------------------------");
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " onHandleIntent - Exit since we are really OFFLINE!");
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " -----------------------------");
			}
			return;
		}
		****************************/	
		
		SystemClock.sleep(20000); // Wait 20 seconds to allow network to fully come online
		// check if we are still online
		if (!UncEpiSettings.IsNetworkAvailable(getBaseContext())) {
			if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " -----------------------------");
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " onHandleIntent - Exit since we are back OFFLINE!");
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " -----------------------------");
			}
			return;
		}
		
		readSyncFilenamesFromDb();
		deleteAllSyncFilenamesFromDb();
		getAllPointsStatusFromServer();
		
		if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ------------------------------");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " onHandleIntent - Exit");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ------------------------------");
		}
	}

	
	// v0.9.53 - UNC
    private void readSyncFilenamesFromDb() {
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " readSyncFilenamesFromDb - Enter");
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
 
		// Create or Open the Database
 		try {
 			epiDB = openOrCreateDatabase(Constants.EPI_DB_NAME, getApplicationContext().MODE_PRIVATE, null);

 			// Create the Points Table in the Database
 			epiDB.execSQL("CREATE TABLE IF NOT EXISTS "
 					+ Constants.SYNC_FILES_TABLE_NAME
 					+ " (ClusterField1, PointField2, LatitudeField3, LongitudeField4, FilenameField5);");
 		 
 			// retrieve data from database
 			Cursor c = epiDB.rawQuery("SELECT * FROM " + Constants.SYNC_FILES_TABLE_NAME , null);
 		 
 			int Column1 = c.getColumnIndex("ClusterField1");
 			int Column2 = c.getColumnIndex("PointField2");
 			int Column3 = c.getColumnIndex("LatitudeField3");
 			int Column4 = c.getColumnIndex("LongitudeField4");
 			int Column5 = c.getColumnIndex("FilenameField5");
 			int Column6 = c.getColumnIndex("FormnameField6");
 		 
 			// check if the query returned zero records
 			if (c.getCount() !=0 ) {
 				c.moveToFirst();
 				// Loop through all Results
 				do {
 					String clusterVal  = c.getString(Column1);
 					String pointVal    = c.getString(Column2);
 					String latVal      = c.getString(Column3);
 					String lonVal      = c.getString(Column4);
 					String filenameVal = c.getString(Column5);
 					String formnameVal = c.getString(Column6);  // v0.9.65 add field
 					
 					if (Constants.LOGS_ENABLED_DATABASE) {
 						Log.d(Constants.LOGTAG, " "+ SyncFilesIntentService.CLASSTAG + " readSyncFilenamesFromDb");
 						Log.d(Constants.LOGTAG, "    " + " Cluster = "  + clusterVal
 													   + " Point = "    + pointVal 
 													   + " Lat = "      + latVal
 													   + " Lat = "      + lonVal
 													   + " Filename = " + filenameVal
 													   + " Formname = " + formnameVal);  // v0.9.65 add field
 					}

 					// uploadSyncFiles(filenameVal);  // send sync files (encrypted and non-encrypted) to server
 					uploadSyncFiles(filenameVal, formnameVal);  // v0.9.65 extra param, send sync files (encrypted and non-encrypted) to server
 					setPointStatusOnServer(clusterVal, pointVal, latVal, lonVal);
 					// updatePointForStatus(clusterVal + "-" + pointVal, statusVal);  // needed???
 					
 				} while(c.moveToNext());
 			}
 			else {
 				if (Constants.LOGS_ENABLED_DATABASE) {
					Log.d(Constants.LOGTAG, " "+ SyncFilesIntentService.CLASSTAG + " readSyncFilenamesFromDb - No Records in DB!");
 				}
 			}
 		}
 		catch(Exception e) {
 			Log.e("readSyncFilenamesFromDb Error", "Error", e);
 		}
 		finally {
 			if (epiDB != null) {
 				epiDB.close();
 			}
 		}
    }
    

	private int setPointStatusOnServer(final String cluster, final String point, final String lat, final String lon) {
		if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ******************************");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " setPointStatusOnServer - Enter");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ******************************");
		}

		// Format CURL string to set the Point Status on the user
		BufferedReader in = null;

		try {
			/*********************************************
			HttpClient client = new DefaultHttpClient();

			String httpGetCmd = Constants.EPI_API_PREFIX + "op=epiSetPointStatus"
					+ "&p1=" + UncEpiSettings.username + "&p2="
					+ UncEpiSettings.selectedSurveyItem.title 
					+ "%7C" + cluster + "-" + point
					+ "%7C" + Constants.POINT_STATUS_COMPLETED
					+ "%7C" + lat
					+ "%7C" + lon
					+ "%7C" + UncEpiSettings.coordinator;
			**********************************************/
			
			URL getCmdUrl = new URL(Constants.EPI_API_PREFIX + "op=epiSetPointStatus"
					+ "&p1=" + UncEpiSettings.username + "&p2="
					+ UncEpiSettings.selectedSurveyItem.title 
					+ "%7C" + cluster + "-" + point
					+ "%7C" + Constants.POINT_STATUS_COMPLETED
					+ "%7C" + lat
					+ "%7C" + lon
					+ "%7C" + UncEpiSettings.coordinator);
			HttpURLConnection urlConnection = (HttpURLConnection) getCmdUrl.openConnection();
			urlConnection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);

			if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
				// Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " httpgetCmd = " + httpGetCmd);
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " httpgetCmd = " + getCmdUrl.toString());
			}
			
			// e.g.
			// http://50.16.195.50/epiinfo/api/epi_api.php?op=epiStatusUpdate&p1=one&p2=1|1|10|35.3245|-78.5612
			// p1=username
			// p2=SurveyId,ClusterId,PointId,Latitude,longitude

			/**********************************************
			HttpGet method = new HttpGet(httpGetCmd);
			HttpResponse response = client.execute(method);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			**********************************************/
			in = new BufferedReader (new InputStreamReader(urlConnection.getInputStream()));
			
			String line = "";
			if ((line = in.readLine()) != null) {
				if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
					Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " in: " + line);
				}
				if (line.indexOf("0") != -1) {
					return 0;
				}
			}

			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " exception cause: " + e.getCause());
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " exception message: " + e.getMessage());
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " exception desc: " + e.toString());
				Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " Connectivity Issue");
			}
		}

		return -1; // error
	}

	
	void uploadSyncFiles(final String filename, final String formname) {
		if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ******************************");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " uploadSyncFiles - Enter");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " uploadSyncFiles - filename = " + filename);
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " uploadSyncFiles - formname = " + formname);
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ******************************");
		}
		
		Intent uploadFilesIntent = new Intent(this, UploadSurveyFileActivity.class);
		uploadFilesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		uploadFilesIntent.putExtra("Filename",  filename + ".epi7");  // e.g. "DurhamCHOS_Hispanic_FINAL_201309090116.epi7");
		uploadFilesIntent.putExtra("Filename2", filename + ".xml");
		uploadFilesIntent.putExtra("Formname", formname);  // v0.9.65 extra param
		startActivity(uploadFilesIntent);
		
		if (Constants.LOGS_ENABLED_SYNCFILESINTENTSERVICE) {
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ------------------------------");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " uploadSyncFiles - Exit");
			Log.d(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ------------------------------");
		}
	}

	// v0.9.53 - UNC
    private void deleteAllSyncFilenamesFromDb() {
    	if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " deleteAllSyncFilenamesFromDb - Enter");
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
 
		// Create or Open the Database
 		try {
 			epiDB = openOrCreateDatabase(Constants.EPI_DB_NAME, getApplicationContext().MODE_PRIVATE, null);
 			epiDB.execSQL("delete from " + Constants.SYNC_FILES_TABLE_NAME);
 			epiDB.execSQL("vacuum");    // clear all allocated spaces
 			
 		}
 		catch(Exception e) {
 			Log.e("deleteAllSyncFilenamesFromDb Error", "Error", e);
 		}
 		finally {
 			if (epiDB != null) {
 				epiDB.close();
 			}
 		}
    }
    
    // v0.9.53 - UNC
    private void getAllPointsStatusFromServer() {
    	if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " getAllPointsStatusFromServer - Enter");
			Log.v(Constants.LOGTAG, " " + SyncFilesIntentService.CLASSTAG + " ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
    }
    
}