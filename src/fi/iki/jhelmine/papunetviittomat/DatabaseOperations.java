package fi.iki.jhelmine.papunetviittomat;

//import SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOperations extends SQLiteOpenHelper {

	public static final String IMAGES = "Images";
	public static final String IMAGE_ID ="_id";
	public static final String IMAGE_SEARCH = "_search";
	public static final String IMAGE_TITLE = "_title";
	public static final String IMAGE_SHORT_TITLE = "_s_title";
	public static final String IMAGE_HREF = "_href";
	public static final String IMAGE_FILENAME = "_file";
	
	private static final String DATABASE_NAME = "papunet.db";
	private static final int DATABASE_VERSION = 2;
	
	/*
	private static final String DATABASE_CREATE = "create table " + IMAGES
			+ "(" + IMAGE_ID + " integer primary key autoincrement, "
			+ IMAGE_TITLE + " text not null, "
			+ IMAGE_HREF + " text not null, "
			+ IMAGE_FILENAME + " text not null );";*/
	private static final String DATABASE_CREATE = "create table " + IMAGES
			+ "(" + IMAGE_ID + " integer PRIMARY KEY ASC, "
			+ IMAGE_SEARCH + " text not null, "
			+ IMAGE_TITLE + " text not null, "
			+ IMAGE_HREF + " text not null, "
			+ IMAGE_FILENAME + " text not null, "	// last , suppressed
			+ IMAGE_SHORT_TITLE + " text not null "
			//+ "PRIMARY KEY(" + IMAGE_ID + "ASC )" 
			+ ");";
	
	public DatabaseOperations(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// you should do some logging in here
		// ..

		db.execSQL("DROP TABLE IF EXISTS " + IMAGES);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// you should do some logging in here
		// ..

		db.execSQL("DROP TABLE IF EXISTS " + IMAGES);
		onCreate(db);
	}

}
