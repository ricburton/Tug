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

	// The method below is used for matching the last characters of the string
	public class SubStringEx {
		public String getLastnCharacters(String inputString, int subStringLength) {
			int length = inputString.length();
			if (length <= subStringLength) {
				return inputString;
			}
			int startIndex = length - subStringLength;
			return inputString.substring(startIndex);
		}
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
					Log.v(DEBUG_TAG,
							"Got a contact result: " + result.toString());

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

					// START THE STAT ENGINE!
					Uri uriSMSURI = Uri.parse("content://sms"); // access the
																// sms db
					Cursor cur = getContentResolver().query(uriSMSURI, null,
							null, null, null);

					// set the data to be collected

					Integer total = 0;
					Integer sent = 0;
					Integer received = 0;
					Integer kissesSent = 0;
					Integer kissesReceived = 0;
					Integer questionsSent = 0;
					Integer questionsReceived = 0;

					while (cur.moveToNext()) {

						// TODO look at the number outside the first zero.
						// Matching the number +27 (84) 5543262 to 0845543262 to
						// +27845543262
						// That should be done from end to beginning

						/**
						 * String ssn ="123456789"; SubStringEx subEx = new
						 * SubStringEx(); String last4Digits =
						 * subEx.getLastnCharacters(ssn,4);
						 * System.out.println("Last 4 digits are " +
						 * last4Digits); //will print 6789
						 * 
						 */

						// set the rat to be analysed
						String rat = phone;
						// strip the last 9 digits
						SubStringEx subRatter = new SubStringEx(); //new SubStringEx object from the class
						String cleanRat = subRatter.getLastnCharacters(rat,9);
						
						// get the raw number of the message
						String num = cur.getString(2);
						// string the last 9 digits
						SubStringEx subNumber = new SubStringEx(); //TODO this could be code-bloat?
						//can you just use the method and not need a new SubStringEx object?
						
						String cleanNum = subNumber.getLastnCharacters(num, 9);
						
						

						if (cleanRat.equals(cleanNum)) {
							total++;

							// messages sent
							if (cur.getInt(8) == 2) {
								sent++;

								// kisses sent
								if (cur.getString(11).indexOf(" x ") > 0
										|| cur.getString(11).indexOf(" x") > 0) {
									kissesSent++;
								}
								// question-marks sent
								if (cur.getString(11).indexOf("?") > 0) {
									questionsSent++;
								}
							} else if (cur.getInt(8) == 1) { // messages
																// received
								received++;
								Log.d(rat,
										"Message Received: "
												+ cur.getString(11));
								// kisses received
								if (cur.getString(11).indexOf(" x ") > 0
										|| cur.getString(11).indexOf(" x") > 0) {
									kissesReceived++;
								}
								// question-marks received
								if (cur.getString(11).indexOf("?") > 0) {
									questionsReceived++;
									Log.d(rat, "Question Received");
								}
							} else {
								// TODO uh oh
								Log.d(rat, "No match to rat");
							}

						} else {
							// log no texts match to person here
						}

					}

					// TODO present the data nicely

					// Total texts

					TextView totalTexts = (TextView) findViewById(R.id.total_texts);
					String total_report = Integer.toString(total); // TODO add
																	// more
																	// words
					totalTexts.setText(total_report);

					// Messages Row
					TextView messagesSent = (TextView) findViewById(R.id.MessagesSent);
					String sent_count = Integer.toString(sent);
					messagesSent.setText(sent_count);

					TextView messagesReceived = (TextView) findViewById(R.id.MessagesReceived);
					String received_count = Integer.toString(received);
					messagesReceived.setText(received_count);

					// Questions Row
					TextView questionsSentCounter = (TextView) findViewById(R.id.QuestionsSent);
					String questions_sent = Integer.toString(questionsSent);
					questionsSentCounter.setText(questions_sent);

					TextView questionsReceivedCounter = (TextView) findViewById(R.id.QuestionsReceived);
					String questions_received = Integer
							.toString(questionsReceived);
					questionsReceivedCounter.setText(questions_received);

					// Kisses Row
					TextView kissesSentCounter = (TextView) findViewById(R.id.KissesSent);
					String kisses_sent = Integer.toString(kissesSent);
					kissesSentCounter.setText(kisses_sent);

					TextView kissesReceivedCounter = (TextView) findViewById(R.id.KissesReceived);
					String kisses_received = Integer.toString(kissesReceived);
					kissesReceivedCounter.setText(kisses_received);

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