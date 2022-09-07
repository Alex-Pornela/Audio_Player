package com.activity.audioplayer;

import static com.activity.audioplayer.ApplicationClass.ACTION_CLOSE;
import static com.activity.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.activity.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.activity.audioplayer.ApplicationClass.ACTION_PREV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context,AudioPlayerService.class);
        if(actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra( "ActionName", "playPause" );
                    context.startService( serviceIntent );
                    break;
                case ACTION_PREV:
                    serviceIntent.putExtra( "ActionName", "previous" );
                    context.startService( serviceIntent );
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra( "ActionName", "next" );
                    context.startService( serviceIntent );
                    break;
                case ACTION_CLOSE:
                    serviceIntent.putExtra( "ActionName", "close" );
                    context.startService( serviceIntent );
                    break;
            }
        }
    }
}
