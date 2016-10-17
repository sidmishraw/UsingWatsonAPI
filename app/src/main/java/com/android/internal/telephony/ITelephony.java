package com.android.internal.telephony;

/**
 * Created by sidmishraw on 10/16/16.
 */
public interface ITelephony {

    public boolean endCall();

    public void answerRingingCall();

    public void silenceRinger();
}
