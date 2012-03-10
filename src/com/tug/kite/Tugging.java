package com.tug.kite;

import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
					
					// declare the ArrayList of reply-time integers
					Integer lastMessageStatus = 0; // sent = 2, received = 1
					Integer lastMessageTime = 0;
					ArrayList<Integer> replySpeeds = new ArrayList<Integer>();
					ArrayList<Integer> sendSpeeds = new ArrayList<Integer>();

					// Double-texts
					Integer sentDoubles = 0;
					Integer receivedDoubles = 0;

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
						SubStringEx subRatter = new SubStringEx(); // new
																	// SubStringEx
																	// object
																	// from the
																	// class
						String cleanRat = subRatter.getLastnCharacters(rat, 9);

						// get the raw number of the message
						String num = cur.getString(2);
						// string the last 9 digits
						SubStringEx subNumber = new SubStringEx(); // TODO this
																	// could be
																	// code-bloat?
						// can you just use the method and not need a new
						// SubStringEx object?

						String cleanNum = subNumber.getLastnCharacters(num, 9);

						if (cleanRat.equals(cleanNum)) {
							Integer messageStatus = cur.getInt(8);
							Integer replyTime = cur.getInt(4);
							total++;
							// messages sent
							if (messageStatus == 2) {
								sent++;
								Log.d(rat, "Message Sent: " + cur.getString(11));

								// see if this is a reply or a follow-up text
								if (lastMessageStatus == 2) { 
									sentDoubles++;
								}
								else if (lastMessageStatus == 1) {
									Integer sendDiff =  lastMessageTime - replyTime;
									sendSpeeds.add(sendDiff);
									Log.d("Difference in time", sendDiff.toString());
								}
								 
								// kisses sent
								if (cur.getString(11).indexOf(" x ") > 0
										|| cur.getString(11).indexOf(" x") > 0) {
									kissesSent++;
								}
								// question-marks sent
								if (cur.getString(11).indexOf("?") > 0) {
									questionsSent++;
								}
							} else if (messageStatus == 1) { // messages
																// received
								received++;

								// see if this a reply or a follow-up text
								if (lastMessageStatus == 1) {
									receivedDoubles++;
								} else if (lastMessageStatus == 2) {
									Integer replyDiff =  lastMessageTime - replyTime;
									replySpeeds.add(replyDiff);
									Log.d("Difference in time", replyDiff.toString());
								}
								 

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

								Log.d(rat, "Not sent or received. Odd");
							}

							// set the LastMessageStatus for next loop
							lastMessageStatus = messageStatus;
							lastMessageTime = replyTime;
						} else {
							// log no texts match to person here
							Log.d(rat, "Not a match to rat.");
						}

					}

					Log.i("Sent Doubles", sentDoubles.toString());
					Log.i("Received Doubles", receivedDoubles.toString());
					Log.i("Send Speeds", sendSpeeds.toString());
					Log.i("Reply Speeds", replySpeeds.toString());
					
					//GRAPHING TIME
					
					//replySpeeds
					
					Integer replyNum = replySpeeds.size();
					GraphViewData[] replyData = new GraphViewData[replyNum];
					
					Log.i("About to spin data in", "DATA GOING IN");
					for(int i=0;i<replyNum;i++) {
						replyData[i] = new GraphViewData(i, replySpeeds.get(i));
						Log.i("pushing reply data", replySpeeds.get(i).toString());
					}
					
					GraphViewSeries seriesReplies = new GraphViewSeries("Reply Speeds", Color.rgb(200, 50, 00), replyData);  
					
					/** //sendSpeeds
					Integer sendNum = replySpeeds.size();
					GraphViewData[] sendData = new GraphViewData[sendNum];
					
					for(int i=0;i<sendNum;i++) {
						sendData[i] = new GraphViewData(i, sendSpeeds.get(i));
						Log.i("pushing reply data", sendSpeeds.get(i).toString());
					}
					
					GraphViewSeries seriesSent = new GraphViewSeries("Reply Speeds", Color.rgb(200, 50, 00), sendData);  
					
					*/
					LinearLayout graphBox = (LinearLayout) findViewById(R.id.graph1);
			        GraphView graphView = new LineGraphView(this , "Reply Speeds");

			        graphView.addSeries(seriesReplies);
			     // set legend  
			        graphView.setShowLegend(true);  
			        graphView.setLegendAlign(LegendAlign.BOTTOM); 
			        graphView.setLegendWidth(200);
			       
			        Log.d("Update Graph", "About to update...");
			        
			        graphBox.addView(graphView);

					// Total texts

					TextView totalTexts = (TextView) findViewById(R.id.total_texts);
					totalTexts.setText("Messages: " + total.toString());

					// Messages Row
					TextView messagesSent = (TextView) findViewById(R.id.MessagesSent);
					messagesSent.setText(sent.toString());

					TextView messagesReceived = (TextView) findViewById(R.id.MessagesReceived);
					messagesReceived.setText(received.toString());

					// Questions Row
					TextView questionsSentCounter = (TextView) findViewById(R.id.QuestionsSent);
					questionsSentCounter.setText(questionsSent.toString());

					TextView questionsReceivedCounter = (TextView) findViewById(R.id.QuestionsReceived);
					questionsReceivedCounter.setText(questionsReceived.toString());

					// Kisses Row
					TextView kissesSentCounter = (TextView) findViewById(R.id.KissesSent);
					kissesSentCounter.setText(kissesSent.toString());

					TextView kissesReceivedCounter = (TextView) findViewById(R.id.KissesReceived);
					kissesReceivedCounter.setText(kissesReceived.toString());

					// Doubles Row
					TextView doublesSentCounter = (TextView) findViewById(R.id.DoublesSent);
					doublesSentCounter.setText(sentDoubles.toString());

					TextView doublesReceivedCounter = (TextView) findViewById(R.id.DoublesReceived);
					doublesReceivedCounter.setText(receivedDoubles.toString());

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