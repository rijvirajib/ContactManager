package com.example.android.contactmanager.models;

import java.util.ArrayList;
import java.util.List;

import android.provider.ContactsContract;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.model.KinveyMetaData;

public class Contact extends GenericJson {
	@Key("_id")
	private String _id;
	@Key
	private String owner_id;
	@Key
	private String name;
	@Key
	private List<Email> email;
	@Key
	private List<Phone> phone;
	@Key("_kmd") 
	private KinveyMetaData meta;
	
	public void setOwner(String owner_id) {
		this.owner_id = owner_id;
	}
	
	public String getOwner() {
		return this.owner_id;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return this._id;
	}
	
	public Contact(){}
	
	// public instead of private: http://stackoverflow.com/a/18644324/247231
	public static class Email extends GenericJson {
		@Key
		private String email;
		@Key
		private String type;
		
		public Email(String e, String t) {
			email = e;
			type = t;
		}
		
		public String getEmail() {
			return email;
		}
		
		public String getType() {
			return type;
		}
		
		public Email() {}
	}
	
	public static class Phone extends GenericJson{
		@Key
		private String phone;
		@Key
		private String type;
		
		public Phone(String p, String t) {
			phone = p;
			type = t;
		}
		
		public String getNumber() {
			return phone;
		}
		
		public String getType() {
			return type;
		}
		
		public Phone() {}
	}
	
	public Contact(String owner_id, String name, String phone, int phoneType, String email, int emailType) {
		super();
		this.name = name;
		this.owner_id = owner_id;
		
		this.phone = new ArrayList();
		this.email = new ArrayList();
		
		addPhone(phone, phoneType);
		addEmail(email, emailType);
	}
	
	public void addPhone(String phone, int type) {        
		Phone p = new Phone(phone, getType(type));
		this.phone.add(p);
	}
	
	public void addEmail(String email, int type) {
		Email e = new Email(email, getType(type));
		this.email.add(e);
	}
	
	public List<Phone> getPhone() {
		return phone;
	}
	
	public List<Email> getEmail() {
		return email;
	}
	
	private String getType(int type) {
		String type_string;
		switch(type) {
			case 0: 
				type_string = "home";
				break;
			case 1:
				type_string = "work";
				break;
			case 2:
				type_string = "mobile";
				break;
			case 3:
				type_string = "other";
				break;
			default:
				type_string = "other";
				break;
		}
		return type_string;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
