
package com.kavya.automessaging.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

/**
 * A simple class that denotes unread conversations and messages. In a real world application,
 * this would be replaced by a content provider that actually gets the unread messages to be
 * shown to the user.
 */
public class Conversations {


    public static class Conversation {

        private final int conversationId;

        private final String participantName;

        /**
         * A given conversation can have a single or multiple messages.
         * Note that the messages are sorted from *newest* to *oldest*
         */
        private final List<String> messages;

        private final long timestamp;

        public Conversation(int conversationId, String participantName,
                            List<String> messages) {
            this.conversationId = conversationId;
            this.participantName = participantName;
            this.messages = messages == null ? Collections.<String>emptyList() : messages;
            this.timestamp = System.currentTimeMillis();
        }

        public int getConversationId() {
            return conversationId;
        }

        public String getParticipantName() {
            return participantName;
        }

        public List<String> getMessages() {
            return messages;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String toString() {
            return "[Conversation: conversationId=" + conversationId +
                    ", participantName=" + participantName +
                    ", messages=" + messages +
                    ", timestamp=" + timestamp + "]";
        }
    }

    private Conversations() {
    }
    
    

//    @SuppressLint("NewApi") public static Conversation[] getUnreadConversations(HashMap<String, List<String>> msgInfo,
//                                                        Context context) {
//        Conversation[] conversations = new Conversation[msgInfo];
////        for (int i = 0; i < howManyConversations; i++) {
////            conversations[i] = new Conversation(
////                    ThreadLocalRandom.current().nextInt(),
////                    name(), makeMessages(messagesPerConversation));
////        }
//        return conversations;
//    }
//    
    

//    @SuppressLint("NewApi")
//    private static List<String> makeMessages(int messagesPerConversation) {
//        int maxLen = MESSAGES.length;
//        List<String> messages = new ArrayList<>(messagesPerConversation);
//        for (int i = 0; i < messagesPerConversation; i++) {
//            messages.add(MESSAGES[ThreadLocalRandom.current().nextInt(0, maxLen)]);
//        }
//        return messages;
//    }

//    @SuppressLint("NewApi") 
//    private static String name() {
//        return PARTICIPANTS[ThreadLocalRandom.current().nextInt(0, PARTICIPANTS.length)];
//    }



	public static void updateMessageBox(String originatingAddress,
			String messageBody) {
		
		
	}



	@SuppressLint("NewApi") public static Conversation[] getUnreadConversations(
			HashMap<String, List<String>> msgInfo ,Context context) {
		
//		String numbers[] = new String[10];
//		ArrayList<String> keys = new ArrayList<String>();
		DBAdapter db = new DBAdapter(context);
		db.open();
			
		List<String> values  = new ArrayList<String>();
		 List<Conversation> conversationsList = new ArrayList<Conversation>();
		//int i=1;
    	for (Map.Entry<String, List<String>> entry : msgInfo.entrySet()) {
    		String key = entry.getKey();
    		values = entry.getValue();
    		Log.d("Kavya","Key"+key);
    		Log.d("Kavya","Value"+values);
    
    		System.out.println("Key = " + key);
    		System.out.println("Values = " + values + "n");

    		 Conversation[] messageConversations = new Conversation[values.size()];
    		
    	for(int keyIndex=0;keyIndex<values.size();keyIndex++){ 
    		 messageConversations[keyIndex] = new Conversation(
                    ThreadLocalRandom.current().nextInt(),
                    key, values);
    		 conversationsList.add(messageConversations[keyIndex]);
    	}
    	
    	}
    
        Conversation[] conversations = conversationsList.toArray(new Conversation[conversationsList.size()]);
	
		return conversations;
	}
	
	
	/*public List<String> makeMessages(int numberofMessages){
		
		List<String> messages = new ArrayList<String>(numberofMessages);
		
		for(int messageIndex=0;messageIndex<numberofMessages;messageIndex++){
			
		}
		
		return messages;
		
	}*/
}
