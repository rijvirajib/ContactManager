package com.example.android.contactmanager;

import java.util.List;

import com.example.android.contactmanager.models.Contact;
import com.example.android.contactmanager.models.Contact.Email;
import com.example.android.contactmanager.models.Contact.Phone;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContactEntry extends Activity {
	
	public static final String TAG = "ContactEntry";
	public String contact_id;
	
	private LinearLayout linearLayout;
	/**
     * Called when the activity is first created. Responsible for initializing the UI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_entry);
        linearLayout = (LinearLayout) findViewById(R.id.contact_entry_root);
        Bundle bundle = getIntent().getExtras();
        contact_id = bundle.getString("contact_id");
        displayContact();
    }
    
    public void displayContact() {    	
    	Query mQuery = ContactManager.kinveyClient.query();
    	mQuery.equals("id", contact_id);
    	ContactManager.kinveyClient.appData("newCollection", Contact.class).getEntity(contact_id, new KinveyClientCallback<Contact>() {
    		@Override
			public void onSuccess(Contact contact) {
				// TODO Auto-generated method stub
				TextView contact_name = (TextView) findViewById(R.id.contactEntryText);
            	contact_name.setText(contact.getName());
            	
            	List<Phone> phone = contact.getPhone();
            	List<Email> email = contact.getEmail();
            	
            	for (Phone p : phone) {
            		TextView tv = new TextView(getBaseContext());
                    tv.setText(p.getNumber() + " at " + p.getType());
                    linearLayout.addView(tv);
            	}
            	
            	for (Email e: email) {
            		TextView tv = new TextView(getBaseContext());
                    tv.setText(e.getEmail() + " at " + e.getType());
                    linearLayout.addView(tv);
            	}
            	
            	TextView tv = new TextView(getBaseContext());
                tv.setText("Account " + contact.getOwner());
                linearLayout.addView(tv);
            	
			}

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "AppData.get all Failure", error);
                Toast.makeText(getApplicationContext(), "Get All error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
