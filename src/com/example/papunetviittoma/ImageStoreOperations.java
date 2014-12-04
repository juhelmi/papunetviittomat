package com.example.papunetviittoma;

// http://examples.javacodegeeks.com/android/core/database/sqlite/sqlitedatabase/android-sqlite-example/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ImageStoreOperations {
	// Database fields
	private DatabaseOperations dbHelper;
	// Update this when database changes!!!
	private String[] IMAGE_TABLE_COLUMNS = { DatabaseOperations.IMAGE_ID, DatabaseOperations.IMAGE_SEARCH, 
			DatabaseOperations.IMAGE_TITLE, DatabaseOperations.IMAGE_HREF, DatabaseOperations.IMAGE_FILENAME,
			DatabaseOperations.IMAGE_SHORT_TITLE};
	private SQLiteDatabase database;

	public ImageStoreOperations(Context context) {
		dbHelper = new DatabaseOperations(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public void setupDB() {
		//dbHelper.onCreate(db)
	}
	
	public void writeLog(String filepath) {
		//String filename = this.imageStoreDir+"/" + this.imageLogName;
        List<SignImage> values = this.getAllImages();
        SignImage img;
    	//List<String> lines = new ArrayList<String>();
    	File file = new File(filepath);
    	FileOutputStream f;
		try {
			f = new FileOutputStream(file, false);
	    	PrintStream p = new PrintStream(f);
	    	p.println("Hakuihin vastauskuvia:"+Integer.toString(values.size()));
	    	//p.println("Tallennus kansioon:"+imageStoreDir);
	    	for (Iterator<SignImage> i = values.iterator(); i.hasNext(); ) {
	    		img = i.next();
	    		p.println(img.getSearchname() + " : " + img.getHref()+" : "+ img.getTitle());
	    	}
	    	p.close();
	    	f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void restoreFromOldLog(String filepath) {
		BufferedReader br = null;
		try {
			File f = new File(filepath);
			if (!f.exists()) {
				return;
			}
			br = new BufferedReader(new FileReader(filepath));
			String line;

			while ((line = br.readLine()) != null) {
				addImage(line);
			}
			//br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (br != null)
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static String getShortTitle(String title) {
		String shortTitle;
		int pos = title.indexOf(" (");
		if (pos > 0) {
			shortTitle = title.substring(0, pos);
		} else {
			shortTitle = title;
		}
		return shortTitle.toLowerCase();
	}
	public static String getUrlBaseFilename(String str) {
		int pos;
		String res;
		pos = str.lastIndexOf("/");
		if (pos <= 0) {
			res = str;
		} else {
			res = str.substring(pos+1);
		}
		return res;
	}
	
	public SignImage convertLine(String logLine) {
		String[] str = logLine.split(" : ");
		SignImage img = new SignImage();
		if (str.length < 3) {
			return null;
		} else {
			img.setSearchname(str[0].toLowerCase());
			img.setHref(str[1]);
			img.setTitle(str[2]);
			img.setFilename(getUrlBaseFilename(str[1]));
			img.setShortTitle(getShortTitle(str[2]));
			return img;
		}		
	}
	
	public SignImage addImage(String logLine) {
		String[] str = logLine.split(" : ");
		if (str.length < 3) {
			return null;
		} else {
			return addImage(str[0], str[2], str[1], getUrlBaseFilename(str[1]), getShortTitle(str[2]));
		}
	}
	
	public SignImage addImage(String searchName, String title, String href) {
		return addImage(searchName, title, href, getUrlBaseFilename(href), getShortTitle(title));
	}
	
	public SignImage addImage(String searchName, String title, String href, String filename, String shortTitle) {

		ContentValues values = new ContentValues();

		values.put(DatabaseOperations.IMAGE_SEARCH, searchName.toLowerCase());
		values.put(DatabaseOperations.IMAGE_TITLE, title);
		values.put(DatabaseOperations.IMAGE_HREF, href);
		values.put(DatabaseOperations.IMAGE_FILENAME, filename);
		values.put(DatabaseOperations.IMAGE_SHORT_TITLE, shortTitle);

		long studId = database.insert(DatabaseOperations.IMAGES, null, values);

		// now that the image is created return it ...
		Cursor cursor = database.query(DatabaseOperations.IMAGES,
				IMAGE_TABLE_COLUMNS, DatabaseOperations.IMAGE_ID + " = "
						+ studId, null, null, null, null);

		cursor.moveToFirst();

		SignImage newElem = parseSignImage(cursor);
		cursor.close();
		return newElem;
	}

	public void deleteImage(SignImage elem) {
		long id = elem.getId();
		System.out.println("Image deleted with id: " + id);
		database.delete(DatabaseOperations.IMAGES, DatabaseOperations.IMAGE_ID
				+ " = " + id, null);
	}

	public List<SignImage> getAllImages() {
		List<SignImage> images = new ArrayList<SignImage>();

		try {
			Cursor cursor = database.query(DatabaseOperations.IMAGES,
				IMAGE_TABLE_COLUMNS, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SignImage image = parseSignImage(cursor);
				images.add(image);
				cursor.moveToNext();
			}

			cursor.close();
		} catch (Exception e) {
			// none found
			// if cursor == null create DB?
		}

		return images;
	}

	public List<SignImage> getImagesBySearchName(String searchName) {
		List<SignImage> images = new ArrayList<SignImage>();

		if (searchName.trim().length() > 0)
		try {
			Cursor cursor = database.query(DatabaseOperations.IMAGES,
				IMAGE_TABLE_COLUMNS, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SignImage image = parseSignImage(cursor);
				if (image.getSearchname().compareToIgnoreCase(searchName) == 0) {
					images.add(image);
				}
				cursor.moveToNext();
			}

			cursor.close();
		} catch (Exception e) {
			// none found
			// if cursor == null create DB?
		}

		return images;
	}

	public void getImagesByTitleMatch(String searchName, List<SignImage> images) {
		//
		String searchText = searchName.toLowerCase();
		try {
			Cursor cursor = database.query(DatabaseOperations.IMAGES,
				IMAGE_TABLE_COLUMNS, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SignImage image = parseSignImage(cursor);
				// if text found from title?
				if (image.getShortTitle().indexOf(searchText) >= 0) {
					// Test if already in images list
					Boolean imgFound = false;
					for (Iterator<SignImage> i = images.iterator(); i.hasNext() && !imgFound; ) {
						//
						SignImage cmpImg = i.next();
						if (cmpImg.getHref().compareTo(image.getHref()) == 0) {
							imgFound = true;
						}
					}
					if (!imgFound) {
						images.add(image);
					}
				}
				cursor.moveToNext();
			}

			cursor.close();
		} catch (Exception e) {
			// none found
			// if cursor == null create DB?
		}
	}
	
	private SignImage parseSignImage(Cursor cursor) {
		SignImage image = new SignImage();
		
		// same order as in query, IMAGE_TABLE_COLUMNS
		image.setId((cursor.getInt(0)));
		image.setSearchname(cursor.getString(1));
		image.setTitle(cursor.getString(2));
		image.setHref(cursor.getString(3));
		image.setFilename(cursor.getString(4));
		image.setShortTitle(cursor.getString(5));
		return image;
	}
}
