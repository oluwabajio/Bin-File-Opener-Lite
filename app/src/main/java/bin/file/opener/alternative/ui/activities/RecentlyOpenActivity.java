package bin.file.opener.alternative.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.startapp.sdk.adsbase.StartAppSDK;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.databinding.ActivityRecentlyOpenBinding;
import bin.file.opener.alternative.ui.adapters.RecentlyOpenRecyclerAdapter;
import bin.file.opener.R;
import bin.file.opener.alternative.models.FileData;


public class RecentlyOpenActivity extends AppCompatActivity implements RecentlyOpenRecyclerAdapter.OnEventListener {
    private MyApplication mApp = null;
    public static final String RESULT_START_OFFSET = "startOffset";
    public static final String RESULT_END_OFFSET = "endOffset";
    ActivityRecentlyOpenBinding binding;

    /**
     * Starts an activity.
     *
     * @param c                      Android context.
     * @param activityResultLauncher Activity Result Launcher.
     */
    public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent intent = new Intent(c, RecentlyOpenActivity.class);
        activityResultLauncher.launch(intent);
    }

    /**
     * Set the base context for this ContextWrapper.
     * All calls will then be delegated to the base context.
     * Throws IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(MyApplication.getInstance().onAttach(base));
    }

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecentlyOpenBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mApp = MyApplication.getInstance();

        initAds();
        initStartAppAds();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Lookup the recyclerview in activity layout
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<RecentlyOpenRecyclerAdapter.UriData> list = new ArrayList<>();
        final List<FileData> li = mApp.getRecentlyOpened().list();
        int index = 0;
        int max = li.size();
        int m = String.valueOf(max).length();
        for (int i = max - 1; i >= 0; i--) {
            list.add(new RecentlyOpenRecyclerAdapter.UriData(this, ++index, m, li.get(i)));
        }
        RecentlyOpenRecyclerAdapter adapter = new RecentlyOpenRecyclerAdapter(this, list, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.getSwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setTitle(getString(R.string.action_recently_open_title));
    }

    private void initStartAppAds() {

        //    StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
        StartAppSDK.init(this, BuildConfig.APPLICATION_ID, true);

    }

    private void initAds() {
        Appodeal.setTesting(BuildConfig.DEBUG);
        String APP_KEY = "0735ee422c20c34818eb8d9ef96eefe3e8028bd4334ece68";
        Appodeal.setBannerViewId(R.id.appodealBannerView);
        Appodeal.initialize(this, APP_KEY, Appodeal.BANNER, true);
        Appodeal.setBannerCallbacks(setBannerCallBacks());
        Appodeal.show(this, Appodeal.BANNER_VIEW);

//        MobileAds.initialize(this) {}
//        val mAdView: AdView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
    }

    private BannerCallbacks setBannerCallBacks() {
        return new BannerCallbacks() {
            @Override
            public void onBannerLoaded(int height, boolean isPrecache) {

            }

            @Override
            public void onBannerFailedToLoad() {

            }

            @Override
            public void onBannerShown() {

            }

            @Override
            public void onBannerShowFailed() {

            }

            @Override
            public void onBannerClicked() {

            }

            @Override
            public void onBannerExpired() {

            }
        };
    }


    /**
     * Called when the options item is clicked (home).
     *
     * @param item The selected menu.
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    @Override
    public void onClick(@NonNull RecentlyOpenRecyclerAdapter.UriData ud) {
        Intent i = new Intent();
        i.setData(ud.fd.getUri());
        i.putExtra(RESULT_START_OFFSET, ud.fd.getStartOffset());
        i.putExtra(RESULT_END_OFFSET, ud.fd.getEndOffset());
        setResult(RESULT_OK, i);
        finish();
    }

    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    @Override
    public void onDelete(@NonNull RecentlyOpenRecyclerAdapter.UriData ud) {
        mApp.getRecentlyOpened().remove(ud.fd);
        if (mApp.getRecentlyOpened().list().isEmpty()) {
            Intent i = new Intent();
            i.setData(null);
            setResult(RESULT_OK, i);
            finish();
        }
    }

}
