package com.ultimatetoolsil.mikescarplayer;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.ultimatetoolsil.mikescarplayer.music.Album;
import com.ultimatetoolsil.mikescarplayer.music.Song;
import com.ultimatetoolsil.mikescarplayer.music.SongsAdapter;
import com.ultimatetoolsil.mikescarplayer.music.SongsFragment;
import com.ultimatetoolsil.mikescarplayer.music.playback.MediaPlayerHolder;
import com.ultimatetoolsil.mikescarplayer.music.playback.MusicNotificationManager;
import com.ultimatetoolsil.mikescarplayer.music.playback.MusicService;
import com.ultimatetoolsil.mikescarplayer.music.playback.PlaybackInfoListener;
import com.ultimatetoolsil.mikescarplayer.music.playback.PlayerAdapter;
import com.ultimatetoolsil.mikescarplayer.music.utils.EqualizerUtils;
import com.ultimatetoolsil.mikescarplayer.music.utils.ResizeAnimation;
import com.ultimatetoolsil.mikescarplayer.music.utils.Utils;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import static com.ultimatetoolsil.mikescarplayer.music.utils.Utils.fileDownloaded;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SongsFragment.SelectionListener{



    private enum State {
        UNINITIALISED, LOADING, SEARCHING, LOADING_PAGE, LOADED, LOADING_DETAIL
    }
    private MusicService mMusicService;
    private Boolean mIsBound;
    public PlayerAdapter mPlayerAdapter;
    private Album selectedAlbum=null;
    private boolean mUserIsSeeking = false;
    private PlaybackListener mPlaybackListener;
    private SongsFragment fragment;
    private MediaRequestGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;
    private ImageButton playPause, next, previous;
    private SeekBar mSeekbar;
    // private ArrayList<Request> mItems= new ArrayList<>();
    private int mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;
    private boolean mEndOfListReached = false;
    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";
    public static final int LOADING_DIALOG_FRAGMENT = 1;
    private State mState = State.UNINITIALISED;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener childEventListener;
    private RecyclerView mRecylcerView;
    private ChildEventListener listener;
    private Toolbar toolbar;
    private ArrayList<Album> albums=new ArrayList<>();
    private ImageButton back;
    private int mPos=0;
    private ImageButton shuffleBtn;
    private ConstraintLayout constraintLayout;
    private boolean mStopHandler=false;
    private MediaPlayerHolder.progressUpdate progressUpdate;
    private TextView songTitle;
    private SongsAdapter.SongItemClickListener clickListener;
    private TextView timePass,timeTotal;
    private MusicNotificationManager mMusicNotificationManager;
    private ImageButton stretch;
    private MediaRequestGridAdapter.OnAlbumClick albumClicklistener;
    private Runnable runnable;
    private  Handler handler=new Handler();
    private boolean isHomeDirected=false;
    private ProgressBar progressBar;
    private ArrayList<Song> songs;
    private boolean isFullScreen=false,isShuffling=false;
    private int  originalPlayerWidth;
    private ImageButton background;
    private ConstraintLayout root;
    private boolean startupLaunch=true;
    private int selectedPos=0;
    private Timer timer;
    private ConstraintLayout recylerWrapper,menuWrapper, constraintLayout2;;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.play_button): {
                Log.d("player","playpause");
                resumeOrPause();
                //mPlayerAdapter.getMediaPlayer().pause();
                break;
            }
            case (R.id.forward_button): {
                mPos++;
                skipNext();
                break;
            }
            case (R.id.rewind_button): {
                mPos--;
                skipPrev();
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecylcerView=findViewById(R.id.recyclerView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        // mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        toolbar=findViewById(R.id.toolbar);
        back=toolbar.findViewById(R.id.toolbar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLayoutManager = new GridLayoutManager(this, mColumns);
        mRecylcerView.setLayoutManager(mLayoutManager);
        doBindService();
        setViews();

        initializeSeekBar();
        prepareListener();
        myRef.child("music").child("English").addChildEventListener(listener);

        originalPlayerWidth=constraintLayout.getWidth();

    }
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {

             switch (keycode){
                 case 87:{
                     mPos++;
                     skipNext();
                     break;
                 }
                 case 88:{
                     mPos--;
                     skipPrev();
                     break;
                 }
             }



        return super.onKeyDown(keycode, e);
    }
    @Override
    public void onSongSelected(ArrayList<Song> songs, int position) {
        if(selectedAlbum!=null) {
            if (!mSeekbar.isEnabled()) {
                mSeekbar.setEnabled(true);
            }

            this.songs = new ArrayList<Song>(selectedAlbum.getSongs().values());
            mPlayerAdapter.setCurrentSong(this.songs.get(position), this.songs);
            if(songs.get(position).getThumbnail()!=null&& !TextUtils.isEmpty(songs.get(position).getThumbnail())){
                Picasso.with(MainActivity.this).load(songs.get(position).getThumbnail()).placeholder(R.drawable.music_background).into(background);
            }else {
                background.setImageDrawable(getResources().getDrawable(R.drawable.music_background));
            }
            String folder= Environment.getExternalStorageDirectory()+"/CarPlayer/Music";
            mPos = position;
            if(!Utils.fileDownloaded(songs.get(position).getTitle()+".mp3",folder)){
                mPlayerAdapter.initMediaPlayer(true);
            }else {
                mPlayerAdapter.initMediaPlayer(false);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child("music").child("english").removeEventListener(listener);
    }
    private void prepareListener(){
        listener= new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                getUpdates(dataSnapshot);
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateItem(dataSnapshot);
            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                deleteItem(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    private void initializeSeekBar() {
        mSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                        Log.d("seekbar","move");
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        if (fromUser) {
                            userSelectedPosition = progress;

                        }

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        if (mUserIsSeeking) {

                        }
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }
    private void deleteItem(DataSnapshot dataSnapshot) {
        Album album= dataSnapshot.getValue(Album.class);
        for( int i=0;i<albums.size();i++){
            if(albums.get(i).getTitle().equals(album.getTitle())){
                albums.remove(i);
                mAdapter= new MediaRequestGridAdapter(MainActivity.this,albums,2,mRecylcerView);
                mRecylcerView.setAdapter(mAdapter);
            }

        }
    }

    private void getUpdates(DataSnapshot dataSnapshot) {
        Log.d("album",dataSnapshot.getKey());
        Album album= dataSnapshot.getValue(Album.class);
        albums.add(album);
        Collections.sort(albums, new Comparator<Album>() {
            @Override
            public int compare(Album lhs,Album rhs) {
                if (lhs.timestamp > rhs.timestamp)
                    return -1;
                else if(lhs.timestamp == rhs.timestamp)
                    return 0;
                else
                    return 1;
            }
        });

        Log.d("album",String.valueOf(album.getSongs().size()));
        mAdapter= new MediaRequestGridAdapter(MainActivity.this,albums,2,mRecylcerView);
        albumClicklistener=new MediaRequestGridAdapter.OnAlbumClick() {
            @Override
            public void onAlbumClick(Album album, int position) {
                selectedAlbum=album;
                selectedPos=position;
                 fragment = SongsFragment.newInstance(album);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragment.setSelectionListener(MainActivity.this);
                fragmentManager.beginTransaction().replace(R.id.recycler_wrapper, fragment).addToBackStack("songs").commit();

            }
        };

        mAdapter.setAlbumClickListener(albumClicklistener);
        mRecylcerView.setAdapter(mAdapter);
        if(album.getTitle().equals("Top charts")&&startupLaunch){
            selectedAlbum=album;
            startupLaunch=false;
            Bundle b=new Bundle();

            b.putString("init","t");
            fragment = SongsFragment.newInstance(album);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment.setSelectionListener(MainActivity.this);
            fragment.setArguments(b);
            fragmentManager.beginTransaction().replace(R.id.recycler_wrapper, fragment).addToBackStack("songs").commit();
        }
    }

    private void setViews() {
        playPause = findViewById(R.id.play_button);
        next = findViewById(R.id.forward_button);
        previous = findViewById(R.id.rewind_button);
        mSeekbar =(SeekBar) findViewById(R.id.seekBar);
       // progressBar=v.findViewById(R.id.progressBar6);
         songTitle = findViewById(R.id.songTitle);
         menuWrapper=findViewById(R.id.ee);
         recylerWrapper=findViewById(R.id.recycler_wrapper);
         root=findViewById(R.id.rootLayout);
         shuffleBtn=findViewById(R.id.shuffle);
         background=findViewById(R.id.imageButton4);
         if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("shuffle", false)){
             int color = Color.parseColor("#3a7ce8"); //The color u want
             isShuffling=true;
             shuffleBtn.setColorFilter(color);
         }else {
             int color = Color.parseColor("#ffffff"); //The color u want
             shuffleBtn.setColorFilter(color);
             isShuffling=false;
         }
         shuffleBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(!isShuffling) {
                     isShuffling=true;
                     PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("shuffle", true).apply();
                     Toast.makeText(MainActivity.this,"Shuffle Mode Is On",Toast.LENGTH_SHORT).show();
                     int color = Color.parseColor("#3a7ce8"); //The color u want
                     shuffleBtn.setColorFilter(color);
                 }else {
                     isShuffling=false;
                     PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("shuffle",false).apply();
                     Toast.makeText(MainActivity.this,"Shuffle Mode Is off",Toast.LENGTH_SHORT).show();
                     int color = Color.parseColor("#ffffff"); //The color u want
                     shuffleBtn.setColorFilter(color);

                 }

             }
         });
        constraintLayout2=findViewById(R.id.rlpp);
       // To listen to clicks
        stretch=findViewById(R.id.scale_button);
        stretch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFullScreen){
                    isFullScreen=false;
                    //exit full screen
                    recylerWrapper.setVisibility(View.VISIBLE);
                    menuWrapper.setVisibility(View.VISIBLE);

                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, 0);
                    constraintLayout.setLayoutParams(params);
                    ConstraintSet set = new ConstraintSet();
                    set.clone(MainActivity.this,R.layout.activity_main);


                    AutoTransition transition = new AutoTransition();
                    transition.setDuration(1000);

                    TransitionManager.beginDelayedTransition(root, transition);
                    set.applyTo(root);



                }else {
                    isFullScreen=true;
                    recylerWrapper.setVisibility(View.INVISIBLE);
                    menuWrapper.setVisibility(View.INVISIBLE);
                    //go to full screen
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, 0);
                    constraintLayout.setLayoutParams(params);
                    ConstraintSet set = new ConstraintSet();
                    set.clone(root);

                    set.connect(constraintLayout.getId(), ConstraintSet.BOTTOM, root.getId(), ConstraintSet.BOTTOM);
                    set.connect(constraintLayout.getId(), ConstraintSet.TOP, root.getId(), ConstraintSet.TOP);
                    set.connect(constraintLayout.getId(), ConstraintSet.LEFT, root.getId(), ConstraintSet.LEFT);
                    set.connect(constraintLayout.getId(), ConstraintSet.RIGHT, root.getId(), ConstraintSet.RIGHT);
                    AutoTransition transition = new AutoTransition();
                    transition.setDuration(1000);

                    TransitionManager.beginDelayedTransition(root, transition);
                    set.applyTo(root);
                }
            }
        });
        constraintLayout=findViewById(R.id.player);
         playPause.setOnClickListener(MainActivity.this);
         next.setOnClickListener(MainActivity.this);
         previous.setOnClickListener(MainActivity.this);
         progressBar=findViewById(R.id.progressBar2);
         timePass=findViewById(R.id.text_time_pass);
         timeTotal=findViewById(R.id.text_time_total);

       // set adapter


    }
    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void updateItem(DataSnapshot dataSnapshot) {
        try {
            Album album=dataSnapshot.getValue(Album.class);
            for (int i = 0; i < albums.size(); i++) {
                if (albums.get(i).getTitle().equals(album.getTitle())) {
                    albums.set(i, dataSnapshot.getValue(Album.class));
                    mAdapter = new MediaRequestGridAdapter(MainActivity.this, albums, 2,mRecylcerView);
                   mRecylcerView.setAdapter(mAdapter);
                }

            }
        }catch (DatabaseException e){
            try {
                Album album=new Album();
                ObjectMapper om=new ObjectMapper();
                album = om.convertValue(dataSnapshot.getValue(), Album.class);
                for (int i = 0; i < albums.size(); i++) {
                    if (albums.get(i).getTitle().equals(album.getTitle())) {
                        albums.set(i, dataSnapshot.getValue(Album.class));
                        mAdapter = new MediaRequestGridAdapter(MainActivity.this, albums, 2,mRecylcerView);
                       mRecylcerView.setAdapter(mAdapter);
                    }

                }
            }catch (Exception e2){
                e2.printStackTrace();
            }

        }

    }
    private void setState(State state) {
        if (mState != state) {
            mState = state;
            //updateUI();
        }
    }
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = mLayoutManager.getChildCount();
            mTotalItemCount = mLayoutManager.getItemCount() - (mAdapter.isLoading() ? 1 : 0);
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (mState == State.LOADING_PAGE) {
                if (mTotalItemCount > mPreviousTotal) {
                    mPreviousTotal = mTotalItemCount;
                    mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
                    setState(State.LOADED);
                }
            }

            if (!mEndOfListReached && mState != State.SEARCHING && mState != State.LOADING_PAGE && mState != State.LOADING && (mTotalItemCount - visibleItemCount) <= (firstVisibleItem +
                    mLoadingTreshold)) {

                //mFilters.setPage(mPage);
                //providerManager.getCurrentMediaProvider().getList(mItems, new MediaProvider.Filters(mFilters), mCallback);

                mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
                // setState(MediaListFragment.State.LOADING_PAGE);
            }
        }
    };
    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(MainActivity.this,
                MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

        final Intent startNotStickyIntent = new Intent(MainActivity.this, MusicService.class);
        if(Build.VERSION.SDK_INT>=26){
            startForegroundService(startNotStickyIntent);
        }else {
            startService(startNotStickyIntent);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        doUnbindService();
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.onPauseActivity();
        }

        mAdapter.clearItems();
    }
    @Override
    public void onResume() {
        super.onResume();
        doBindService();

        if (mPlayerAdapter != null && mPlayerAdapter.isPlaying()) {
            prepareListener();

            restorePlayerStatus();
        }
    }

    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ?
                R.drawable.ic_av_pause : R.drawable.ic_av_play;
        playPause.post(new Runnable() {
            @Override
            public void run() {
                playPause.setImageResource(drawable);
                if(mPlayerAdapter.getState()!= PlaybackInfoListener.State.PLAYING){
                    mStopHandler=true;
                }else {
                    if(mPlayerAdapter.getState()==PlaybackInfoListener.State.PLAYING){
                        mStopHandler=false;
                    }
                }
            }
        });
    }



    private void restorePlayerStatus() {
        mSeekbar.setEnabled(mPlayerAdapter.isMediaPlayer());

        mSeekbar.setMax(mPlayerAdapter.getMediaPlayer().getDuration());
        songTitle.setText(mPlayerAdapter.getCurrentSong().getTitle());
        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {

            mPlayerAdapter.onResumeActivity();
            updatePlayingInfo(true, false);
        }
    }
    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();


        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf

                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            progressUpdate=new MediaPlayerHolder.progressUpdate() {
                @Override
                public void showProgressBar() {
                    // mProgressDialog.show();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void hideProgressBar() {
//                    if(mProgressDialog.isShowing()){
//                        mProgressDialog.dismiss();
                    progressBar.setVisibility(View.INVISIBLE);
//                    }
                }

                @Override
                public void OnPlayerReady(Song song) {
                    mSeekbar.setMax(mPlayerAdapter.getMediaPlayer().getDuration());
                    timeTotal.setText(getTimeString(mPlayerAdapter.getMediaPlayer().getDuration()));

                    songTitle.setText(song.getTitle());
                    Log.d("song",song.getTitle());

//                    runnable=new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                String duration=  getTimeString( mPlayerAdapter.getMediaPlayer().getCurrentPosition());
//                                Log.d("duration",duration);
//                                timePass.setText(duration);
//                                handler.postDelayed(runnable,1000);
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//
//                        }
//                    };

                //   handler.postDelayed(runnable,1000);
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            // do your stuff - don't create a new runnable here!
//                            seekBar.setProgress(mPlayerAdapter.getMediaPlayer().getCurrentPosition()/1000);
//                            if (!mStopHandler) {
//                                mHandler.postDelayed(this, 1000);
//                            }
//                        }
//                    };mHandler.post(runnable);

// start it with:

                }



            };

            mMusicService = ((MusicService.LocalBinder) iBinder).getInstance();
            mMusicService.setProgressUpdate(progressUpdate);
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();

            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }
            if (mPlayerAdapter != null && mPlayerAdapter.isPlaying()) {

                restorePlayerStatus();
            }
            checkReadStoragePermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMusicService = null;
        }
    };

    private void checkReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
    private void updatePlayingInfo(boolean restore, boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMusicService.startForeground(MusicNotificationManager.NOTIFICATION_ID,
                            mMusicNotificationManager.createNotification());
                }
            }, 250);
        }

        final Song selectedSong = mPlayerAdapter.getCurrentSong();

        songTitle.setText(selectedSong.getTitle());
        //final int duration = selectedSong.duration;
        //seekBar.setMax(duration);

        if (restore) {
            mSeekbar.setProgress(mPlayerAdapter.getPlayerPosition());
            updatePlayingStatus();


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stop foreground if coming from pause state
                    if (mMusicService.isRestoredFromPause()) {
                        mMusicService.stopForeground(false);
                        mMusicService.getMusicNotificationManager().getNotificationManager()
                                .notify(MusicNotificationManager.NOTIFICATION_ID,
                                        mMusicService.getMusicNotificationManager().getNotificationBuilder().build());
                        mMusicService.setRestoredFromPause(false);
                    }
                }
            }, 250);
        }
    }
    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(MainActivity.this);
        }
        return isPlayer;
    }
    public void skipPrev() {
//        if (checkIsPlayer()) {
//            mPlayerAdapter.instantReset();
//        }
        if (checkIsPlayer()) {
            // mPlayerAdapter.skip(true);
            if(mPos<0)
                mPos=songs.size()-1;
            if (!mSeekbar.isEnabled()) {
                mSeekbar.setEnabled(true);
            }
            mPlayerAdapter.setCurrentSong(songs.get(mPos), songs);
            if(!fileDownloaded(songs.get(mPos).getTitle()+".mp3",Environment.getExternalStorageDirectory()+"/CarPlayer/Music"))
                mPlayerAdapter.initMediaPlayer(true);
            else mPlayerAdapter.initMediaPlayer(false);
        }
    }
    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    public void resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter.resumeOrPause();
        }
    }

    public void skipNext() {
        if (checkIsPlayer()) {
            // mPlayerAdapter.skip(true);
            if(mPos>=songs.size())
                mPos=0;
            if (!mSeekbar.isEnabled()) {
                mSeekbar.setEnabled(true);
            }
            mPlayerAdapter.setCurrentSong(songs.get(mPos), songs);
            if(!fileDownloaded(songs.get(mPos).getTitle()+".mp3",Environment.getExternalStorageDirectory()+"/CarPlayer/Music"))
            mPlayerAdapter.initMediaPlayer(true);
            else mPlayerAdapter.initMediaPlayer(false);

        }
    }
    class PlaybackListener extends PlaybackInfoListener

    {

        @Override
        public void onPositionChanged(final int position) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mUserIsSeeking) {
                    mSeekbar.setProgress(position);
                }
                String duration=  getTimeString( mPlayerAdapter.getMediaPlayer().getCurrentPosition());
                timePass.setText(duration);
            }
        });


    }

        @Override
        public void onStateChanged(@PlaybackInfoListener.State int state) {

        updatePlayingStatus();
        if (mPlayerAdapter.getState() != PlaybackInfoListener.State.RESUMED && mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED) {
            updatePlayingInfo(false, true);
        }
    }

        @Override
        public void onPlaybackCompleted() {

        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("shuffle", false)){
            Random rand = new Random(System.currentTimeMillis());
           mPos= rand.nextInt((songs.size()));
          //  mPos = ThreadLocalRandom.current().nextInt(0, albums.get(selectedPos).getSongs().size() );

        }else
            mPos++;

            skipNext();
    }
        private String getTimeString(long millis) {
            StringBuffer buf = new StringBuffer();


            int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
            int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

            buf

                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));

            return buf.toString();
        }
    }

}
