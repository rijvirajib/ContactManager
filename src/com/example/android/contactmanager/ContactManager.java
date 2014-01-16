/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contactmanager;

import com.example.android.contactmanager.models.Contact;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.query.AbstractQuery.SortOrder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public final class ContactManager extends Activity {
    public static final String TAG = "ContactManager";

    private Button mAddAccountButton;
    private Button mImportContactsButton;
    private ListView mContactList;
    ProgressDialog mProgressDialog;
    AlertDialog mAlertDialog;
    
    private String appKey="kid_TT9Z901M7O";
    private String appSecret="efbe20c5914849f59c2512d2c2e527e0";
    
    public static Client kinveyClient;
    
    public List<Contact> contacts;
    ContactAdapter mAdapter;

    /**
     * Called when the activity is first created. Responsible for initializing the UI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_manager);
        
        // Obtain handles to UI objects
        mAddAccountButton = (Button) findViewById(R.id.addContactButton);
        mContactList = (ListView) findViewById(R.id.contactList);
        mImportContactsButton = (Button) findViewById(R.id.importContactsButton);
        
        mProgressDialog = new ProgressDialog(ContactManager.this);
	    mProgressDialog.setMessage("Importing to ContactManager...");
	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    mProgressDialog.setCancelable(false);
	    mProgressDialog.setMax(100);
	    mProgressDialog.setProgress(0);
	    mProgressDialog.setCanceledOnTouchOutside(false);
	    
	    mAlertDialog = new AlertDialog.Builder(
	    		ContactManager.this).create();
	    mAlertDialog.setTitle("Loading...");
	    mAlertDialog.setMessage("Please wait...");		
	    mAlertDialog.setCancelable(false);
        
     // Register handler for UI elements
        mAddAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mAddAccountButton clicked");
                launchContactAdder();
            }
        });
        
        mImportContactsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mImportContactsButton clicked");
                final ContactImporter importer = new ContactImporter();
        		importer.execute();

        		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        		    @Override
        		    public void onCancel(DialogInterface dialog) {
        		    	importer.cancel(true);
        		    }
        		});
            }
        });

        // Generates an implicit Kinvey user
        loadKinveyClient();
        loadList();
        
        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				// TODO Auto-generated method stub
				Contact contact = (Contact) mContactList.getItemAtPosition(position);
				//Toast.makeText(getApplicationContext(), contact.getId(),Toast.LENGTH_SHORT).show();
				launchContactEntry(contact.getId());
			}
        	
		});
        
    }
    
    public void loadKinveyClient() {
    	kinveyClient = new Client.Builder(appKey, appSecret, this).build();
        if (!kinveyClient.user().isUserLoggedIn()) {
            kinveyClient.user().login(new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    Log.i(TAG,"Logged in successfully as " + result.getId());
                    //Toast.makeText(getApplicationContext(), "New implicit user logged in successfully as " + result.getId(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Throwable error) {
                    Log.e(TAG, "Login Failure", error);
                    //Toast.makeText(getApplicationContext(), "Login error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }   else {
            //Toast.makeText(this, "Using cached implicit user " + kinveyClient.user().getId(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public void loadList() {
    	mAlertDialog.show();
    	Query mQuery = kinveyClient.query();
    	//mQuery.equals("owner_id", kinveyClient.user().getId());
    	mQuery.addSort("name", SortOrder.ASC);
    	kinveyClient.appData("newCollection", Contact.class).get(mQuery, new KinveyListCallback<Contact>() {
            @Override
            public void onSuccess(Contact[] result) {    	
                if(result.length > 0) {
                	/*
                	for (Contact contact : result) {
                        Toast.makeText(getApplicationContext(),"Entity Retrieved\nTitle: " + contact.getName()
                                + "\nPhone: " + contact.get("phone"), Toast.LENGTH_SHORT).show();
                    }
                    */
                	contacts = Arrays.asList(result);
	            	//mAdapter = new ArrayAdapter<Contact>(getApplicationContext(), android.R.layout.simple_list_item_1, contacts);
                	mAdapter = new ContactAdapter(getApplicationContext(), R.layout.contact_row, contacts);
	            	mContactList.setAdapter(mAdapter);
	            	mAdapter.notifyDataSetChanged();
                } else {
                	Toast.makeText(getApplicationContext(), "No contacts, please import or add a new contact.", Toast.LENGTH_SHORT).show();
                }
                mAlertDialog.hide();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "AppData.get all Failure", error);
                Toast.makeText(getApplicationContext(), "Get All error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                mAlertDialog.hide();
            }
        });
    }
    
    protected void launchContactAdder() {
        Intent i = new Intent(this, ContactAdder.class);
        startActivityForResult(i, 0);
    }
    
    protected void launchContactEntry(String contact_id) {
    	Intent i = new Intent(this, ContactEntry.class);
    	i.putExtra("contact_id", contact_id);
    	startActivity(i);
    }

    //TODO:: Figure out how to load the list again correctly
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if (requestCode == 0) { // ContactManager
	      if (resultCode == RESULT_OK) {
	        loadList();
	      }
	   }
	}
	
	private class ContactImporter extends AsyncTask<Void, Integer, Integer> {
		private static final String TAG = "ContactImporter";
		// Let's hope I don't need to use SSL stuff..
		public static final String SERVER_URL = "http://raw2.github.com/Fetchnotes/ContactManager/super-secret-stuff/contacts.json";
		@Override
		protected Integer doInBackground(Void... params) {
			try {
				//Create an HTTP client
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(SERVER_URL);
				
				//Perform the request and check the status code
				HttpResponse response = client.execute(post);
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					
					try {
						//Read the server response and attempt to parse it as JSON
						Reader reader = new InputStreamReader(content);
						
						GsonBuilder gsonBuilder = new GsonBuilder();
                                                gsonBuilder.setDateFormat("M/d/yy hh:mm a");
						Gson gson = gsonBuilder.create();
						List<Contact> contacts = new ArrayList<Contact>();
						contacts = Arrays.asList(gson.fromJson(reader, Contact[].class));
						content.close();
						
						for (int i = 0; i < contacts.size(); i++) {
							Contact contact = contacts.get(i);
							publishProgress((int) ((i / (float) contacts.size()) * 100));
							kinveyClient.appData("newCollection", Contact.class).save(contact, new KinveyClientCallback<Contact>() {
					            @Override
					            public void onSuccess(Contact result) {}
					            
					            @Override
					            public void onFailure(Throwable error) {}
					        });
						}
						return contacts.size();
					} catch (Exception ex) {
						Log.e(TAG, "Failed to parse JSON due to: " + ex);
					}
				} else {
					Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
				}
			} catch(Exception ex) {
				Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        mProgressDialog.show();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "Imported " + Integer.toString(result));
			mProgressDialog.hide();
			loadList();
		}
		
		@Override
		protected void onProgressUpdate(Integer... result) {
			/* We can update this and show a nice download progress bar */
			mProgressDialog.setProgress(result[0]);
		}
	}
}
