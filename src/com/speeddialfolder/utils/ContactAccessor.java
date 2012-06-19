package com.speeddialfolder.utils;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

class ContactAccessor {

	/**
	 * Retrieves the contact information.
	 */
	public ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri) {

		// contactUri --> content://com.android.contacts/data/1557

		ContactInfo contactInfo = new ContactInfo();

		// Load the display name for the specified person
		Cursor cursor = contentResolver.query(contactUri, new String[] { Contacts._ID,
			Contacts.DISPLAY_NAME, Phone.NUMBER, Contacts.PHOTO_ID }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				contactInfo.setId(cursor.getLong(0));
				contactInfo.setDisplayName(cursor.getString(1));
				contactInfo.setPhoneNumber(cursor.getString(2));
			}
		} finally {
			cursor.close();
		}
		return contactInfo; // <-- returns info for contact
	}

	public Bitmap getPhoto(ContentResolver contentResolver, long contactId) {
		Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);

		// contactPhotoUri --> content://com.android.contacts/contacts/1557

		InputStream photoDataStream = Contacts.openContactPhotoInputStream(contentResolver,
			contactPhotoUri); // <-- always null
		Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
		return photo;
	}

	public class ContactInfo {

		private long id;
		private String displayName;
		private String phoneNumber;
		private Uri photoUri;

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public Uri getPhotoUri() {
			return this.photoUri;
		}

		public void setPhotoUri(Uri photoUri) {
			this.photoUri = photoUri;
		}

		public long getId() {
			return this.id;
		}

		public void setId(long id) {
			this.id = id;
		}

	}
}
