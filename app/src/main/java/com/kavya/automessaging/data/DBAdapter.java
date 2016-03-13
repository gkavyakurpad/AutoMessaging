package com.kavya.automessaging.data;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter 
{
    public static final String KEY_ROWID = "_id";
    
    public static final String KEY_MESSAGETEXT = "msgcontent";
  //  public static final String KEY_MSGID="msgid";
    public static final String KEY_INCOMINGNUMBER="incomingnumber";
    
    public static final String KEY_READ = "readmessage";
    
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "MessageDetails";
    private static final String DATABASE_TABLE = "MessageInfo";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table MessageInfo (_id integer primary key autoincrement,incomingnumber text not null, msgcontent text not null , readmessage integer)";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
            System.out.println("database created successfully"+" "+DATABASE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        System.out.println("database opened successfully");
        System.out.println("DATA"+ DATABASE_CREATE);
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a title into the database---
    public long insertTitle( String msgNumber, String msgContent , String readMessage) 
    {
        ContentValues initialValues = new ContentValues();
     //   initialValues.put(KEY_MSGID,msgId);
        initialValues.put(KEY_MESSAGETEXT, msgContent);
        initialValues.put(KEY_INCOMINGNUMBER,msgNumber);
        initialValues.put(KEY_READ , readMessage);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }
    
    

  
    public Cursor getAllTitles() 
    {
        return db.query(DATABASE_TABLE, new String[] {
        		KEY_ROWID, KEY_INCOMINGNUMBER,
        		KEY_MESSAGETEXT ,KEY_READ
             }, 
                null, 
                null, 
                null, 
                null, 
                null);
    }

    //---retrieves a particular title---
   public Cursor getTitle(int readMessage) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE,
                		new String[] { KEY_INCOMINGNUMBER ,KEY_MESSAGETEXT,KEY_READ
                		}, 
                		KEY_READ + " = " + readMessage, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        System.out.println("rowid"+ readMessage);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
   
   
   /**
    * Update the note using the details provided.
    */
   public boolean updateNote(String msgNumber ,String msgContent ,int readMessage) {
       ContentValues args = new ContentValues();
       args.put(KEY_READ, readMessage);
     //  args.put(KEY_INCOMINGNUMBER , msgNumber);
     //  args.put(KEY_MESSAGETEXT , msgContent);
     //  String where = "readmessage + '='+1 ";
       String where = "incomingnumber = ? and msgcontent = ?";
       String[] whereArgs = { msgNumber, msgContent };
       
       System.out.println("Updating rows values: "+" "+db.update(DATABASE_TABLE,args,where,whereArgs));
       return db.update(DATABASE_TABLE,args,where,whereArgs) > 0;
   }

   
}