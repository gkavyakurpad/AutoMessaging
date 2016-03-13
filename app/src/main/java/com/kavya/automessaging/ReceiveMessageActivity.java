package com.kavya.automessaging;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.kavya.automessaging.adapter.ExpandableListAdapter;
import com.kavya.automessaging.data.DBAdapter;
import com.kavya.automessaging.logger.MessageLogger;
import com.kavya.automessaging.services.MessagingService;


@SuppressLint("NewApi")
public class ReceiveMessageActivity extends Activity {

	private ExpandableListView messageContent;
	private HashMap<String, List<String>> totalMessages = new HashMap<String, List<String>>();
	private HashMap<String, List<String>> unreadMessages = new HashMap<String, List<String>>();
	private List<String> numbers = new ArrayList<String>();

	private DBAdapter dbAdapter;
	private Cursor cursor;

	private ExpandableListAdapter listAdapter;

	private Messenger mService;
	private boolean mBound;
	
	private TextView noMsgText;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		noMsgText = (TextView) findViewById(R.id.emptyMessage);
		
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();

		messageContent = (ExpandableListView) findViewById(R.id.messageContent);
		cursor = dbAdapter.getTitle(0);

		if (cursor.moveToFirst()) {
			do

			{
				String incomingNumber = cursor.getString(0);
				String messageText = cursor.getString(1);
				String readMessage = cursor.getString(2);
				
				List<String> msg = unreadMessages.get(incomingNumber);
				
				if(msg == null) {
					msg = new ArrayList<String>();
					unreadMessages.put(incomingNumber, msg);
				}
				
				msg.add(messageText);
				unreadMessages.put(incomingNumber, msg);
			} while (cursor.moveToNext());
		}

		// messages = (HashMap<String, String>)
		// getIntent().getSerializableExtra("message");
	
		//get readMessages and then add to totalMessages hashMap.
		cursor = null;
		cursor = dbAdapter.getAllTitles();
		
		if (cursor.moveToFirst()) {
			do

			{
				String incomingNumber = cursor.getString(1);
				String messageText = cursor.getString(2);
				
				List<String> msg = totalMessages.get(incomingNumber);
				
				if(msg == null) {
					msg = new ArrayList<String>();
					totalMessages.put(incomingNumber, msg);
				}
				
				msg.add(messageText);
				totalMessages.put(incomingNumber, msg);
			} while (cursor.moveToNext());
		}
		
		for (Entry<String, List<String>> entry : totalMessages.entrySet()) {
			numbers.add(entry.getKey());
		}
		
		if (totalMessages != null && totalMessages.size() != 0) {
			listAdapter = new ExpandableListAdapter(this, numbers, totalMessages);
			messageContent.setAdapter(listAdapter);
		} else {
			noMsgText.setVisibility(View.VISIBLE);
			messageContent.setVisibility(View.GONE);
		}

	}

	public void onStart() {
		super.onStart();
		bindService(new Intent(this, MessagingService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mService = new Messenger(service);
			mBound = true;
			sendMsg(unreadMessages);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mService = null;
			mBound = false;
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onPause() {
		super.onPause();
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();
	}

	@SuppressLint("NewApi")
	@Override
	public void onStop() {
		super.onStop();
		if (mBound) {
			this.unbindService(mConnection);
			mBound = false;
		}
	}

	private void sendMsg(HashMap<String, List<String>> unReadmessageMap) {
		if (mBound) {
			Message msg = Message.obtain(null,
					MessagingService.MSG_SEND_NOTIFICATION, unReadmessageMap);
			try {
				mService.send(msg);
			} catch (RemoteException e) {
				Log.e("ReceiveMessageActivity", "Error sending a message", e);
				MessageLogger.logMessage(this,
						"Error occurred while sending a message.");
			}
		}
	}

}
