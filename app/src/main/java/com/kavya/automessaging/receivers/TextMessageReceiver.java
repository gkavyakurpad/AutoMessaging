package com.kavya.automessaging.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.kavya.automessaging.ReceiveMessageActivity;
import com.kavya.automessaging.data.DBAdapter;

public class TextMessageReceiver extends BroadcastReceiver {

    
	private DBAdapter dbAdapter;
	
	
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		Object[] messages = (Object[]) bundle.get("pdus");
		SmsMessage[] sms = new SmsMessage[messages.length];

		for (int n = 0; n < messages.length; n++) {
			sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		}
		
		dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		

		for (SmsMessage msg : sms) {
			dbAdapter.insertTitle(msg.getOriginatingAddress(), msg.getMessageBody() ,"0");

			//messagesContent.put(msg.getMessageBody(),msg.getOriginatingAddress());
		}
//		List<String> keyList = new ArrayList<String>(messagesContent.keySet());
//		List<String> valueList = new ArrayList<String>(messagesContent.values());
		
		Intent i = new Intent(context , ReceiveMessageActivity.class);
		//i.putExtra("message", messagesContent);
	//	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		
		context.startActivity(i);
		

	}

}