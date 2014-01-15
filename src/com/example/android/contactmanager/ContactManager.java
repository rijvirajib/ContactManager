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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public final class ContactManager extends Activity {
    public static final String TAG = "ContactManager";

    private Button mAddAccountButton;
    private ListView mContactList;
    
    private String appKey="";
    private String appSecret="";
    
    public static Client kinveyClient;

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
        
     // Register handler for UI elements
        mAddAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mAddAccountButton clicked");
                launchContactAdder();
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
				Toast.makeText(getApplicationContext(), contact.getId(),	Toast.LENGTH_SHORT).show();
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
    	kinveyClient.appData("entityCollection", Contact.class).get(new Query(), new KinveyListCallback<Contact>() {
            @Override
            public void onSuccess(Contact[] result) {    	
                if(result.length > 0) {
                	for (Contact contact : result) {
                        contact.setOwner(kinveyClient.user().getId());
                        /*Toast.makeText(getApplicationContext(),"Entity Retrieved\nTitle: " + contact.getName()
                                + "\nPhone: " + contact.get("phone"), Toast.LENGTH_SHORT).show(); */
                    }
                	
	            	ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(getApplicationContext(),
	                        android.R.layout.simple_list_item_1, result);
	            	mContactList.setAdapter(adapter);
	            	adapter.notifyDataSetChanged();
                } else {
                	Toast.makeText(getApplicationContext(), "No contacts, please import or add a new contact.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "AppData.get all Failure", error);
                Toast.makeText(getApplicationContext(), "Get All error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    protected void launchContactAdder() {
        Intent i = new Intent(this, ContactAdder.class);
        startActivityForResult(i, 0);
    }
    

    //TODO:: Figure out how to load the list again correctly
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if (requestCode == 0) { // ContactManager
	      if (resultCode == RESULT_OK) {
	        loadList();
	      }
	   }
	}


}
