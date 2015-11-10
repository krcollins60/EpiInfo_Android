
package com.epiinfo.droid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

/* #UNC - Start
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
 #UNC - End */

public class EpiDbHelper {

    public static final String KEY_ROWID = "_id";
    public static final String GUID = "globalRecordId";
    private static final String TAG = "EpiDbHelper";
    //public static RecordList recList;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static String DATABASE_NAME;// = "epiinfo";
    private static String DATABASE_TABLE;// = "Survey";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    /* #UNC - Start
    static AmazonSimpleDBClient sdbClient;
    #UNC - End */
	static String ACCESS_KEY_ID = "";
	static String SECRET_KEY = "";	
	static String nextToken;
	

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	
        	String DATABASE_CREATE =
                "create table "+ DATABASE_TABLE +" (" +
                "_id integer primary key autoincrement, ";
        	
   			for (int x=0;x<FormMetadata.Fields.size();x++)
   			{
   				if (!FormMetadata.Fields.get(x).getType().equals("2") && !FormMetadata.Fields.get(x).getType().equals("21"))
   				{
   					String dbType;
   					if (FormMetadata.Fields.get(x).getType().equals("5") || FormMetadata.Fields.get(x).getType().equals("10") || FormMetadata.Fields.get(x).getType().equals("11") || FormMetadata.Fields.get(x).getType().equals("12") || FormMetadata.Fields.get(x).getType().equals("17"))
   						dbType="real";
   					else
   						dbType="text";
    				DATABASE_CREATE += FormMetadata.Fields.get(x).getName() + " " + dbType + " null, ";
   				}
   			}
   			DATABASE_CREATE = DATABASE_CREATE.substring(0, DATABASE_CREATE.length() - 2) + ", globalRecordId text null);";
      		db.execSQL(DATABASE_CREATE);
      		
      		try
      		{
      			//new CloudDomainCreator().execute(true);
      		}
      		catch (Exception ex)
      		{
      			
      		}
        }
        
        private class CloudDomainCreator extends AsyncTask<Boolean,Void, Integer>
        {
    		@Override
    		protected Integer doInBackground(Boolean... params) {

    			/* #UNC - Start
    			AWSCredentials credentials = new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY );
      	        sdbClient = new AmazonSimpleDBClient( credentials); 
      	        sdbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
      			nextToken = null;
      			
      			CreateDomainRequest cdr = new CreateDomainRequest( DATABASE_TABLE );
      			sdbClient.createDomain( cdr );
      			#UNC - End */
    			return 0;
    		}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public EpiDbHelper(Context ctx, String tableName) {
        this.mCtx = ctx;
        DATABASE_TABLE = tableName;
        DATABASE_NAME = tableName + "DB";
        //recList = (RecordList)ctx;
    }

    public EpiDbHelper open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void DropDatabase(String tableName)
    {
    	mCtx.deleteDatabase(tableName + "DB");    	
    }

    public void close() {
        mDbHelper.close();
    }

    public long createRecord(ContentValues initialValues, boolean sendToCloud, String preexistingGuid) {
    	
    	if (preexistingGuid == null)
    	{
    	initialValues.put(GUID, UUID.randomUUID().toString());
    	}
    	else
    	{
    		initialValues.put(GUID, preexistingGuid);
    	}
        long retVal = mDb.insert(DATABASE_TABLE, null, initialValues);
        
        if (sendToCloud)
        {
    		//new CloudRecordCreator().execute(initialValues);
    	}
        
        return retVal;
    }
    
    private class CloudRecordCreator extends AsyncTask<ContentValues,Void, Integer>
    {
		@Override
		protected Integer doInBackground(ContentValues... params) {

			createCloudRecord(params[0]);
			return 0;
		}
    }
    
    private void createCloudRecord(ContentValues initialValues) {
		/* #UNC - Start
		String guidValue = initialValues.get(GUID).toString();
		
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		for (String key : initialValues.keySet())
		{
			attrs.add( new ReplaceableAttribute(key, initialValues.getAsString(key), true) );
		}		
		
		PutAttributesRequest par = new PutAttributesRequest( DATABASE_TABLE, guidValue + "", attrs);		
		
		if (sdbClient == null)
		{
			AWSCredentials credentials = new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY );
  	        sdbClient = new AmazonSimpleDBClient( credentials); 
  	        sdbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		}
		
		try {
			sdbClient.putAttributes( par );
		}
		catch ( Exception exception ) {
			System.out.println( "EXCEPTION = " + exception );
		}
		#UNC - End */
	}
        
	
	public void SyncWithCloud()
	{
		/*SelectRequest selectRequest = new SelectRequest( "select * from " + DATABASE_TABLE ).withConsistentRead( true );
		selectRequest.setNextToken( null );
		
		if (sdbClient == null)
		{
			AWSCredentials credentials = new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY );
  	        sdbClient = new AmazonSimpleDBClient( credentials); 
  	        sdbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		}
		
		SelectResult response = sdbClient.select( selectRequest );
		nextToken = response.getNextToken();
		
		List<Item> items = response.getItems();
		if (items.size() > 0)		
		{
			InsertOrUpdate( items );
		}*/
	}
	/* #UNC - Start
	private void InsertOrUpdate(List<Item> items)
	{
		List<Attribute> attributes = items.get(0).getAttributes();
		String[] attrNames = new String[attributes.size()];
		int count = 0;
		for (Attribute attr : attributes)
		{
			attrNames[count] = attr.getName();
			count++;
		}
		
		for (Item item : items)
		{
			String cloudGuid = item.getName();
			ContentValues attrValues = new ContentValues();
			
			Cursor c = mDb.rawQuery("select * from " + DATABASE_TABLE + " where " + GUID + " = '" + cloudGuid + "'", null);
			
			if (c.getCount() < 1)
			{			
				count = 0;
				for (Attribute attr : item.getAttributes())
				{
					attrValues.put(attr.getName(), attr.getValue());
					count++;
				}
				createRecord(attrValues, false, cloudGuid);
			}
			else
			{
				c.moveToFirst();
				String rowId = c.getString(0);
				count = 0;
				for (Attribute attr : item.getAttributes())
				{
					attrValues.put(attr.getName(), attr.getValue());
					count++;
				}
				updateRecord(Long.parseLong(rowId), attrValues, false);
			}
    }

		
	}
    #UNC - End */
    

    public boolean deleteRecord(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllRecords() {

    	String[] columns = new String[FormMetadata.DataFields.size() + 2];
    	for (int x=0; x<FormMetadata.DataFields.size() + 1; x++)
    	{
    		if (x==0)
    			columns[x]=KEY_ROWID;
    		else
    			columns[x]=FormMetadata.DataFields.get(x-1).getName();
    	}
    	columns[FormMetadata.DataFields.size() + 1]=GUID;
    	return mDb.query(DATABASE_TABLE, columns, null, null, null, null, null);
    }
    
    public Cursor fetchTopOne() {

    	String[] columns = new String[FormMetadata.DataFields.size() + 2];
    	for (int x=0; x<FormMetadata.DataFields.size() + 1; x++)
    	{
    		if (x==0)
    			columns[x]=KEY_ROWID;
    		else
    			columns[x]=FormMetadata.DataFields.get(x-1).getName();
    	}
    	columns[FormMetadata.DataFields.size() + 1]=GUID;
    	return mDb.query(DATABASE_TABLE, columns, null, null, null, null, "1");
    }
    
    public Cursor fetchLineListing(String field1, String field2, String field3) {

    	String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", '" + field3 + "' as columnName3, " + field3 + ", " + GUID + " FROM " + DATABASE_TABLE;
    	
    	return mDb.rawQuery(queryString, null);
    }

    public Cursor fetchRecord(long rowId) throws SQLException {

    	String[] columns = new String[FormMetadata.DataFields.size() + 2];
    	for (int x=0; x<FormMetadata.DataFields.size() + 1; x++)
    	{
    		if (x==0)
    			columns[x]=KEY_ROWID;
    		else
    			columns[x]=FormMetadata.DataFields.get(x-1).getName();
    	}
    	columns[FormMetadata.DataFields.size() + 1]=GUID;
    	
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor fetchWhere(String field1, String field2, String field3, String whereClause) {

    	String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", '" + field3 + "' as columnName3, " + field3 + ", " + GUID + " FROM " + DATABASE_TABLE + " WHERE " + whereClause;
    	
    	return mDb.rawQuery(queryString, null);
    }
    
    public Cursor fetchWhere_all(String where) throws SQLException {

    	String[] columns = new String[FormMetadata.DataFields.size() + 2];
    	for (int x=0; x<FormMetadata.DataFields.size() + 1; x++)
    	{
    		if (x==0)
    			columns[x]=KEY_ROWID;
    		else
    			columns[x]=FormMetadata.DataFields.get(x-1).getName();
    	}
    	columns[FormMetadata.DataFields.size() + 1]=GUID;
    	
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, where, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getFrequencyWhere(String field, String where) throws SQLException {

    	String[] columns = new String[] {field, "COUNT(*)"};
    	
        Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, where, null,
                    field, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getFrequency(String field, boolean reverseOrder) throws SQLException {

    	String[] columns = new String[] {field, "COUNT(*)"};
    	
    	Cursor mCursor;
    	
    	if (reverseOrder)
    	{
    		mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null, field, null, field + " desc", null);
    	}
    	else
    	{
    		mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null, field, null, field + " asc", null);
    	}
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getFieldValues(String field) throws SQLException {

    	String[] columns = new String[] {field};
    	
        Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getNumericValues(String field) throws SQLException {

    	String[] columns = new String[] {field};
    	
        Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, field + " < " + Double.MAX_VALUE, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public boolean updateRecord(long rowId, ContentValues args, boolean sendToCloud) {
        boolean retVal = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        
        if (sendToCloud)
        {
        	/*Cursor c = fetchRecord(rowId);
        	String guidValue = c.getString(c.getColumnCount() - 1);
        	args.put(GUID, guidValue);
        	new CloudRecordUpdator().execute(args);*/
        }
        
        return retVal;
    }
    
    private class CloudRecordUpdator extends AsyncTask<ContentValues,Void, Integer>
    {
		@Override
		protected Integer doInBackground(ContentValues... params) {

			updateCloudRecord(params[0]);
			return 0;
		}
    }
    
    private void updateCloudRecord(ContentValues initialValues) {
		/* #UNC - Start
		String guidValue = initialValues.get(GUID).toString();

		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		for (String key : initialValues.keySet())
		{
			attrs.add( new ReplaceableAttribute(key, initialValues.getAsString(key), true) );
		}		
		
		PutAttributesRequest par = new PutAttributesRequest( DATABASE_TABLE, guidValue, attrs);
		DeleteAttributesRequest dar = new DeleteAttributesRequest( DATABASE_TABLE, guidValue );
		try 
		{
			if (sdbClient == null)
			{
				AWSCredentials credentials = new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY );
	  	        sdbClient = new AmazonSimpleDBClient( credentials); 
	  	        sdbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
			}
			
			sdbClient.deleteAttributes( dar );
			sdbClient.putAttributes( par );
		}
		catch ( Exception exception ) 
		{

		}
		#UNC - End */
	}
    
}