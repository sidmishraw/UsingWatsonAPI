package edu.sidmishraw.afkreplier.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import edu.sidmishraw.afkreplier.R;
import edu.sidmishraw.afkreplier.service.AFKMessageHandler;

/**
 * Created by sidmishraw on 10/15/2016
 */
public class MainActivity extends AppCompatActivity {

    private static TextView statusText;
    private static EditText inputSentence;
    private static Button serviceHandlerButton;

    private static String statusMessage    = null;
    private static String messageValue     = null;
    private static String buttonValue      = null;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onPause() {

        super.onPause();

        buttonValue     = serviceHandlerButton.getText().toString();
        messageValue    = inputSentence.getText().toString();
        statusMessage   = statusText.getText().toString();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("STATUSMESSAGE",
                null != statusMessage ? statusMessage : "SERVICE IS OFF" );
        savedInstanceState.putString("MESSAGEVALUE",
                null != messageValue ? messageValue : "Enter your message here ..." );
        savedInstanceState.putString("BUTTONVALUE",
                null != buttonValue ? buttonValue : "ON" );
    }

    @Override
    protected void onResume() {

        super.onResume();

        statusText.setText(statusMessage);
        inputSentence.setText(messageValue);
        serviceHandlerButton.setText(buttonValue);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        statusText.setText(savedInstanceState.getString("STATUSMESSAGE"));
        inputSentence.setText(savedInstanceState.getString("MESSAGEVALUE"));
        serviceHandlerButton.setText(savedInstanceState.getString("BUTTONVALUE"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //added by sidmishraw for accessing the widgets
        statusText = (TextView) findViewById(R.id.statusText);
        inputSentence = (EditText) findViewById(R.id.inputSentence);
        serviceHandlerButton = (Button) findViewById(R.id.serviceHandlerButton);

        if ( null != savedInstanceState ) {

            buttonValue     = null == savedInstanceState.getString("BUTTONVALUE")
                    ? serviceHandlerButton.getText().toString() : savedInstanceState.getString("BUTTONVALUE");
            messageValue    = null == savedInstanceState.getString("MESSAGEVALUE")
                    ? inputSentence.getText().toString() : savedInstanceState.getString("MESSAGEVALUE");
            statusMessage   = null == savedInstanceState.getString("STATUSMESSAGE")
                    ? statusText.getText().toString() : savedInstanceState.getString("STATUSMESSAGE");
        } else {

            buttonValue     = serviceHandlerButton.getText().toString();
            messageValue    = inputSentence.getText().toString();
            statusMessage   = statusText.getText().toString();
        }

        //setting an actionlistener for the button click event
        //start my edu.sidmishraw.afkreplier.service when clicking on the button
        serviceHandlerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intentForService = new Intent(MainActivity.this, AFKMessageHandler.class);

                //sending the input message to the service
                if ( null == intentForService.getExtras() ) {

                    Bundle bundle = new Bundle();
                    bundle.putString("MESSAGE_TEXT", inputSentence.getText().toString());

                    intentForService.putExtras(bundle);
                } else {

                    intentForService.getExtras().putString("MESSAGE_TEXT", inputSentence.getText().toString());
                }

                // when the button is ON, edu.afkreplier.afkreplier.edu.afkreplier.afkreplier.service is started, else edu.afkreplier.afkreplier.edu.afkreplier.afkreplier.service is stopped.
                if (statusMessage.equalsIgnoreCase("SERVICE IS ON")) {

                    statusMessage   = "SERVICE IS OFF";
                    buttonValue     = "ON";

                    statusText.setText(statusMessage);
                    serviceHandlerButton.setText(buttonValue);
                    stopService(intentForService);
                } else {

                    statusMessage   = "SERVICE IS ON";
                    buttonValue     = "OFF";

                    statusText.setText(statusMessage);
                    serviceHandlerButton.setText(buttonValue);

                    startService(intentForService);
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static TextView getStatusText() {
        return statusText;
    }

    public static EditText getInputSentence() {
        return inputSentence;
    }

    public static Button getServiceHandlerButton() {
        return serviceHandlerButton;
    }

    public static void setServiceHandlerButton(Button serviceHandlerButton) {
        MainActivity.serviceHandlerButton = serviceHandlerButton;
    }

    public static void setStatusText(TextView statusText) {
        MainActivity.statusText = statusText;
    }

    public static void setInputSentence(EditText inputSentence) {
        MainActivity.inputSentence = inputSentence;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
