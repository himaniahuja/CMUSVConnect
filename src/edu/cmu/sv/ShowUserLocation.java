package edu.cmu.sv;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ShowUserLocation extends MapActivity {

	double currentLatitude;
	double currentLongitude;
	Location currentLocation;
	TextView addressText;
	MapController mapController;
	MapView mapview;
	LocationManager locationManager;
	LocationListener locationListener;
	GeoPoint geopoint;
	
	class MapOverlay extends com.google.android.maps.Overlay
	{


	    @Override
	    public boolean draw(Canvas canvas, MapView mapView, 
	    boolean shadow, long when) 
	    {
	        super.draw(canvas, mapView, shadow);                   

	        //---translate the GeoPoint to screen pixels---
	        Point screenPts = new Point();
	        mapView.getProjection().toPixels(geopoint, screenPts);

	        //---add the marker---
	        Bitmap bmp = BitmapFactory.decodeResource(
	            getResources(), R.drawable.pin3);            
	        canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);         
	        return true;
	    }
	} 
	
	
	public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.location);
    	
    	mapview = (MapView)findViewById(R.id.mapview);
    	mapview.setBuiltInZoomControls(true);
		mapController = mapview.getController();
	    mapController.setZoom(16);
    	addressText = (TextView)findViewById(R.id.addressText);
    	
    	// http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
	  
		@SuppressWarnings("unchecked")
		HashMap<String, String> location = (HashMap<String, String>) getIntent().getSerializableExtra("location_info");
		if(location != null){
			Logger.getAnonymousLogger().info("location obj " +location);
			// get current date
    		Date dt = new Date();
    		String format = "yyyy-MM-dd";
    			
    		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
    		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    		
    		String curr_date = sdf.format(dt);
    		
    		String found_date = location.get("datetime").substring(0, 10);
    		Logger.getAnonymousLogger().info("date " + found_date + " " + curr_date);
    		if (curr_date.equalsIgnoreCase(found_date)){
    			Logger.getAnonymousLogger().info("Current location");
    			
				
    		} else {
    			String no_location = "Location not available for today. Showing previous address";
    			Toast.makeText(getBaseContext(), 
    					no_location, Toast.LENGTH_LONG).show();	
    			
    		}
    		double lon = Double.parseDouble(location.get("lon"));
			double lat = Double.parseDouble(location.get("lat"));
			double lon1 = lon * 1E6;
			double lat1 = lat * 1E6;
			
			int longitude = (int)lon1;
			int latitute = (int)lat1;
			geopoint = new GeoPoint(latitute, longitude);
			
			mapController.animateTo(geopoint);
			getAddress(lon, lat);
			
			//---Add a location marker---
		    MapOverlay mapOverlay = new MapOverlay();
		    List<Overlay> listOfOverlays = mapview.getOverlays();
		    Logger.getAnonymousLogger().info(" " + listOfOverlays);
			listOfOverlays.clear();
		    listOfOverlays.add(mapOverlay);  
		    
		    //mapview.invalidate();			    
		}
		else {
			
			// TODO have to clear map. 
			String no_location = "Location not available for today.";
			addressText.setText(no_location);
		}
			
			
	}
		
	void getAddress(Double lon, Double lat){
        try{
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = 
                gcd.getFromLocation(lat, lon,1);
            if (addresses.size() > 0) {
                StringBuilder result = new StringBuilder();
                for(int i = 0; i < addresses.size(); i++){
                    Address address =  addresses.get(i);
                    int maxIndex = address.getMaxAddressLineIndex();
                    for (int x = 0; x <= maxIndex; x++ ){
                        result.append(address.getAddressLine(x));
                        result.append("\n");
                    } 
                }
                addressText.setText(result.toString());
            }
        }
        catch(IOException ex){
            addressText.setText(ex.getMessage().toString());
        }
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}	
}
    

