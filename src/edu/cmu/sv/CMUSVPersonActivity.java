package edu.cmu.sv;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.sv.model.Message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CMUSVPersonActivity extends Activity {

		private static String HOSTNAME = "http://cmusvdirectory.appspot.com";
		String phone_number = null;
		ListAdapter adapter = null;
		ListView lView = null;
		HashMap<String,String> phone_list = null;
		HashMap<String, String> location_info = null;
		ArrayList<Message> messageList = null;
	
	  @Override
		public void onCreate(Bundle savedInstanceState) {
	    	
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.profile);
		  
		  	// http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 
		  
		 
		  try {
			
			HashMap<String, String> person = (HashMap<String, String>) getIntent().getSerializableExtra("person");
		    final String email = person.get("email");
		    final String email_id = person.get("email").split("@")[0];
			
		    // Get Image of person
		    Object content = null;
		    
		    // types of URL:
		    
		    // http://s3.amazonaws.com/cmusv-rails-production/people/photo/603/profile/AlexanderKurilin.jpg
		    // /images/staff/AbeIshihara.jpg
		    // http://cmusv-rails-production.s3.amazonaws.com/REV_a7e4c77154cc1546c4baa9e8d56927e255bc696b/images/students/2012/SE/AJCave.jpg
		    // http://cmusv-rails-production.s3.amazonaws.com/REV_a7e4c77154cc1546c4baa9e8d56927e255bc696b/images/staff/AbeIshihara.jpg
		    
		    String input_uri = person.get("image_uri");
		    String final_uri;
		    if (input_uri.contains("http"))
		    	final_uri = input_uri;
		    else 
		    	final_uri = "http://cmusv-rails-production.s3.amazonaws.com/REV_a7e4c77154cc1546c4baa9e8d56927e255bc696b" + input_uri;
		    
		    URL url = new URL(final_uri);
		    content = url.getContent();
			
		    InputStream is = (InputStream)content;
		    Drawable image = Drawable.createFromStream(is, "src");
			
		    ImageView imgView = new ImageView(this);
            imgView = (ImageView)findViewById(R.id.image_view);
            imgView.setImageDrawable(image);
		  	
		   
			// Get Name Of Person
			TextView text = new TextView(this); 

		    text = (TextView)findViewById(R.id.name_on_profile);
		    
		    text.setText(person.get("human_name"));
		    
		    
		    // Get the phone numbers and pass to phone activity
		    
		    phone_list = new HashMap<String,String>();
		    
		    if (person.containsKey("Mobile")){
		    	phone_list.put("Mobile", person.get("Mobile"));
		    }
		    
		    if (person.containsKey("Home")){
		    	phone_list.put("Home", person.get("Home"));
		    }  
		    
		    if (person.containsKey("Work")){
		    	phone_list.put("Work", person.get("Work"));
		    }
		    
		    if (person.containsKey("Google Voice")){
		    	phone_list.put("Google Voice", person.get("Google Voice"));
		    }
		    
		    // On click for telephone button
		    
		    final Button phone_button = (Button) findViewById(R.id.button_phone);
	    	phone_button.setOnClickListener(new View.OnClickListener() {
	              public void onClick(View v) {
	            	  Intent intent = new Intent(CMUSVPersonActivity.this, PhoneCallActivity.class);
	    			   Bundle b4 = new Bundle();
	    			   b4.putSerializable("phone_list", phone_list);
	    		   	   intent.putExtras(b4);
	    		   	   startActivityForResult(intent, 0);
	              }
	        });
		   
	   
		    // SMS button
		    final Button sms_button = (Button) findViewById(R.id.button_sms);
		    final HashMap<String, String> sms_list = phone_list;
		    sms_button.setOnClickListener(new View.OnClickListener() {
		          public void onClick(View v) {
		               Intent intent = new Intent(CMUSVPersonActivity.this, SmsActivity.class);
		    		   Bundle b3 = new Bundle();
		    		   b3.putSerializable("sms_list", sms_list);
		   		   	   intent.putExtras(b3);
		   		   	   startActivityForResult(intent, 0);
		              }
		        });	
		    	
			    
			// On click for email button
			final Button email_button = (Button) findViewById(R.id.button_email);
		    	 
		    email_button.setOnClickListener(new View.OnClickListener() {
		              public void onClick(View v) {
		            	  Intent intent = new Intent(CMUSVPersonActivity.this, EmailActivity.class);
		    			   Bundle b5 = new Bundle();
		    			   b5.putString("email_id", email);
		    		   	   intent.putExtras(b5);
		    		   	   startActivityForResult(intent, 0);
		              }
		          });
	
		    // on click location button
		    
		    Logger.getAnonymousLogger().info(email);
		    Logger.getAnonymousLogger().info(email_id);
		    String locationData = CMUSVUtils.readPeopleData(HOSTNAME + "/Location.json?limit=1&email=" + email_id);
		    Logger.getAnonymousLogger().info(locationData);
	    	JSONArray locationJson = null;
	    	
    	
    		locationJson = new JSONArray(locationData);
    		if (locationJson.length() !=0){
    			
    			JSONObject childJSONObject = locationJson.getJSONObject(0);
    		
	    		Double lat = childJSONObject.getDouble("lat");
	    		Double lon = childJSONObject.getDouble("long");
	    		
	    		//Json returns "DateTime": "2012-06-20 16:39:43.720440"
	    		String datetime = childJSONObject.getString("DateTime");
	    		
	    		location_info = new HashMap<String, String> ();
	    		location_info.put("lat", String.valueOf(lat));
	    		location_info.put("lon", String.valueOf(lon));
	    		location_info.put("email", email);
	    		location_info.put("datetime", datetime);
	    		location_info.put("human_name", person.get("human_name"));
	    		
	    		// On click for location button
			}
    			
		    final Button button_pin = (Button) findViewById(R.id.button_pin);
	    	button_pin.setOnClickListener(new View.OnClickListener() {
	              public void onClick(View v) {
	            	  Intent intent = new Intent(CMUSVPersonActivity.this, ShowUserLocation.class);
	    			  if(location_info !=null) {
		            	  Bundle b10 = new Bundle();
		    			  b10.putSerializable("location_info", location_info);
		    		   	  intent.putExtras(b10);
	    			  }
	    			  startActivityForResult(intent, 0);
	    			  
	              }
	        });
	    	
	    	// blank 
			TextView blank4 = (TextView) findViewById(R.id.blank4);
			blank4.setText("                 ");
			// blank 
			TextView blank5 = (TextView) findViewById(R.id.blank5);
			blank5.setText("                 ");
			// blank 
			TextView blank6 = (TextView) findViewById(R.id.blank6);
			blank6.setText("                 ");

	    	
	    	// Calendar integration 
	    	
	    	final Button calendarButton = (Button) findViewById(R.id.button_calendar);
	    	calendarButton.setOnClickListener(new View.OnClickListener() {
	    		 
	    		@Override
	    		public void onClick(View arg0) {
	    			//https://www.google.com/calendar/embed?src=himani.ahuja@west.cmu.edu
	    			Intent intent = new Intent(Intent.ACTION_VIEW, 
	    			     Uri.parse("https://www.google.com/calendar/embed?mode=WEEK&src=" + email_id + "@west.cmu.edu"));
	    			startActivity(intent);
	     
	    		}
	     	});
    
	    	// Get user messages 
	    	
	    	String messageData = CMUSVUtils.readPeopleData("http://cmusvdirectory.appspot.com/Message.json?limit=20&email=" + email_id);
	    	JSONArray messageJson = null;
	    	messageList = new ArrayList<Message>();

	    	messageJson = new JSONArray(messageData);
	    	if (messageJson.length() != 0){
	    		for (int i = 0 ; i < messageJson.length(); i++) {
	    			JSONObject message = messageJson.getJSONObject(i);
	    			Message m = new Message();
	    			m.setText(message.getString("text"))
	    			    .setEmail(message.getString("email"))
	    			    .setDateTime(message.getString("DateTime"));
	    		
	    			messageList.add(m);
	    		}
	    	}

	    	  final Button message_button = (Button) findViewById(R.id.button_message);
	    	  message_button.setOnClickListener(new View.OnClickListener() {
		              public void onClick(View v) {
		            	  Intent intent = new Intent(CMUSVPersonActivity.this, MessageActivity.class);
		    			   Bundle b4 = new Bundle();
		    			   b4.putSerializable("message_list", messageList);
		    		   	   intent.putExtras(b4);
		    		   	   startActivityForResult(intent, 0);
		              }
		        });
			   
	    	
	    	
	    	
	  } catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
  
  	  catch (IOException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
  		}
  
  	  catch (JSONException e2) {
  		e2.printStackTrace();
  		
  	  }
  }
}	



