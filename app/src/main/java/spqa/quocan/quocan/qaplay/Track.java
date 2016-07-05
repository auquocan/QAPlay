package spqa.quocan.quocan.qaplay;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MyPC on 01/07/2016.
 */
public class Track {
    @SerializedName("title")
    private String mTitle;

    @SerializedName("id")
    private int mID;

    @SerializedName("stream_url")
    private String mStreamURL;

    @SerializedName("artwork_url")
    private String mArtworkURL;

    public String getTitle() {
        return mTitle;
    }

    public int getID() {
        return mID;
    }

    public String getStreamURL() {
        return mStreamURL;
    }

    public String getArtworkURL() {
        if (mArtworkURL == null)
            return "empty";
        else
            return mArtworkURL;

    }
}
