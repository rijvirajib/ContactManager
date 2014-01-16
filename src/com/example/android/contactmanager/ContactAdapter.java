package com.example.android.contactmanager;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.contactmanager.models.Contact;

public class ContactAdapter extends ArrayAdapter<Contact> {
	private List<Contact> contacts;
	private LayoutInflater mLayoutInflater;
	
	public ContactAdapter(Context context, int resource, List<Contact> contacts) {
		super(context, resource, contacts);
		this.contacts = contacts;
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = mLayoutInflater.inflate(R.layout.contact_row, null);
			holder.customerName = (TextView) view.findViewById(R.id.contact_name);
			view.setTag(holder);
		} else {
			holder = (ViewHolder)view.getTag();
		}
		Contact c = contacts.get(position);
		if (c != null) {
			if(holder.customerName != null) {
				holder.customerName.setText(c.getName());
			}
		}
		return view;
    }
	
	@Override
	public int getCount() {
		return contacts.size();
	}
	
	@Override
	public Contact getItem(int i) {
		return contacts.get(i);
	}
	
	@Override
    public long getItemId(int i) {
        return 0;
    }
	
	/* This static method is what keeps it efficient */
	private static class ViewHolder {
        protected TextView customerName;
    }
}
