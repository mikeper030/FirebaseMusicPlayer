package com.ultimatetoolsil.mikescarplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Px;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.ultimatetoolsil.mikescarplayer.music.Album;
import com.ultimatetoolsil.mikescarplayer.music.Song;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mike Peretz on 16/12/2018.
 */

public class MediaRequestGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int NORMAL = 0, LOADING = 1;
    private int itemWidth, itemHeight, margin, columns;
    private ArrayList<?> mItems = new ArrayList<>();
    private Context mContext;
    //	private ArrayList<Media> mData = new ArrayList<>();

    private OnAlbumClick click;

    public MediaRequestGridAdapter(Context context, ArrayList<?> items, Integer columns,RecyclerView recyclerView) {
        this.columns = columns;

        int screenWidth =recyclerView.getWidth();
        itemWidth = (screenWidth / columns);
        itemHeight = (int) ((double) itemWidth / 0.677);
        margin = PixelUtils.getPixelsFromDp(context, 2);
        this.mContext=context;
        setItems(items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem_loading, parent, false);
                return new MediaRequestGridAdapter.LoadingHolder(v);
            case NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem, parent, false);
                return new MediaRequestGridAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        @Px int doubleMargin = margin * 2;
        @Px int topMargin = (position < columns) ? margin * 2 : margin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.height = itemHeight;
        layoutParams.width = itemWidth;

         if (position % columns == columns - 1) {
            viewHolder.itemView.setPadding(margin, topMargin, doubleMargin, margin);
        } else {
            viewHolder.itemView.setPadding(margin, topMargin, margin, margin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == NORMAL) {

            final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            final Object overviewItem = getItem(position);
            videoViewHolder.placehodler.setVisibility(View.VISIBLE);
           // Media item = overviewItem.media;

                if(overviewItem instanceof Song) {
                    Song song = (Song) getItem(position);
                    videoViewHolder.title.setText(song.getTitle() != null ? song.getTitle() : "");
                    //videoViewHolder.year.setText(song.getYear() != null ? song.getYear() : "");
                    if (song.getThumbnail() != null && !song.getThumbnail().equals("")) {
                        Picasso.with(videoViewHolder.coverImage.getContext()).cancelRequest(videoViewHolder.coverImage);
                        Picasso.with(videoViewHolder.coverImage.getContext())
                                .load(song.getThumbnail())
                                //.resize(viewHolder.itemView.getWidth(), viewHolder.itemView.getHeight())
                                .transform(DrawGradient.getInstance())
                                .into(videoViewHolder.coverImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        videoViewHolder.placehodler.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }

                    }if(overviewItem instanceof Album){
                        Album album = (Album) getItem(position);
                        videoViewHolder.title.setText(album.getTitle() != null ? album.getTitle() : "");
                        videoViewHolder.year.setText(album.getArtist() != null ? album.getArtist() : "");
                        if (album.getCover() != null && !album.getCover().equals("")) {
                            Picasso.with(videoViewHolder.coverImage.getContext()).cancelRequest(videoViewHolder.coverImage);
                            Picasso.with(videoViewHolder.coverImage.getContext())
                                    .load(album.getCover())
                                    //.resize(viewHolder.itemView.getWidth(), viewHolder.itemView.getHeight())
                                    .transform(DrawGradient.getInstance())
                                    .into(videoViewHolder.coverImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            videoViewHolder.placehodler.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onError() {

                                        }
                                    });

                        }


            }



        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        //if (getItem(position).isLoadingItem) {
         //   return LOADING;
       // }
        return NORMAL;
    }

    public Object getItem(int position) {
        if (position < 0 || mItems.size() <= position) return null;
        return mItems.get(position);
    }
    public void setAlbumClickListener(OnAlbumClick clickListener){
        click=clickListener;
    }



    public void removeLoading() {
        if (getItemCount() <= 0) return;
        Object item = mItems.get(getItemCount() - 1);
      //  if (item.isLoadingItem) {
        //    mItems.remove(getItemCount() - 1);
            notifyDataSetChanged();
       // }
    }


    public void addLoading() {
        OverviewItem item = null;
        if (getItemCount() != 0) {
         //   item = mItems.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
          //  mItems.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }

    public boolean isLoading() {
        return getItemCount() > 0 && getItemViewType(getItemCount() - 1) == LOADING;
    }

    public void setItems(ArrayList<?> items) {
        // Clear items
        mItems.clear();
        // Add new items, if available
        if (null != items) {
          mItems=(ArrayList<?>) items.clone();
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }
    public interface OnAlbumClick{
        void onAlbumClick(Album album, int position);
    }


    private static class OverviewItem {
        Album media;
        boolean isLoadingItem = false;

        OverviewItem(Album media) {
            this.media = media;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    private static class DrawGradient implements Transformation {
        private static Transformation instance;

        public static Transformation getInstance() {
            if (instance == null) {
                instance = new DrawGradient();
            }
            return instance;
        }

        @Override
        public Bitmap transform(Bitmap src) {
            // Code borrowed from https://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);

            canvas.drawBitmap(src, 0, 0, null);
            src.recycle();

            Paint paint = new Paint();
            float gradientHeight = h / 2f;
            LinearGradient shader = new LinearGradient(0, h - gradientHeight, 0, h, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, h - gradientHeight, w, h, paint);
            return overlay;
        }

        @Override
        public String key() {
            return "gradient()";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @BindView(R.id.focus_overlay)
        View focusOverlay;
        @BindView(R.id.cover_image)
        ImageView coverImage;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.year)
        TextView year;
        @BindView(R.id.placeholder_image)
        ImageView placehodler;

        private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(itemHeight);
            Glide.with(mContext).asGif().load(R.drawable.icon_load).into(placehodler);
            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

        @Override
        public void onClick(View view) {
            Log.d("position","click");
            int position = getLayoutPosition();

               // Toast.makeText(mContext, "This feature is under development and will be available soon", Toast.LENGTH_SHORT).show();
                if(click!=null){
                    Album album = (Album) getItem(position);
                    click.onAlbumClick(album,position);
                }


        }

    }

    private class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(itemHeight);
        }

    }
}