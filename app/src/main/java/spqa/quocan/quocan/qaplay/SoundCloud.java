package spqa.quocan.quocan.qaplay;

import retrofit.RestAdapter;

/**
 * Created by MyPC on 01/07/2016.
 */
public class SoundCloud {
    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder().setEndpoint(Config.API_URL).build();
    private static final QAService SERVICE = REST_ADAPTER.create(QAService.class);

    public static QAService getService() {
        return SERVICE;
    }
}
