package com.example.papunetviittoma;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
//import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Environment;
//import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
//import android.widget.ListView;
import android.widget.TextView;

// DB part http://examples.javacodegeeks.com/android/core/database/sqlite/sqlitedatabase/android-sqlite-example/

public class MainPapunet extends Activity /*implements OnClickListener*/ {

	class OneImage {
		public String href;
		public String title;
		public String thumb;
		OneImage() {
			href = "";
			title = "";
			thumb = "";
		}
	}
	
	protected String queryWord = "";
	protected List<OneImage> curr_images = null; 
	private int image_position = 0;
	private String imageStoreDir = "";	// if null then it is not used
	private String dbg = "";
	
	// DB
	private final String imageLogName = "kuvat.txt";
	private ImageStoreOperations imageDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	this.curr_images = new ArrayList<OneImage>();
    	OneImage img = new OneImage();
    	img.href = "http://www.nic.fi/~jhelmin1/NikonLS40/HaleBob_640.jpg";
    	img.title = "Start image";
    	this.curr_images.add(img);
    	this.image_position = 0;
    	if (this.imageStoreDir != null) {
    		String path = android.os.Environment.getExternalStorageDirectory().getPath() + "/papunet";
    		File f = new File(path);
    		if (!f.exists() || !f.isDirectory()) {
    			// create it if possible
    			f.mkdir();
    		}
    		if (f.isDirectory()) {
    			this.imageStoreDir = path;
    		} else {
	    		PackageManager m = getPackageManager();
	    		String s = getPackageName();
	    		PackageInfo p;
				try {
					p = m.getPackageInfo(s, 0);
		    		s = p.applicationInfo.dataDir;
		    		this.imageStoreDir = s;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
			//App.getApp().getApplicationContext().getFilesDir().getAbsolutePath(); 
    	}

		setContentView(R.layout.activity_main_papunet);
		
		// Button registration
        Button aButton = (Button) findViewById(R.id.buttonExit);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonExit();
            }
        });
        aButton = (Button) findViewById(R.id.buttonNext);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonNext(v);
            }
        });
        aButton = (Button) findViewById(R.id.buttonPrev);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonPrev(v);
            }
        });
        aButton = (Button) findViewById(R.id.buttonGetPapunet);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonGetPapunet(v);
            }
        });
        aButton = (Button) findViewById(R.id.buttonSettings);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonSettings(v);
            }
        });
        TextView tView = (TextView)findViewById(R.id.textViewInfo);
        tView.setText(this.dbg);
        
        // database
        this.imageDB = new ImageStoreOperations(this);
        this.imageDB.open();
        
        // dbg
        //this.imageDB
        //setContentView(R.layout.activity_main_papunet);
        //
        List<SignImage> values = this.imageDB.getAllImages();
        System.out.println("Image count: "+Integer.toString(values.size()));
        // change for debug
        if (values.size()  < 0) {
        	/*List<String> lines = new ArrayList<String>();
        	EditText txt = (EditText)findViewById(R.id.editTextDb);
        	lines.add("Haku kuvia:"+Integer.toString(values.size()));
        	lines.add("Tallennus:"+imageStoreDir);
        	for (int i=0; i<values.size(); i++) {
        		lines.add(values.get(i).getSearchname() + ":" + values.get(i).getFilename()+"\n");
        	}
        	CharSequence strarray = lines.toString();
        	txt.setText(strarray);*/
        } else 
        if (values.size() == 0) {
        	// read old log file
        	this.imageDB.restoreFromOldLog(this.imageStoreDir+"/" + this.imageLogName);
        } else {
        	EditText txt = (EditText)findViewById(R.id.editTextDb);
        	txt.setVisibility(View.INVISIBLE);
        }
	}
	
	@Override
	protected void onResume() {
		imageDB.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		imageDB.close();
		super.onPause();
	}
	
	private void buttonExit() {
		this.imageDB.writeLog(this.imageStoreDir+"/" + this.imageLogName);
    	System.exit(RESULT_OK);
	}
	
	//--- Implement the OnClickListener callback
    public void onClick(View v) {
    	//
    	//this.dbg = v.toString();
    }
    
    public void buttonSettings(View v) {
    	// read database seed from some http-source
    	Intent intent = new Intent(this, SettingsActivity.class);
    	//EditText editText = (EditText) findViewById(R.id.editTextDatabaseImportUrl);
    	//editText.setText("osoite tähän");
    	startActivity(intent);
    }
    
    private void buttonGetPapunet(View v) {
        //
    	//this.dbg = v.toString();
    	String text;
    	TextView tView = (TextView)findViewById(R.id.textViewInfo);
    	EditText tWord = (EditText)findViewById(R.id.editTextWord);
    	
    	// close virtual keyboard
    	InputMethodManager imm = (InputMethodManager)getSystemService(
		      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tWord.getWindowToken(), 0);
    	
    	text = "Haetaan: " + tWord.getText();
    	tView.setText(text);
    	
    	// 
    	this.queryWord = tWord.getText().toString();
    	
    	// run rest of in separate thread
    	{
    		//String encode_word = queryWord;
    		// test if already in database?
			//String encode_word = URLEncoder.encode(queryWord, "ISO-8859-1");
	    	//String link = "http://papunet.net/materiaalia/kuvapankki/haku?kuva-q="+encode_word+"&field_stockimage_type_tid[]=181";
	    	new HtmlFetchTask().execute(queryWord);
		} 
    	//MyInetThread th = new MyInetThread(link, this);
    	//th.start();
    }
    
    public void buttonNext(View v) {
    	//
    	ShowImages(1);
    }
    public void buttonPrev(View v) {
    	//
    	ShowImages(-1);
    }
	
	// General parser routines
    private void ShowUrlImage(String s_url) {
    	// Test if image is already downloaded
    	String storedImgPath = this.imageStoreDir + "/" + ImageStoreOperations.getUrlBaseFilename(s_url);
    	java.io.File file = new java.io.File(this.imageStoreDir, ImageStoreOperations.getUrlBaseFilename(s_url));
    	if (file.exists()) {
    	    //FileInputStream fIn = new FileInputStream(file);
    		Bitmap image = BitmapFactory.decodeFile(storedImgPath);
    		if (image == null) {
    			// invalid image -> delete it
    			file.delete();
    		} else {
    			// OK, show it
	    		new JpegFetchTask().onPostExecute(image);
	    		return;
    		}
    	}
    	// not loaded or image was invalid
    	new JpegFetchTask().execute(s_url);
        /*if (s_url.isEmpty()) {
        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;
        Bitmap bm = loadBitmap(s_url, bmOptions);
        i.setImageBitmap(bm);
        }*/
    }
    
    public Bitmap loadBitmap(String URL, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in, null, options);
            in.close();
        } catch (IOException e1) {
        	e1.printStackTrace();
        }
        return bitmap;
    }
    private InputStream OpenHttpConnection(String strURL)
            throws IOException {
        InputStream inputStream = null;
        URL url = new URL(strURL);
        URLConnection conn = url.openConnection();

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        return inputStream;
    }
    
    private void ShowImages(int pos)
    {
    	int _size = this.curr_images.size();
    	this.image_position += pos;
    	if (this.image_position >= _size) {
    		this.image_position = 0;
    	} else {
    		if (this.image_position < 0) {
    			if (pos < 0 && _size > 0) {
    				this.image_position = _size - 1;
    			} else
    				this.image_position = 0;
    		}
    	}
    	if (_size > 0) {
    		OneImage img = this.curr_images.get(this.image_position);
    		ShowUrlImage(img.href);
    		///this.jLabelInfo.setText(img.title+" \n"+img.href);
    	}
    }
    
	/*private String urlFileBasename(String str) {
		String res;
		int pos;
		pos = str.lastIndexOf("/");
		if (pos <= 0) {
			res = str;
		} else {
			res = str.substring(pos+1);
		}
		return res;
	}*/

	// Not needed for scandinavian unicode letters
    public URL convertToURLEscapingIllegalCharacters(String string){
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()); 
            return uri.toURL(); 
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
        
    private List<OneImage> parseHtml(String s_url) {
    	List<String> lines = new ArrayList<String>();
    	List<OneImage> images = new ArrayList<OneImage>();
    	    	
    	//URL url_tmp = new URL(s_url);
    	URL url_tmp = convertToURLEscapingIllegalCharacters(s_url);

		try {
			InputStream is;
			try {
				is = OpenHttpConnection(url_tmp.toURI().toASCIIString());
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return images;
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = in.readLine()) != null) {
	            lines.add(line);
			}
	        in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// do the HTML parsing with regex
    	images = getImageNamesRegex(lines);
    	
    	// update images to screen
    	//this.curr_images = images;
    	//this.image_position = 0;
    	return images;
    }
    
    private String getQuotedPart(String str) {
    	int pos;
    	int end = 0;
    	
    	pos = str.indexOf('"');
    	if (pos>=0) {
    		pos += 1;
    		end = str.lastIndexOf('"') ;
    	}
    	if (end > 0) {
    		str = str.substring(pos, end);
    	}
    	return str;
    }
    
    private OneImage getImageDataFromLine(String str, OneImage img) {
    	//
    	Pattern pHref = Pattern.compile("href=\"[^ ]+\" ");
    	Pattern pTitle = Pattern.compile("title=\".*\"");
    	Pattern pSrc = Pattern.compile("src=\"[^ ]*\"");
    	Matcher m;
    	
    	m = pHref.matcher(str);
    	if (m.find()) {
    		String s = getQuotedPart(m.group(0));
    		img.href = s;
    	}
    	m = pTitle.matcher(str);
    	if (m.find()) {
    		String s = getQuotedPart(m.group(0));
    		img.title = s;
    	}
    	m = pSrc.matcher(str);
    	if (m.find()) {
    		String s = getQuotedPart(m.group(0));
    		img.thumb = s;
    	}
    	
    	return img;
    }
    
    private List<OneImage> getImageNamesRegex(List<String> lines) {
    	//
    	int state = 0;
    	OneImage img = new OneImage();
    	List<OneImage> result = new ArrayList<OneImage>();
    	Iterator<String> iter;
    	String reDivC = ".div[ \t]+id=\"content\"";
    	String reDivV = ".div[ \t]+class=\"view-content\"";
    	String reLi = ".li[ \t]+class=\"views-row ";
    	String reEUl = "</(ul|div)>";
    	String reELi = "</li>";
    			
    	Matcher m = null;
    	
    	Pattern pDivC = Pattern.compile(reDivC);
    	Pattern pDivV = Pattern.compile(reDivV);
    	Pattern pLi = Pattern.compile(reLi);
    	Pattern pEUl = Pattern.compile(reEUl);
    	Pattern pELi = Pattern.compile(reELi);
    	
    	iter = lines.iterator();
    	while (iter.hasNext()) {
    		String s = (String)iter.next();
    		switch (state)
    		{
    		default:
    		case 0:
    			m = pDivC.matcher(s);
    			if (m.find()) {
    				//
    				System.out.println("Match "+m.group(0));
    				state = 1;
    			}
    			break;
    		case 1:
    			m = pDivV.matcher(s);
    			if (m.find()) {
    				//
    				System.out.println("Match "+m.group(0));
    				state = 2;
    			}
    			break;
        	case 2:
    			m = pLi.matcher(s);
    			if (m.find()) {
    				//
    				System.out.println("Match "+m.group(0));
    				state = 3;
    				img = getImageDataFromLine(s, img);
    			} else {
	    			m = pEUl.matcher(s);
	    			if (m.find()) {
	    				//
	    				System.out.println("Match "+m.group(0));
	    				state = 0;
	    			}
    			}
    			break;
        	case 3:
    			m = pELi.matcher(s);
    			if (m.find()) {
    				//
    				System.out.println("Match "+m.group(0));
    				result.add(img);
    				img = new OneImage();
    				state = 2;
    			} else {
    				// read more for current image
    				img = getImageDataFromLine(s, img);
    			}
    			break;
    		}
    	}
    	return result;
    }

    // http://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread
    // needs http://developer.android.com/reference/android/os/AsyncTask.html
    private class HtmlFetchTask extends AsyncTask<String, Integer, List<List<OneImage>>> { 
    	// Copies database image element to html result format.
    	// Copying is done when item do not exist in reference list
    	protected void copySignImageListToOneImageList(List<SignImage> src, List<OneImage> cmp, List<OneImage> res) {
			for (Iterator<SignImage> i = src.iterator(); i.hasNext(); ) {
				SignImage item = i.next();
				OneImage img = new OneImage();
				img.href = item.getHref();
				// test that image is not already in res
				Boolean alreadyFound = false;
	    		for (Iterator<OneImage> j = cmp.iterator(); j.hasNext() && !alreadyFound; ) {
	    			OneImage imgCmp = j.next();
	    			if (imgCmp.href.contentEquals(img.href)) {
	    				alreadyFound = true;
	    			}
	    		}
	    		if (!alreadyFound) {
    				img.title = item.getTitle();
    				img.thumb = "";
	    			res.add(img);
	    		}
			}    		
    	}
    	protected List<List<OneImage>> doInBackground(String... word) {
    		// test if already in database?
			String searchName = word[0];
    		Boolean wordFound = false;
    		List<OneImage> result = null;	// direct result for query word 
    		List<OneImage> extraMatchResult = new ArrayList<OneImage>();	// place these after direct results
    		List<SignImage> dbRes;		// result from database query
    		List<List<OneImage>> returnLists = new ArrayList<List<OneImage>>();
    		//
    		dbRes = imageDB.getImagesBySearchName(searchName);
    		if (dbRes.size() > 0) {
    			wordFound = true;
    			List<SignImage> dbResExtra = new ArrayList<SignImage>();
    			List<OneImage> emptyImageList = new ArrayList<OneImage>();
    			result = new ArrayList<OneImage>();
    			// Copy first dbRes to res. emptyImageList is temporal empty list 
    			copySignImageListToOneImageList(dbRes, emptyImageList, result);
    			// look for additional images
    			imageDB.getImagesByTitleMatch(searchName, dbResExtra);
    			copySignImageListToOneImageList(dbResExtra, result, extraMatchResult);
    		}
    		if (!wordFound) {
    			if (word[0].trim().length() <= 1) {
       				result = new ArrayList<OneImage>();
       			} else {
		    		String link = "http://papunet.net/materiaalia/kuvapankki/haku?kuva-q="+word[0]+"&field_stockimage_type_tid[]=181";
		    		result = parseHtml(link);
       			}
	    		// store to DB
	    		for (Iterator<OneImage> i = result.iterator(); i.hasNext(); ) {
	    			OneImage img = i.next();
	    			String title = img.title;
	    			String href = img.href;
	    			imageDB.addImage(searchName, title, href);
	    		}
	    		// check old matches
	    		imageDB.getImagesByTitleMatch(searchName, dbRes);
	    		copySignImageListToOneImageList(dbRes, result, extraMatchResult);
    		}
    		returnLists.add(result);
    		returnLists.add(extraMatchResult);
    		return returnLists;
    	}
    	protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(List<List<OneImage>> result) {
            // result info not allowed to write to GUI
        	int listACount = result.get(0).size();
        	int listBCount = result.get(1).size();
        	curr_images.clear();
        	curr_images.addAll(result.get(0));
        	curr_images.addAll(result.get(1));
    		String text = queryWord +" : " + Integer.toString(listACount) + " + " + Integer.toString(listBCount);
    		TextView tView = (TextView)findViewById(R.id.textViewInfo);
            tView.setText(text);
            image_position = 0;
            ShowImages(0);	// show in current position
            // clear enter text field
            EditText tWord = (EditText)findViewById(R.id.editTextWord);
            tWord.setText("");
            // add to database
        }
    }
    private class JpegFetchTask extends AsyncTask<String, Integer, Bitmap> {
    	protected Bitmap doInBackground(String... link) {
            URL url;
            if (imageStoreDir == null) {
	            try {
	    			url = new URL(link[0]);
	    			try {
	    				return BitmapFactory.decodeStream(url.openConnection().getInputStream());
	    		        
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    		} catch (MalformedURLException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
            } else {
            	// load jpeg to file and from there to bitmap
            	// http://stackoverflow.com/questions/15758856/android-how-to-download-file-from-webserver
            	try {
            		int count;
            		String storePath;
					url = new URL(link[0]);
	                URLConnection conection;
					conection = url.openConnection();
	                conection.connect();

	                // this will be useful so that you can show a typical 0-100%
	                // progress bar
	                //int lenghtOfFile = conection.getContentLength();

	                // download the file
	                InputStream input = new BufferedInputStream(url.openStream(),
	                        8192);
	                
	                // Output stream
	                //storePath = Environment.getExternalStorageDirectory().toString()
	                //        + "/"+fileBasename(link[0]);
	                storePath = imageStoreDir + "/" + ImageStoreOperations.getUrlBaseFilename(link[0]);
	                OutputStream output = new FileOutputStream(storePath);

	                byte data[] = new byte[1024];

	                //long total = 0;

	                while ((count = input.read(data)) != -1) {
	                    //total += count;
	                    // publishing the progress....
	                    // After this onProgressUpdate will be called
	                    //publishProgress("" + (int) ((total * 100) / lenghtOfFile));

	                    // writing data to file
	                    output.write(data, 0, count);
	                }

	                // flushing output
	                output.flush();

	                // closing streams
	                output.close();
	                input.close();
	                // if (total < lenghtOfFile) ???
	                // get image back from file
	                Bitmap bitmap = BitmapFactory.decodeFile(storePath);
	                return bitmap;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
    		
    		return null;
    	}
    	protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Bitmap image) {
            // result info not allowed to write to GUI
        	if (image != null) {
	            ImageView i = (ImageView)findViewById(R.id.imageViewSign);
	            i.setImageBitmap(image);
	            // Set title
	            OneImage img = curr_images.get(image_position);
	            String text = Integer.toString(image_position+1) + ": " +img.title;
	            TextView tView = (TextView)findViewById(R.id.textViewImageInfo);
	            tView.setText(text);
        	}
        }
    }
}
