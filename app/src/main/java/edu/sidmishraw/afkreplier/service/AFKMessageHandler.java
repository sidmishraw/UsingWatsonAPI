package edu.sidmishraw.afkreplier.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by sidmishraw on 10/15/16.
 */
public class AFKMessageHandler extends Service {

    // notice the naming pattern for android
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    // String - message that needs to be sent to the starred caller
    private String inputMessage;

    //adding a broadcast receiver to the service
    private  CallReceiver callReceivingService;

    // Having the Service Handler as an inner-class saves clutter since I know
    // I'm not gonna use this in another place anyways
    // so I make it a private inner class in order to avoid cluttering
    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {

            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            System.out.println("handling message");
            Toast.makeText(AFKMessageHandler.this, "handling message for service", Toast.LENGTH_SHORT);
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {

                Thread.sleep(5000);
            } catch (InterruptedException e) {

                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread handlerThread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);

        //starting the handler thread
        handlerThread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper       = handlerThread.getLooper();

        // fetch an instance of the service handler
        // I think I should be having just 1 instance for the service handler
        // given I have just 1 instance for my service
        serviceHandler      = new ServiceHandler(serviceLooper);

        // get instance of the broadcast receiver
        callReceivingService = CallReceiver.getInstance();

        callReceivingService.setMessageText(inputMessage);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("started");

        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();

        inputMessage = intent.getExtras().getString("MESSAGE_TEXT");

        callReceivingService.setMessageText(inputMessage);

        System.out.println(inputMessage);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg     = serviceHandler.obtainMessage();
        msg.arg1        = startId;

        //serviceHandler.sendMessage(msg);

        // I register my receiver in the onStartCommand of my service since this starts and stops my service
        // Register my broadcast receiver in this service
        // Create an intent filter
        IntentFilter filterBReceiver = new IntentFilter();

        filterBReceiver.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

        // Registering my receiver
        registerReceiver(callReceivingService, filterBReceiver);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        unregisterReceiver(callReceivingService);

        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
