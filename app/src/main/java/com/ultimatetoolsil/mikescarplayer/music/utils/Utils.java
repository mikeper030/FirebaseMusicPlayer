package com.ultimatetoolsil.mikescarplayer.music.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaMetadataRetriever;

import com.ultimatetoolsil.mikescarplayer.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;



public  class Utils {
    public static boolean fileDownloaded(String s ,String folder) {
        File file= new File(folder+"/"+s);
        return file.exists();
    }

    public static Bitmap songArt (String path, Context context){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        InputStream inputStream;
        retriever.setDataSource(path);
        if (retriever.getEmbeddedPicture()!=null){
            inputStream= new ByteArrayInputStream(retriever.getEmbeddedPicture());
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            retriever.release();
            return bitmap;
        }else {
            return getLargeIcon(context);
        }
    }
    public static Bitmap getLargeIcon(Context context) {
        VectorDrawable vectorDrawable = (VectorDrawable) context.getResources().getDrawable(R.drawable.music_notification);

//        final VectorDrawable vectorDrawable = (VectorDrawable)context.getDrawable(R.drawable.music_notification);

        final int largeIconSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_dim);
        final Bitmap bitmap = Bitmap.createBitmap(largeIconSize, largeIconSize, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.setAlpha(100);
            vectorDrawable.draw(canvas);
        }

        return bitmap;
    }
}
