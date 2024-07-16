package bin.file.opener.alternative.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.multidex.BuildConfig;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.databinding.ActivityLineUpdateBinding;
import bin.file.opener.alternative.ui.adapters.HexTextArrayAdapter;
import bin.file.opener.alternative.ui.adapters.LineUpdateHexArrayAdapter;
import bin.file.opener.alternative.ui.utils.LineUpdateTextWatcher;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.models.LineEntry;
import bin.file.opener.alternative.utils.SysHelper;


public class LineUpdateActivity extends AppCompatActivity implements View.OnClickListener {
  private static final String ACTIVITY_EXTRA_TEXTS = "ACTIVITY_EXTRA_TEXTS";
  private static final String ACTIVITY_EXTRA_POSITION = "ACTIVITY_EXTRA_POSITION";
  private static final String ACTIVITY_EXTRA_NB_LINES = "ACTIVITY_EXTRA_NB_LINES";
  private static final String ACTIVITY_EXTRA_FILENAME = "ACTIVITY_EXTRA_FILENAME";
  private static final String ACTIVITY_EXTRA_CHANGE = "ACTIVITY_EXTRA_CHANGE";
  private static final String ACTIVITY_EXTRA_SEQUENTIAL = "ACTIVITY_EXTRA_SEQUENTIAL";
  public static final String RESULT_REFERENCE_STRING = "RESULT_REFERENCE_STRING";
  public static final String RESULT_NEW_STRING = "RESULT_NEW_STRING";
  public static final String RESULT_POSITION = "RESULT_POSITION";
  public static final String RESULT_NB_LINES = "RESULT_NB_LINES";

  private MyApplication mApp = null;
  private TextInputEditText mEtInputHex;
  private TextInputLayout mTilInputHex;
  private int mPosition = -1;
  private int mNbLines = 0;
  private int mRefLength = 0;
  private String mFile;
  private boolean mChange;
  private boolean mSequential;
  private String mHex;
  private ImageView mIvVisibilitySource;
  private ImageView mIvVisibilityResult;
  private LinearLayout mLlSource;
  private LinearLayout mLlResult;
  private LineUpdateHexArrayAdapter mAdapterSource;
  private LineUpdateHexArrayAdapter mAdapterResult;
  ActivityLineUpdateBinding binding;

  /**
   * Starts an activity.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   * @param texts                  The texts.
   * @param file                   The file name.
   * @param position               The item position.
   * @param nbLines                The number of lines.
   * @param change                 A change is detected?
   */
  public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher,
                                   final byte[] texts, final String file,
                                   final int position,
                                   final int nbLines,
                                   final boolean change,
                                   final boolean sequential) {
    Intent intent = new Intent(c, LineUpdateActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_TEXTS, texts);
    intent.putExtra(ACTIVITY_EXTRA_POSITION, position);
    intent.putExtra(ACTIVITY_EXTRA_NB_LINES, nbLines);
    intent.putExtra(ACTIVITY_EXTRA_FILENAME, file);
    intent.putExtra(ACTIVITY_EXTRA_CHANGE, change);
    intent.putExtra(ACTIVITY_EXTRA_SEQUENTIAL, sequential);
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

    binding = ActivityLineUpdateBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    mApp = MyApplication.getInstance();

    initAds();
    initStartAppAds();

    ListView lvSource = findViewById(R.id.lvSource);
    ListView lvResult = findViewById(R.id.lvResult);
    mLlSource = findViewById(R.id.llSource);
    mLlResult = findViewById(R.id.llResult);
    mIvVisibilitySource = findViewById(R.id.ivVisibilitySource);
    mIvVisibilityResult = findViewById(R.id.ivVisibilityResult);
    TextView tvLabelSource = findViewById(R.id.tvLabelSource);
    TextView tvLabelResult = findViewById(R.id.tvLabelResult);

    HexTextArrayAdapter.LineNumbersTitle titleSource = new HexTextArrayAdapter.LineNumbersTitle();
    titleSource.titleContent = findViewById(R.id.titleContentSource);
    titleSource.titleLineNumbers = findViewById(R.id.titleLineNumbersSource);
    HexTextArrayAdapter.LineNumbersTitle titleResult = new HexTextArrayAdapter.LineNumbersTitle();
    titleResult.titleContent = findViewById(R.id.titleContentResult);
    titleResult.titleLineNumbers = findViewById(R.id.titleLineNumbersResult);
    AppCompatCheckBox chkSmartInput = findViewById(R.id.chkSmartInput);
    AppCompatCheckBox chkOverwrite = findViewById(R.id.chkOverwrite);


    mEtInputHex = findViewById(R.id.etInputHex);
    mTilInputHex = findViewById(R.id.tilInputHex);

    mIvVisibilitySource.setOnClickListener(this);
    mIvVisibilityResult.setOnClickListener(this);
    tvLabelSource.setOnClickListener(this);
    tvLabelResult.setOnClickListener(this);

    if (!mApp.isLineEditSrcExpanded())
      animateVisibility(mIvVisibilitySource, mLlSource);
    if (!mApp.isLineEditRstExpanded())
      animateVisibility(mIvVisibilityResult, mLlResult);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /* init */
    mChange = false;
    List<String> list = new ArrayList<>();
    StringBuilder sbHex = new StringBuilder();
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      byte[] array = extras.getByteArray(ACTIVITY_EXTRA_TEXTS);
      mRefLength = array.length;
      List<LineEntry> li = SysHelper.formatBuffer(array, null, SysHelper.MAX_BY_ROW_8);
      mPosition = extras.getInt(ACTIVITY_EXTRA_POSITION);
      mNbLines = extras.getInt(ACTIVITY_EXTRA_NB_LINES);
      mFile = extras.getString(ACTIVITY_EXTRA_FILENAME);
      mChange = extras.getBoolean(ACTIVITY_EXTRA_CHANGE);
      mSequential = extras.getBoolean(ACTIVITY_EXTRA_SEQUENTIAL);
      for (LineEntry ld : li) {
        String s = ld.toString();
        list.add(s);
        sbHex.append(s.substring(0, 23).trim()).append(" ");
      }
    }
    mAdapterSource = new LineUpdateHexArrayAdapter(this, lvSource, titleSource, list);
    mAdapterResult = new LineUpdateHexArrayAdapter(this, lvResult, titleResult, new ArrayList<>(list));
    lvSource.setAdapter(mAdapterSource);
    lvResult.setAdapter(mAdapterResult);

    chkSmartInput.setChecked(mApp.isSmartInput());
    chkSmartInput.setOnCheckedChangeListener((comp, isChecked) -> mApp.setSmartInput(isChecked));
    chkOverwrite.setChecked(mApp.isOverwrite());
    chkOverwrite.setOnCheckedChangeListener((comp, isChecked) -> mApp.setOverwrite(isChecked));


    if (mFile != null) {
      UIHelper.setTitle(this, getResources().getConfiguration().orientation, false, mFile, mChange);
    }
    if (mPosition == -1) {
      mPosition = 0;
    }
    mHex = sbHex.toString();
    if (mHex.endsWith(" "))
      mHex = mHex.substring(0, mHex.length() - 1);
    mEtInputHex.setText(mHex);
    mEtInputHex.addTextChangedListener(new LineUpdateTextWatcher(mAdapterResult, mTilInputHex, mApp));
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mAdapterSource.notifyDataSetChanged();
    mAdapterResult.notifyDataSetChanged();
    // Checks the orientation of the screen
    if (mFile != null && !mFile.isEmpty()) {
      UIHelper.setTitle(this, newConfig.orientation, false, mFile, mChange);
    }

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      mEtInputHex.setTextSize(mApp.getListSettingsLineEditLandscape().getFontSize());
    } else {
      mEtInputHex.setTextSize(mApp.getListSettingsLineEditPortrait().getFontSize());
    }
  }

  /**
   * Called when the options menu is clicked.
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.line_update, menu);
    return true;
  }


  private void initStartAppAds() {

    //    StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
    StartAppSDK.init(this, BuildConfig.APPLICATION_ID, true);

  }

  private void initAds() {
    Appodeal.setTesting(BuildConfig.DEBUG);
   String APP_KEY = "0735ee422c20c34818eb8d9ef96eefe3e8028bd4334ece68";
    Appodeal.setBannerViewId(R.id.appodealBannerView);
    Appodeal.initialize(this, APP_KEY, Appodeal.BANNER);
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
    } else if (item.getItemId() == R.id.action_delete) {
      mEtInputHex.setText("");
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      final String validate = mEtInputHex.getText() == null ? "" : mEtInputHex.getText().toString().trim().replaceAll(" ", "").toLowerCase(Locale.US);
      if (!SysHelper.isValidHexLine(validate)) {
        mTilInputHex.setError(" "); /* only for the color */
        return false;
      }
      if (mSequential) {
        final byte[] buf = SysHelper.hexStringToByteArray(validate);
        if (mRefLength != buf.length) {
          UIHelper.showErrorDialog(this, getTitle(),
              getString(R.string.error_open_sequential_add_or_delete_data));
          return super.onOptionsItemSelected(item);
        }
      }
      Intent i = new Intent();
      i.putExtra(RESULT_POSITION, mPosition);
      i.putExtra(RESULT_NB_LINES, mNbLines);
      i.putExtra(RESULT_REFERENCE_STRING, mHex.replaceAll(" ", ""));
      i.putExtra(RESULT_NEW_STRING, validate);
      setResult(RESULT_OK, i);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when a view has been clicked.
   *
   * @param v The view that was clicked.
   */
  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.ivVisibilitySource || v.getId() == R.id.tvLabelSource) {
      animateVisibility(mIvVisibilitySource, mLlSource);
      mApp.setLineEditSrcExpanded(mLlSource.getVisibility() == View.VISIBLE);
    } else if (v.getId() == R.id.ivVisibilityResult || v.getId() == R.id.tvLabelResult) {
      animateVisibility(mIvVisibilityResult, mLlResult);
      mApp.setLineEditRstExpanded(mLlResult.getVisibility() == View.VISIBLE);
    }
  }

  private void animateVisibility(ImageView iv, View v) {
    if (v.getVisibility() == View.VISIBLE) {
      TransitionManager.beginDelayedTransition(findViewById(R.id.base_view),
          new AutoTransition());
      v.setVisibility(View.GONE);
      if (iv != null)
        iv.setImageResource(R.drawable.ic_expand_more);
    } else {
      TransitionManager.beginDelayedTransition(findViewById(R.id.base_view),
          new AutoTransition());
      v.setVisibility(View.VISIBLE);
      if (iv != null)
        iv.setImageResource(R.drawable.ic_expand_less);
    }
  }
}
