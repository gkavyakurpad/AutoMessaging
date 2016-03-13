
package com.kavya.automessaging.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.kavya.automessaging.R;
import com.kavya.automessaging.data.Conversations;
import com.kavya.automessaging.data.DBAdapter;
import com.kavya.automessaging.logger.MessageLogger;


public class MessagingService extends Service {
    private static final String TAG = MessagingService.class.getSimpleName();

    public static final String READ_ACTION =
            "com.example.android.messagingservice.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION =
            "com.example.android.messagingservice.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    public static final int MSG_SEND_NOTIFICATION = 1;
    public static final String EOL = "\n";

    private NotificationManagerCompat mNotificationManager;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private Context context;
   
	
    
    /**
    * Handler of incoming messages from clients.
    */
   class IncomingHandler extends Handler {
        @Override
       public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_NOTIFICATION:
            
                	
                	@SuppressWarnings("unchecked")
					HashMap<String,List<String>> hashMsg = (HashMap<String, List<String>>) msg.obj ;
                	

                	
                    sendNotification(hashMsg);
                    getReadMessages();            	
                    break;
                default:
                   super.handleMessage(msg);
           }
        }
    }

   private void getReadMessages(){
	   
	   DBAdapter db = new DBAdapter(getApplicationContext());
	   HashMap<String, List<String>> readMessages = new HashMap<String, List<String>>();

	   db.open();
	   Cursor cursor = db.getTitle(0);

		if (cursor.moveToFirst()) {
			do

			{
				String incomingNumber = cursor.getString(0);
				String messageText = cursor.getString(1);
				
				List<String> msg = readMessages.get(incomingNumber);
				
				if(msg == null) {
					msg = new ArrayList<String>();
					readMessages.put(incomingNumber, msg);
				}
				
				msg.add(messageText);
				readMessages.put(incomingNumber, msg);
			} while (cursor.moveToNext());
		}
		Conversations.Conversation[] conversations = Conversations.getUnreadConversations(
				readMessages , context );
        for (Conversations.Conversation conv : conversations) {
            sendNotificationForConversation(conv);
        }
	   
   }
   
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        context = getApplicationContext();
    }

   @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    // Creates an intent that will be triggered when a message is marked as read.
    private Intent getMessageReadIntent(int id) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(READ_ACTION)
                .putExtra(CONVERSATION_ID, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    @SuppressLint("InlinedApi") private Intent getMessageReplyIntent(int conversationId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(REPLY_ACTION)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void sendNotification(HashMap<String,List<String>> msgInfo) {
        Conversations.Conversation[] conversations = Conversations.getUnreadConversations(
                msgInfo ,context);
        DBAdapter db = new DBAdapter(getApplicationContext());
        db.open();
        
        System.out.println("CONVERSATION SIZE"+" "+conversations.length);
        for (Conversations.Conversation conv : conversations) {
        	String key = conv.getParticipantName();
        	List<String> messagesList = conv.getMessages();
        	String[] messageArray = messagesList.toArray(new String[messagesList.size()]);
        	for(int messageIndex=0;messageIndex<messageArray.length;messageIndex++){
        		 boolean update = db.updateNote(key, messageArray[messageIndex], 1);
        		 System.out.println("Update for "+key+" : "+messageArray[messageIndex]+" "+update);
        	}
        	
            sendNotificationForConversation(conv);
        }
    }

    private void sendNotificationForConversation(Conversations.Conversation conversation) {
        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversation.getConversationId(),
                getMessageReadIntent(conversation.getConversationId()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a RemoteInput for receiving voice input in a Car Notification
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(getApplicationContext().getString(R.string.notification_reply))
                .build();

        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversation.getConversationId(),
                getMessageReplyIntent(conversation.getConversationId()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        UnreadConversation.Builder unreadConvBuilder =
                new UnreadConversation.Builder(conversation.getParticipantName())
                .setLatestTimestamp(conversation.getTimestamp())
                .setReadPendingIntent(readPendingIntent)
                .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder
        StringBuilder messageForNotification = new StringBuilder();
        System.out.println("MESSAGE COUNT"+" "+conversation.getMessages().size());
        for (Iterator<String> messages = conversation.getMessages().iterator();
             messages.hasNext(); ) {
            String message = messages.next();
            System.out.println("MESSAGES"+" "+message);
            unreadConvBuilder.addMessage(message);
            messageForNotification.append(message);
            if (messages.hasNext()) {
                messageForNotification.append(EOL);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
             //   .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), R.drawable.android_contact))
                .setContentText(messageForNotification.toString())
                .setWhen(conversation.getTimestamp())
                .setContentTitle(conversation.getParticipantName())
                .setContentIntent(readPendingIntent)
                .extend(new CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(getApplicationContext()
                                .getResources().getColor(R.color.default_color_light)));
        
        

        MessageLogger.logMessage(getApplicationContext(), "Sending notification "
                + conversation.getConversationId() + " conversation: " + conversation);

        mNotificationManager.notify(conversation.getConversationId(), builder.build());
    }

	
}
