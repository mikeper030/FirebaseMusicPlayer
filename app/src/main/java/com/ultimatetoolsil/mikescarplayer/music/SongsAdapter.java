package com.ultimatetoolsil.mikescarplayer.music;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.squareup.picasso.Picasso;
import com.ultimatetoolsil.mikescarplayer.R;

import java.io.File;
import java.util.List;

import static com.ultimatetoolsil.mikescarplayer.music.utils.Utils.fileDownloaded;

/**
 * Created by Mike on 03/03/2019.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private List<Song> mData;
    private LayoutInflater mInflater;
    private SongItemClickListener mClickListener;
    private Context mContext;
    private String folder;
    private SongMenuClickListener menuClick;
    int selectedPosition=-1;

    // data is passed into the constructor
    SongsAdapter(Context context, List<Song> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext=context;
        this.folder= Environment.getExternalStorageDirectory()+"/CarPlayer/Music";
        if(PreferenceManager.getDefaultSharedPreferences(mContext).getString("downloads",null)!=null&&!PreferenceManager.getDefaultSharedPreferences(mContext).getString("downloads","").isEmpty())
            folder= PreferenceManager.getDefaultSharedPreferences(mContext).getString("downloads", Environment.getExternalStorageDirectory()+"/CarPlayer/Music");
        //Create androiddeft folder if it does not exist
        File directory = new File(folder);

        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.track_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.selectable_pressed));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        final Song song = mData.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();
                if(!fileDownloaded(song.getTitle()+".mp3",folder))
                mClickListener.onItemClick(v,position,true);
                else {
                    mClickListener.onItemClick(v,position,false);
                }
            }
        });

      if(song.getThumbnail()!=null&&!TextUtils.isEmpty(song.getThumbnail()))
           Picasso.with(mContext).load(song.getThumbnail()).placeholder(R.drawable.track_image).into(holder.cover);
//       else if(mData.get(position-1).getThumbnail()!=null&& TextUtils.isEmpty(mData.get(position-1).getThumbnail()))
//           try {
//              // Picasso.with(mContext).load(mData.get(position-1).getThumbnail()).placeholder(R.drawable.track_image).into(holder.cover);
//
//           }catch (Exception e){
//               e.printStackTrace();
//           }

        holder.songTitle.setText(song.getTitle());
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fileDownloaded(song.getTitle()+".mp3",folder))
                menuClick.OnMenuClick(view,position);

            }
        });
        holder.artistName.setText(song.getArtist());
        holder.progressBar.setProgressColor(mContext.getResources().getColor(R.color.main_color));
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFolder();
            }
        });
        if(!fileDownloaded(song.getTitle()+".mp3",folder)) {
                holder.download.setVisibility(View.VISIBLE);
            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                  startDownload(position,holder);
                }
            });
        }else {
            holder.open.setVisibility(View.VISIBLE);
            holder.download.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    private void openFolder()
    {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(folder); //  directory path
            intent.setDataAndType(uri, "*/*");
            mContext.startActivity(Intent.createChooser(intent, "Open folder"));
        }catch (Exception e){
            Toast.makeText(mContext,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void startDownload(int position, final ViewHolder holder) {

        Song song=getItem(position);
        int downloadId = PRDownloader.download(song.getLink(), folder, song.getTitle() + ".mp3")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        holder.download.setVisibility(View.INVISIBLE);
                        holder.progressBar.setVisibility(View.VISIBLE);

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                        holder.progressBar.setProgress((int) progressPercent);
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        holder.open.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Error error) {

                    }




                });
    }
    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView artistName,songTitle;
        ImageView cover,download,menu,open;
        CircularProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            download=itemView.findViewById(R.id.imageButton);
            menu=itemView.findViewById(R.id.imageButton2);
            open=itemView.findViewById(R.id.imageButton3);
            progressBar=itemView.findViewById(R.id.progress);
            artistName = itemView.findViewById(R.id.textViewArtistName);
            songTitle=itemView.findViewById(R.id.textViewSongTitle);
            cover=itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

           // itemView.setBackgroundColor(mContext.getResources().getColor(R.color.selectable_pressed));
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());


        }
    }

    // convenience method for getting data at click position
    Song getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setMenuClickListener(SongMenuClickListener listener){
        this.menuClick=listener;
    }
    void setClickListener(SongItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface SongItemClickListener {
        void onItemClick(View view, int position,boolean is_stream);
    }
    public interface SongMenuClickListener{
        void OnMenuClick(View view, int position);
    }

}