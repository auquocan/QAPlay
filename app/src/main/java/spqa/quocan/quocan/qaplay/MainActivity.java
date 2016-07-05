package spqa.quocan.quocan.qaplay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spqa.quocan.quocan.qaplay.listen.OnLoadMoreListener;
import spqa.quocan.quocan.qaplay.listen.RecyclerItemClickListener;
import spqa.quocan.quocan.qaplay.notification.Constants;
import spqa.quocan.quocan.qaplay.notification.NotificationService;


public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {
    public static List<Track> mListItems = new ArrayList<Track>();
    private List<Track> mList = new ArrayList<Track>();
    private TextView mSelectedTrackTitle;
    private ImageView mSelectedTrackImage;
    public static MediaPlayer mMediaPlayer;
    private static ImageView mPlayerControl;
    private ImageView mPlayerNext;
    private ImageView mPlayerPrev;
    public static int flag_play_or_pause;
    private RecyclerView mRecyclerView;
    private UserAdapter mUserAdapter;
    private int INDEX = 0;
    private int MONTH = 1;
    public static String trackName;
    public static Bitmap trackImage;
    public static int curPosition = 0;
    Animation animFadein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //todo: get track sound cloud and set cycleview
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getTrackFromSoundCloud(false);


        //todo: Tool Bar Play
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause(0);
            }
        });
        //todo:  end of a song then play next song
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mListItems.size() - 1 == curPosition)
                    Toast.makeText(MainActivity.this, "End Of List", Toast.LENGTH_SHORT).show();
                else
                    PlayTrack(curPosition, 1);
            }
        });

        //todo: Tool bar control the music playlist
        mSelectedTrackTitle = (TextView) findViewById(R.id.selected_track_title);
        mSelectedTrackImage = (ImageView) findViewById(R.id.selected_track_image);
        mPlayerControl = (ImageView) findViewById(R.id.player_control);
        mPlayerNext = (ImageView) findViewById(R.id.player_next);
        mPlayerPrev = (ImageView) findViewById(R.id.player_prev);


        //todo: click pause or play
        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause(0);
                //todo:  start notification
                Intent serviceIntent = new Intent(MainActivity.this, NotificationService.class);
                serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startService(serviceIntent);

                flag_play_or_pause = 0;
            }
        });
        //todo: click next
        mPlayerNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListItems.size() - 1 == curPosition)
                    Toast.makeText(MainActivity.this, "End Of List", Toast.LENGTH_SHORT).show();
                else
                    PlayTrack(curPosition, 1);
            }
        });
        //todo: click previous
        mPlayerPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curPosition == 0)
                    Toast.makeText(MainActivity.this, "Top Of List", Toast.LENGTH_SHORT).show();
                else
                    PlayTrack(curPosition, -1);
            }
        });
        //todo: click on item list to change track
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        curPosition = position;
                        PlayTrack(position, 0);
                    }
                })
        );
    }


    //<!--- OUT OF OnCreate ----!>//
    public void PlayTrack(int position, int flag) {
        if (flag == -1) {
            position--;
            curPosition--;
        }
        if (flag == 1) {
            position++;
            curPosition++;
        }
        // get this to use for Next or Prev Track
        //todo: set info to toolbar
        Track track = mListItems.get(position);
        mSelectedTrackTitle.setText(track.getTitle());
        if (track.getArtworkURL().equals("empty"))
            mSelectedTrackImage.setImageResource(R.drawable.music);
        else {
            Picasso.with(this).load(track.getArtworkURL()).into(mSelectedTrackImage);
        }

        // todo : load the animation
        animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.rotate);
        animFadein.setAnimationListener(MainActivity.this);
        mSelectedTrackImage.startAnimation(animFadein);

        //todo set public text, image
        trackName = track.getTitle();
        trackImage = ((BitmapDrawable) mSelectedTrackImage.getDrawable()).getBitmap();


        //todo: play track
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(track.getStreamURL() + "?client_id=" + Config.CLIENT_ID);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //todo:  start notification
        Intent serviceIntent = new Intent(MainActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        flag_play_or_pause = 1; //todo: fix play or pause change on notification
        startService(serviceIntent);

    }

    public static void togglePlayPause(int flag) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.apollo_holo_dark_play);
        } else {
            mMediaPlayer.start();
            mPlayerControl.setImageResource(R.drawable.apollo_holo_dark_pause);
        }

        //when call from close notiication button
        if(flag == 1)
        {
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.apollo_holo_dark_play);
        }
    }

    //todo: get track from soundcloud
    private int getTrackFromSoundCloud(final boolean flag) {
        QAService scService = SoundCloud.getService();
        scService.getRecentTracks(new SimpleDateFormat("2015-" + String.valueOf(MONTH)).format(new Date()), new Callback<List<Track>>() {
            @Override
            public void success(List<Track> tracks, Response response) {
                //  mList.clear();
                loadTracks(tracks);
                MONTH += 1;// load some other track
                if (flag == false) {
                    copyList();

                    mUserAdapter = new UserAdapter();
                    mRecyclerView.setAdapter(mUserAdapter);
                } else {
                    INDEX = -20;
                }

                mUserAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        Log.e("haint", "Load More");
                        mListItems.add(null);
                        mUserAdapter.notifyItemInserted(mListItems.size() - 1);

                        //Load more data for reyclerview
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //Remove loading item
                                mListItems.remove(mListItems.size() - 1);
                                mUserAdapter.notifyItemRemoved(mListItems.size());


                                INDEX += 20;
                                int end = INDEX + 20;
                                for (int i = INDEX; i < end; i++) {
                                    mListItems.add(mList.get(i));

                                }
                                mUserAdapter.setLoaded();
                                mUserAdapter.notifyDataSetChanged();
                                getTrackFromSoundCloud(true);
                            }
                        }, 3000);
                    }
                });
            }


            @Override
            public void failure(RetrofitError error) {
                Log.d("EEE", "Error: " + error);
            }
        });
        return 0;
    }

    //todo: set  first 20 track to recylelist.
    private void copyList() {
        //Todo: Load 20 first item
        for (int i = 0; i < 20; i++) {
            mListItems.add(mList.get(i));
        }
    }

    private void loadTracks(List<Track> tracks) {
        mList.clear();
        mList.addAll(tracks);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //todo: RecyclerView
    static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public ImageView imageTrack;
        //ImageTrack

        public UserViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            imageTrack = (ImageView) itemView.findViewById(R.id.imageViewTrack);

            // tvEmailId = (TextView) itemView.findViewById(R.id.tvEmailId);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private OnLoadMoreListener mOnLoadMoreListener;

        private boolean isLoading;
        private int visibleThreshold = 5;
        private int lastVisibleItem, totalItemCount;

        public UserAdapter() {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public int getItemViewType(int position) {
            return mListItems.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.track_list_row, parent, false);
                return new UserViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_loading_item, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof UserViewHolder) {
                Track iTrack = mListItems.get(position);

                UserViewHolder userViewHolder = (UserViewHolder) holder;
                userViewHolder.tvName.setText(iTrack.getTitle());
                if (iTrack.getArtworkURL().equals("empty"))
                    userViewHolder.imageTrack.setImageResource(R.drawable.music);
                else {
                    Picasso.with(MainActivity.this).load(iTrack.getArtworkURL()).into(userViewHolder.imageTrack);


                }

                //userViewHolder.tvEmailId.setText(iTrack.getTitle());
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return mListItems == null ? 0 : mListItems.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
