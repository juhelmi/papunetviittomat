package com.example.papunetviittoma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.papunetviittoma.MainPapunet.OneImage;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	private ImageStoreOperations imageDB;
	private int countOfDbElements = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		// database
        this.imageDB = new ImageStoreOperations(this);
        this.imageDB.open();
        
		// Init buttons
		Button aButton;
        aButton = (Button) findViewById(R.id.buttonImportDatabase);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonInitDatabase(v);
            }
        });
        aButton = (Button) findViewById(R.id.buttonClearSearchDb);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonClearSearchDatabase(v);
            }
        });
        aButton = (Button) findViewById(R.id.buttonCheckDbStatus);        
        // -- register click event with first button ---
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	buttonCheckSearchDatabaseSize(v);
            }
        });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void buttonCheckSearchDatabaseSize(View v) {
		List<SignImage> images = this.imageDB.getAllImages();
		int imageCount = 0;
		for (Iterator<SignImage> i = images.iterator(); i.hasNext();) {
			SignImage elem = i.next();
			if (! elem.getSearchname().isEmpty() ) {
				imageCount++;
			}
		}
		TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);
		tView.setText("Tietokannassa on " + Integer.toString(imageCount) + " hakutulosta");
		this.countOfDbElements = imageCount;
	}

	private void buttonClearSearchDatabase(View v) {
		TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);
		List<SignImage> images = this.imageDB.getAllImages();
		
		if (this.countOfDbElements <= 0 ) {
			// Calculation must be done before
			tView.setText("Tietokanta on tyhjä tai koon laskentaa ei ole tehty");
			return;
		}
		int imageCount = 0;
		for (Iterator<SignImage> i = images.iterator(); i.hasNext();) {
			SignImage elem = i.next();
			this.imageDB.deleteImage(elem);
			imageCount++;
		}
		tView.setText(Integer.toString(imageCount) + " hakutulosta poistettu");
		this.countOfDbElements = 0;
	}

	private void buttonInitDatabase(View v) {
		//this.imageDB.writeLog(this.imageStoreDir+"/" + this.imageLogName);
		String s_url;
		EditText tUrl = (EditText)findViewById(R.id.editTextDatabaseImportUrl);
		TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);

		s_url = tUrl.getText().toString();
		if (s_url.isEmpty()) {
			// Address is missing
			tView.setText("Anna toimiva http-osoite, josta löytyy kuvaloki.");
			return;
		}
		if ( ! s_url.startsWith("http://") ) {
			s_url = "http://" + s_url;
			tView.setText("Osoite muutettu: "+s_url);
		}
		new DbTextFetchTask().execute(s_url);
	}
	
	// http://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread
    // needs http://developer.android.com/reference/android/os/AsyncTask.html
    private class DbTextFetchTask extends AsyncTask<String, Integer, List<String>> {

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
				TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);
				tView.setText("Virhe: "+ex.toString());
            }
            return inputStream;
        }

		@Override
		protected List<String> doInBackground(String... params) {
			// 
			TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);
			String s_Url = params[0];
			List<String> lines = new ArrayList<String>();
			//
			InputStream is;
			try {
				is = OpenHttpConnection(s_Url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				tView.setText("Virhe: "+e.toString());
				return lines;
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line;
			try {
				while ((line = in.readLine()) != null) {
				    lines.add(line);
				}
		        in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				tView.setText("Virhe: "+e.toString());
			}

			return lines;
		} 
    	protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }
    	protected void onPostExecute(List<String> result) {
    		// put lines to database or write to file?
    		// this one puts to database
    		List<SignImage> currentImages = imageDB.getAllImages();
    		TextView tView = (TextView)findViewById(R.id.textViewStatusInfo);
    		int addedCount = 0;

			tView.setText("Tarkistetaan uudet " + Integer.toString(result.size()) + " merkinnät");

    		for (Iterator<String> i = result.iterator(); i.hasNext(); ) {
    			String item = i.next();
        		SignImage now = imageDB.convertLine(item);	// returns null when line is not correct
        		Boolean bFound = false;
        		
        		// look if current item is not in list already
        		if (now != null) {
        			//already in low case now.getSearchname().toLowerCase()
	        		for (Iterator<SignImage> j = currentImages.iterator(); j.hasNext() && !bFound; ) {
	        			SignImage ref = j.next();
	        			if (ref != null) {
		        			if (now.getHref().equals(ref.getHref()) && now.getTitle().equals(ref.getTitle()) 
		        					&& now.getSearchname().equals(ref.getSearchname().toLowerCase())) {
		        				bFound = true;
		        			}
	        			}
	        		}
	        		if (!bFound) {
	        			imageDB.addImage(item);
	        			//currentImages.add(now);
	        			addedCount++;
	        		}
        		}
        		//
        		
    		}
			tView.setText(Integer.toString(addedCount) + " merkintaa lisatty");
    	}
    }

}
