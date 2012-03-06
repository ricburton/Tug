package com.tug.kite;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Tugging extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    private static final int CONTACT_PICKER_RESULT = 1001;
	private static final String DEBUG_TAG = null;

	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Contacts.CONTENT_URI);
		// TODO remove status bar and application name
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (resultCode == RESULT_OK) {  
            switch (requestCode) {  
            case CONTACT_PICKER_RESULT:  
                Cursor cursor = null;  
                String phone = "";  
                try {  
                    Uri result = data.getData();  
                    Log.v(DEBUG_TAG, "Got a contact result: "  
                            + result.toString());  
      
                    // get the contact id from the Uri  
                    String id = result.getLastPathSegment();  
      
                    // query for everything phone  
                    cursor = getContentResolver().query(Phone.CONTENT_URI,  
                            null, Phone.CONTACT_ID + "=?", new String[] { id },  
                            null);  
      
                    int phoneIdx = cursor.getColumnIndex(Phone.DATA);  
      
                    // let's just get the first phone  
                    if (cursor.moveToFirst()) {  
                        phone = cursor.getString(phoneIdx);  
                        Log.v(DEBUG_TAG, "Got phone: " + phone);  
                    } else {  
                        Log.w(DEBUG_TAG, "No results");  
                    }  
                } catch (Exception e) {  
                    Log.e(DEBUG_TAG, "Failed to get phone data", e);  
                } finally {  
                    if (cursor != null) {  
                        cursor.close();  
                    }  
                    
                    TextView phoneEntry = (TextView) findViewById(R.id.phone_number);  
                    phoneEntry.setText(phone); 
                    
                    
                 // START THE STAT ENGINE!
                    Uri uriSMSURI = Uri.parse("content://sms");
                    Cursor cur = getContentResolver().query(uriSMSURI, null,
                    		null, null, null);
                    // set the rat to be analysed
                    String rat = phone;

                    // set the data to be collected
                    //Integer sent = 0;
                    //Integer received = 0;
                    Integer total = 0;

                    // TODO add the scanning algo
                      while (cur.moveToNext()) {
                    	  
                    	  if (rat.equals(cur.getString(2))) {
                    		  total++;
                    		  Log.i(rat,"Match!");
                    	  } else {
                    		  Log.d(rat,"No match to rat");
                    	  }
                      };

                    // TODO present the data nicely

                    //Total texts
                      TextView totalTexts = (TextView) findViewById(R.id.total_texts);
                      String total_report = Integer.toString(total); //TODO add more words
                      totalTexts.setText(total_report);
                    
                    
                    if (phone.length() == 0) {  
                        Toast.makeText(this, "No phone found for contact.",  
                                Toast.LENGTH_LONG).show();  
                    }  
      
                }  
      
                break;  
            }  
      
        } else {  
            Log.w(DEBUG_TAG, "Warning: activity result not ok");  
        }  
    }  
}