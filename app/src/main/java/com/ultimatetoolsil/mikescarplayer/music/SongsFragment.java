package com.ultimatetoolsil.mikescarplayer.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.ultimatetoolsil.mikescarplayer.R;
import com.ultimatetoolsil.mikescarplayer.music.playback.MediaPlayerHolder;
import com.ultimatetoolsil.mikescarplayer.music.playback.MusicNotificationManager;
import com.ultimatetoolsil.mikescarplayer.music.playback.MusicService;
import com.ultimatetoolsil.mikescarplayer.music.playback.PlaybackInfoListener;
import com.ultimatetoolsil.mikescarplayer.music.playback.PlayerAdapter;
import com.ultimatetoolsil.mikescarplayer.music.utils.EqualizerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.ultimatetoolsil.mikescarplayer.music.utils.Utils.fileDownloaded;


/**
 * Created by Mike on 03/03/2019.
 */

public class SongsFragment extends Fragment implements View.OnClickListener {
private static Album mAlbum;

private RecyclerView songsList;
private SongsAdapter adapter;
private SongsAdapter.SongItemClickListener clickListener;
private SeekBar seekBar;
private ProgressBar progressBar;
private ImageButton playPause, next, previous;
private TextView songTitle;
private MusicService mMusicService;
private Boolean mIsBound;
public PlayerAdapter mPlayerAdapter;
private boolean mUserIsSeeking = false;

private MediaPlayerHolder.progressUpdate progressUpdate;
private MusicNotificationManager mMusicNotificationManager;
//private ProgressDialog mProgressDialog;
private boolean prepared=false;
private boolean mStopHandler=false;
private int mPos=0;
private SlidingUpPanelLayout slider;
private SongsAdapter.SongMenuClickListener menuClickListener;
private ArrayList<Song> songs;
private SelectionListener selectionListener;
private TextView menuTitle,download_t,favor_t;
private TextView menuAlbum;
private ImageView menuIcon,heart,download_i;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.songs_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songsList=view.findViewById(R.id.recyclerView);
        menuTitle=view.findViewById(R.id.menu_title);
        menuAlbum=view.findViewById(R.id.menu_album);
        menuIcon=view.findViewById(R.id.menu_icon);
        heart=view.findViewById(R.id.menu_favour_i);
        download_i=view.findViewById(R.id.menu_download_i);
        download_t=view.findViewById(R.id.menu_download_t);
        favor_t=view.findViewById(R.id.menu_favour_t);
        slider=view.findViewById(R.id.sliding_layout);
        slider.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        songsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        songsList.setHasFixedSize(true);
        songs=new ArrayList<>(mAlbum.getSongs().values());
        adapter=new SongsAdapter(getActivity(),songs);
//        mProgressDialog=new ProgressDialog(getActivity());
//        mProgressDialog.setMessage("Buffering...");
//        mProgressDialog.setCancelable(false);

        setViews(view);
       // initializeSeekBar();
        menuClickListener=new SongsAdapter.SongMenuClickListener() {
            @Override
            public void OnMenuClick(View view, final int position) {
                menuAlbum.setText(songs.get(position).getArtist());
                Picasso.with(getContext()).load(songs.get(position).getThumbnail()).placeholder(R.drawable.track_image).into(menuIcon);
                menuTitle.setText(songs.get(position).getTitle());
                slider.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                View.OnClickListener add=new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                };
                favor_t.setOnClickListener(add);
                heart.setOnClickListener(add);
                View.OnClickListener download=new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SongsAdapter.ViewHolder holder =(SongsAdapter.ViewHolder) songsList.findViewHolderForAdapterPosition(position);
                        adapter.startDownload(position,holder);
                    }
                };
                download_i.setOnClickListener(download);
                download_t.setOnClickListener(download);
            }
        };
        clickListener= new SongsAdapter.SongItemClickListener() {
            @Override
            public void onItemClick(View view, int position,boolean isStream) {
              //play the song

                if(view.getId()!=R.id.imageButton&&view.getId()!=R.id.imageButton2&&view.getId()!=R.id.progress) {
                    //Utilities.serializeSong(".bin",songs.get(position),System.currentTimeMillis(),getContext());
                    selectionListener.onSongSelected(songs,position);


                }
            }
        };

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(getResources().getDrawable( R.drawable.rec_divider));
        songsList.addItemDecoration(itemDecorator);

        songsList.setAdapter(adapter);
        adapter.setClickListener(clickListener);
        adapter.setMenuClickListener(menuClickListener);
        if(getArguments()!=null&&getArguments().getString("init").equals("t")){
            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("shuffle", false)){
                Random rand = new Random(System.currentTimeMillis());
                mPos= rand.nextInt((songs.size()));

                songsList.getLayoutManager().scrollToPosition(mPos);
                selectionListener.onSongSelected(songs,mPos);
            }else {
                selectionListener.onSongSelected(songs,0);
            }
        }

    }
    public void setSelectionListener(SelectionListener selectionListener){
       this. selectionListener=selectionListener;
    }






    public static SongsFragment newInstance(Album album) {
        SongsFragment songsFragment= new SongsFragment();
        mAlbum=album;

        return songsFragment;
    }





    public void skipNext() {
        if (checkIsPlayer()) {
           // mPlayerAdapter.skip(true);
           if(mPos>=songs.size())
               mPos=0;
            if (!seekBar.isEnabled()) {
                seekBar.setEnabled(true);
            }
            mPlayerAdapter.setCurrentSong(songs.get(mPos), songs);
            if(!fileDownloaded(songs.get(mPos)+".mp3", Environment.getExternalStorageDirectory()+"/CarPlayer/Music"))
                mPlayerAdapter.initMediaPlayer(true);
            else mPlayerAdapter.initMediaPlayer(false);

        }
    }

    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(getActivity());
        }
        return isPlayer;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case (R.id.buttonPlayPause): {
//               Log.d("player","playpause");
//                resumeOrPause();
//               //mPlayerAdapter.getMediaPlayer().pause();
//               break;
//            }
//            case (R.id.buttonNext): {
//                mPos++;
//                skipNext();
//                break;
//            }
//            case (R.id.buttonPrevious): {
//                mPos--;
//                skipPrev();
//                break;
//            }
        }
    }

    private void initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
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
    private void setViews(View v) {
   //     playPause = v.findViewById(R.id.buttonPlayPause);
//        next = v.findViewById(R.id.buttonNext);
//        previous = v.findViewById(R.id.buttonPrevious);
//        seekBar = v.findViewById(R.id.seekBar);
//        progressBar=v.findViewById(R.id.progressBar6);
       // songTitle = v.findViewById(R.id.songTitle);
        //To listen to clicks
       // playPause.setOnClickListener(SongsFragment.this);
       // next.setOnClickListener(SongsFragment.this);
       // previous.setOnClickListener(SongsFragment.this);
        //set adapter

//        //get songs
//        mSelectedArtistSongs = SongProvider.getAllDeviceSongs(this);
//        Log.w("SOngs", String.valueOf(mSelectedArtistSongs.size()));
//        simpleAdapter.addManyItems(((List) mSelectedArtistSongs));
    }
//    @Override
//    public void bindView(@NotNull View view, @NotNull Object o, int i) {
//        TextView title = view.findViewById(R.id.textViewSongTitle);
//        TextView artist = view.findViewById(R.id.textViewArtistName);
//        Song song = ((Song) o);
//        title.setText(song.title);
//        artist.setText(song.artistName);
//    }


   public interface SelectionListener{
        void onSongSelected(ArrayList<Song> songs,int position);
   }

}
