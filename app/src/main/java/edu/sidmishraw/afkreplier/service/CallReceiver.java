package edu.sidmishraw.afkreplier.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by sidmishraw on 10/15/16.
 * By declaring this as a receiver in the manifest file, this is made singleton
 * no need to make it singleton explicitly
 *
 * If not declared in manifest file, can explicitly be made singleton using private construcutor
 */

public class CallReceiver extends BroadcastReceiver {

    private static CallReceiver callReceiver = null;

    private String messageText;

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    private CallReceiver() {}

    public static CallReceiver getInstance() {

        if ( null == callReceiver ) {

            callReceiver = new CallReceiver();
        }

        return callReceiver;
    }

    // SMS delivered successfully - receiver
    private class SmsDeliveredReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent arg1) {

            switch ( getResultCode() ) {

                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // SMS sent successfully receiver
    private class SmsSentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent arg1) {

            switch (getResultCode()) {

                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show();
                    break;

                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    break;

                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * Sending message method, uses PendingIntents to send the message
     * Uses sms.divide since the message size can be large, multipart message
     * so if the message is very large, then the message can be broken down into smaller messages
     * that way we'll be needing multiple intents for each part
     * hence the arraylist
     * @param phoneNumber
     * @param message
     */
    private void sendSMS(Context context, String phoneNumber, String message) {

        ArrayList<PendingIntent> sentPendingIntents         = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents    = new ArrayList<PendingIntent>();

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsDeliveredReceiver.class), 0);

        try {

            SmsManager smsManager           = SmsManager.getDefault();

            // breaking down the large message to multiple parts for sending
            ArrayList<String> mSMSMessage   = smsManager.divideMessage(message);

            for (int i = 0; i < mSMSMessage.size(); i++) {

                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }

            smsManager.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);
        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(context, "SMS sending failed...",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Silences the ringing of the phone after the SMS is sent to the user
     * @param context
     * @param intent
     */
    private void silenceRinging(Context context, Intent intent) {

        // drop the call
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        Method m = null;

        try {

            m = Class.forName(telephonyManager.getClass().getName()).getDeclaredMethod("getITelephony");

            m.setAccessible(true);

            ((ITelephony) m.invoke(telephonyManager)).silenceRinger();
            ((ITelephony) m.invoke(telephonyManager)).endCall();
        } catch (NoSuchMethodException e) {

            Toast.makeText(context, e.getStackTrace().toString(), Toast.LENGTH_LONG);
        } catch (InvocationTargetException e ) {

            Toast.makeText(context, e.getStackTrace().toString(), Toast.LENGTH_LONG);
        } catch ( Exception e ) {

            Toast.makeText(context, e.getStackTrace().toString(), Toast.LENGTH_LONG);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        if( intent.getStringExtra(TelephonyManager.EXTRA_STATE).equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING) ) {

            Toast.makeText(context, "Incoming from : " + intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER), Toast.LENGTH_LONG).show();
            System.out.println("Incoming from : " + intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));

            // fetch the content resolver from the context in the onreceive of receiver
            ContentResolver contentResolver = context.getContentResolver();

            Uri uri                         = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)));

            Cursor mycursor                 = contentResolver.query(
                    uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.STARRED},null,null, null);

            // check if the number is of someone who is in starred contacts -- Important person
            // send them a message saying that you are driving and will get back to them soon.
            if ( mycursor!=null && mycursor.moveToFirst() ) {

                System.out.println("Sending message to:" +
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) + " message : " + messageText );

 /*               SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER), null, messageText, null, null);
*/

                sendSMS(context,intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER), messageText);

                silenceRinging(context, intent);
            } else {

                silenceRinging(context, intent);
            }
        } else {

            System.out.println("doing nuthin at the moment!");
        }
    }
}
