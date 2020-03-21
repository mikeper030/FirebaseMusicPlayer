package com.ultimatetoolsil.mikescarplayer.music.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.ultimatetoolsil.mikescarplayer.R;


public class MusicService extends Service {
    private int NOTIFICATION_ID=3;
    private final IBinder mIBinder = new LocalBinder();
    private MediaPlayerHolder.progressUpdate progressUpdate;
    private MediaPlayerHolder mMediaPlayerHolder;

    private MusicNotificationManager mMusicNotificationManager;

    private boolean sRestoredFromPause = false;

    public final boolean isRestoredFromPause() {
        return sRestoredFromPause;
    }

    public void setRestoredFromPause(boolean restore) {
        sRestoredFromPause = restore;
    }

    public final MediaPlayerHolder getMediaPlayerHolder() {
        return mMediaPlayerHolder;
    }

    public MusicNotificationManager getMusicNotificationManager() {
        return mMusicNotificationManager;
    }
    public void setProgressUpdate(MediaPlayerHolder.progressUpdate progressUpdate){
        mMediaPlayerHolder.setProgressUpdate(progressUpdate);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {



            String NOTIFICATION_CHANNEL_ID = "com.ultimatetoolsil.mikescarplayer";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            chan.setImportance(NotificationManager.IMPORTANCE_MIN);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
            }



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {



            String NOTIFICATION_CHANNEL_ID = "com.ultimatetoolsil.mikescarplayer";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
           chan.setImportance(NotificationManager.IMPORTANCE_MIN);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mMediaPlayerHolder.registerNotificationActionsReceiver(false);
        mMusicNotificationManager = null;
        mMediaPlayerHolder.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (mMediaPlayerHolder == null) {
            mMediaPlayerHolder = new MediaPlayerHolder(this);
            mMusicNotificationManager = new MusicNotificationManager(this);
            mMediaPlayerHolder.registerNotificationActionsReceiver(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, "com.ultimatetoolsil.mikescarplayer")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("music serivce runing..")
                    .setAutoCancel(false)
                    .setOngoing(true);

            Notification notification = builder.build();
            startForeground(2, notification);
        }
        return mIBinder;
    }

    public class LocalBinder extends Binder {
        public MusicService getInstance() {
            return MusicService.this;
        }
    }
}
