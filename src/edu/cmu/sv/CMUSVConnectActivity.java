package edu.cmu.sv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.MapView;

import edu.cmu.sv.model.Message;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CMUSVConnectActivity extends ListActivity {

	static String USER_EMAIL = "himani.ahuja@sv.cmu.edu";
	static final String PEOPLE_JSON_VERSION = "1.0";

	private EditText filterText = null;
	ListAdapter adapter = null;
	String[] names = null;
	String[] emails = null;
	JSONObject[] objects = null;
	JSONArray jsonArray;
	ListView lv = null;
	ArrayList<Message> messageList = null;

	/** Called when the activity is first created. */
	public static Logger logger = Logger.getLogger("CMUSVDirectoryActivity");

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(getApplicationContext())
				.getAccounts();
		
		boolean accountNotFound = true;
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				String possibleEmail = account.name;
				logger.info(account.name);
				if (possibleEmail.toLowerCase().endsWith("cmu.edu")) {
					logger.info("Found email");
					USER_EMAIL = possibleEmail;
					accountNotFound = false;
				}
			}
		}

		if (accountNotFound){			
			Toast accountCheck = Toast.makeText(this.getApplicationContext(), 
					"No west.cmu.edu account found. - Opening the Account settings.", Toast.LENGTH_LONG);
				accountCheck.show();
				startActivityForResult(new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT), 0);  
				return;
		}
			
			
			
		// check people data version from backend
		String peopleDataVersion = CMUSVUtils
				.readPeopleData("http://cmusvdirectory.appspot.com/Version");
		StringBuffer readPeopleData = new StringBuffer();
		if (null == peopleDataVersion
				|| peopleDataVersion.equalsIgnoreCase(PEOPLE_JSON_VERSION)) {
			logger.info("version is equal");
			InputStream is;
			try {
				String str = "";
				is = this.getResources().getAssets().open("People.json");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				if (is != null) {
					while ((str = reader.readLine()) != null) {
						readPeopleData.append(str + "\n");
					}
				}
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// read JSON
			readPeopleData
					.append(CMUSVUtils
							.readPeopleData("http://cmusvdirectory.appspot.com/People.json"));
		}
		try {
			jsonArray = new JSONArray(readPeopleData.toString());
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		filterText = (EditText) findViewById(R.building_list.search_box);
		filterText.addTextChangedListener(filterTextWatcher);

		// testing
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

		// Loop the Array
		for (int k = 0; k < jsonArray.length(); k++) {

			HashMap<String, String> map = new HashMap<String, String>();
			JSONObject e;
			try {
				e = jsonArray.getJSONObject(k);

				// creating data to be sent to other activities.
				String firstname = e.getString("first_name");
				String lastname = e.getString("last_name");
				String human_name = firstname + " " + lastname;

				map.put("id", String.valueOf(k));
				map.put("human_name", human_name);
				map.put("email", e.getString("email"));
				map.put("image_uri", e.getString("image_uri"));

				if (e.has("Mobile"))
					map.put("Mobile", e.getString("Mobile"));

				if (e.has("Work"))
					map.put("Work", e.getString("Work"));

				if (e.has("Home"))
					map.put("Home", e.getString("Home"));

				if (e.has("Google Voice"))
					map.put("Google Voice", e.getString("Google Voice"));

				mylist.add(map);

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// http://p-xr.com/android-tutorial-how-to-parse-read-json-data-into-a-android-listview/

		adapter = new SimpleAdapter(this, mylist, R.layout.list_item,
				new String[] { "human_name" }, new int[] { R.id.one_item });
		setListAdapter(adapter);

		lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				@SuppressWarnings("unchecked")
				HashMap<String, String> person = (HashMap<String, String>) lv
						.getItemAtPosition(position);
				Intent intent = new Intent(CMUSVConnectActivity.this,
						CMUSVPersonActivity.class);
				Bundle bundle_person = new Bundle();
				// b.putString("person",person.toString());
				bundle_person.putSerializable("person", person);
				intent.putExtras(bundle_person);
				startActivityForResult(intent, 0);
			}
		});

		// **** not working
		// to hide the virtual keypad
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(filterText.getWindowToken(), 0);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// get location of user
		final Button location_button = (Button) findViewById(R.id.check_in_button);
		location_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(CMUSVConnectActivity.this,
						ShowMyLocationActivity.class);
				startActivityForResult(intent, 0);
			}
		});

		// Share a message
		final Button message_share_button = (Button) findViewById(R.id.message_share_button);
		message_share_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(CMUSVConnectActivity.this,
						MessageShareActivity.class);
				startActivityForResult(intent, 0);
			}
		});

		// blank 
		TextView blank2 = (TextView) findViewById(R.id.blank2);
		blank2.setText("  ");
		
		
		// Get user messages

		String messageData = CMUSVUtils
				.readPeopleData("http://cmusvdirectory.appspot.com/Message.json?limit=20");
		JSONArray messageJson = null;
		messageList = new ArrayList<Message>();

		try {
			messageJson = new JSONArray(messageData);

			if (messageJson.length() != 0) {
				for (int i = 0; i < messageJson.length(); i++) {
					JSONObject message = messageJson.getJSONObject(i);
					Message m = new Message();
					m.setText(message.getString("text"))
							.setEmail(message.getString("email"))
							.setDateTime(message.getString("DateTime"));

					messageList.add(m);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read a message
		final Button message_read_button = (Button) findViewById(R.id.message_read_button);
		message_read_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(CMUSVConnectActivity.this,
						MessageActivity.class);
				Bundle b4 = new Bundle();
				b4.putSerializable("message_list", messageList);
				intent.putExtras(b4);
				startActivityForResult(intent, 0);
			}
		});

	}

	private final TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			((Filterable) adapter).getFilter().filter(s);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		filterText.removeTextChangedListener(filterTextWatcher);
	}

}