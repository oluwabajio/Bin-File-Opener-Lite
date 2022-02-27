package bin.file.opener.alternative.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuCompat;
import androidx.multidex.BuildConfig;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.databinding.ActivityMainBinding;
import bin.file.opener.alternative.ui.dialog.GoToDialog;
import bin.file.opener.alternative.ui.dialog.SequentialOpenDialog;
import bin.file.opener.alternative.ui.payload.PayloadHexHelper;
import bin.file.opener.alternative.ui.payload.PayloadPlainSwipe;
import bin.file.opener.alternative.ui.popup.MainPopupWindow;
import bin.file.opener.alternative.ui.popup.PopupCheckboxHelper;
import bin.file.opener.alternative.ui.tasks.TaskOpen;
import bin.file.opener.alternative.ui.tasks.TaskSave;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.models.FileData;
import bin.file.opener.alternative.models.LineEntry;
import bin.file.opener.alternative.ui.activities.settings.SettingsActivity;
import bin.file.opener.alternative.ui.adapters.SearchableListArrayAdapter;
import bin.file.opener.alternative.ui.launchers.LauncherLineUpdate;
import bin.file.opener.alternative.ui.launchers.LauncherOpen;
import bin.file.opener.alternative.ui.launchers.LauncherRecentlyOpen;
import bin.file.opener.alternative.ui.launchers.LauncherSave;
import bin.file.opener.alternative.ui.undoredo.UnDoRedo;
import bin.file.opener.alternative.utils.SysHelper;


public class MainActivity extends AbstractBaseMainActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
    private FileData mFileData = null;
    private ConstraintLayout mIdleView = null;
    private MenuItem mSearchMenu = null;
    private String mSearchQuery = "";
    private PayloadPlainSwipe mPayloadPlainSwipe = null;
    private LauncherLineUpdate mLauncherLineUpdate = null;
    private LauncherSave mLauncherSave = null;
    private LauncherOpen mLauncherOpen = null;
    private LauncherRecentlyOpen mLauncherRecentlyOpen = null;
    private UnDoRedo mUnDoRedo = null;
    private MainPopupWindow mPopup = null;
    private PayloadHexHelper mPayloadHexHelper = null;
    private GoToDialog mGoToDialog = null;
    private SequentialOpenDialog mSequentialOpenDialog = null;
    ActivityMainBinding binding;
    private String APP_KEY = "0735ee422c20c34818eb8d9ef96eefe3e8028bd4334ece68";
    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initAds();
        initStartAppAds();

        mUnDoRedo = new UnDoRedo(this);

        mPopup = new MainPopupWindow(this, mUnDoRedo, this::onPopupItemClick);

        LinearLayout mainLayout = binding.mainLayout;
        mIdleView = binding.idleView;
        mIdleView.setVisibility(View.VISIBLE);

        binding.buttonOpenFile.setOnClickListener((v) -> onPopupItemClick(R.id.action_open));
        binding.buttonPartialOpenFile.setOnClickListener((v) -> onPopupItemClick(R.id.action_open_sequential));
        binding.buttonRecentlyOpen.setOnClickListener((v) -> onPopupItemClick(R.id.action_recently_open));
        binding.buttonRecentlyOpen.setEnabled(!mApp.getRecentlyOpened().list().isEmpty());
        mPayloadHexHelper = new PayloadHexHelper();
        mPayloadHexHelper.onCreate(this);

        mPayloadPlainSwipe = new PayloadPlainSwipe();
        mPayloadPlainSwipe.onCreate(this);

        mLauncherOpen = new LauncherOpen(this, mainLayout);
        mLauncherSave = new LauncherSave(this);
        mLauncherLineUpdate = new LauncherLineUpdate(this);
        mLauncherRecentlyOpen = new LauncherRecentlyOpen(this);

        mGoToDialog = new GoToDialog(this);
        mSequentialOpenDialog = new SequentialOpenDialog(this);

        if (savedInstanceState == null) handleIntent(getIntent());
    }

    private void initStartAppAds() {

        //    StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
        StartAppSDK.init(this, BuildConfig.APPLICATION_ID, true);

    }

    private void initAds() {
        Appodeal.setTesting(BuildConfig.DEBUG);

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
     * Called when the activity is resumed.
     */
    public void onResume() {
        super.onResume();

        setRequestedOrientation(mApp.getScreenOrientation(null));
        if (mPopup != null) mPopup.dismiss();
        mApp.applyApplicationLanguage(this);
        /* refresh */
        onOpenResult(!FileData.isEmpty(mFileData), false);
        if (mPayloadHexHelper.isVisible()) mPayloadHexHelper.refreshAdapter();
        else if (mPayloadPlainSwipe.isVisible()) mPayloadPlainSwipe.refreshAdapter();

        Appodeal.show(this, Appodeal.BANNER_VIEW);
    }

    @Override
    public void onBackPressed() {
        StartAppAd.onBackPressed(MainActivity.this);
        super.onBackPressed();
    }

    /**
     * Handles activity intents.
     *
     * @param intent The intent.
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            doSearch(mSearchQuery == null ? "" : mSearchQuery);
        } else {
            if (intent.getData() != null) {
                closeOrphanDialog();
                Uri uri = getIntent().getData();
                if (uri != null) {
                    boolean addRecent;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                        addRecent = false;
                    } else addRecent = takeUriPermissions(this, uri, false);
                    FileData fd = new FileData(this, uri, true);
                    mApp.setSequential(true);
                    final Runnable r = () -> mLauncherOpen.processFileOpen(fd, addRecent);
                    if (mUnDoRedo.isChanged()) {// a save operation is pending?
                        UIHelper.confirmFileChanged(this, mFileData, r, () -> new TaskSave(this, this).execute(new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
                    } else {
                        r.run();
                    }
                }
            }
        }
    }

    /**
     * Called to create the option menu.
     *
     * @param menu The main menu.
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        mSearchMenu = menu.findItem(R.id.action_search);
        mSearchMenu.setVisible(false);
        setSearchView(mSearchMenu);
        return true;
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package,
     * or if a client used the Intent#FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Performs the research.
     *
     * @param queryStr The query string.
     */
    @Override
    public void doSearch(String queryStr) {
        mSearchQuery = queryStr;
        final SearchableListArrayAdapter laa = ((mPayloadPlainSwipe.isVisible()) ? mPayloadPlainSwipe.getAdapter() : mPayloadHexHelper.getAdapter());
        laa.getFilter().filter(queryStr);
    }

    /**
     * Method called when the file is saved.
     *
     * @param fd           The new FileData.
     * @param success      The result.
     * @param userRunnable User runnable (can be null).
     */
    @Override
    public void onSaveResult(FileData fd, boolean success, final Runnable userRunnable) {
        if (success) {
            mUnDoRedo.refreshChange();
            if (mFileData.isOpenFromAppIntent()) {
                if (mPopup != null) mPopup.setSaveMenuEnable(true);
            }
            mFileData = fd;
            mFileData.clearOpenFromAppIntent();
            setTitle(getResources().getConfiguration());
            mPayloadHexHelper.resetUpdateStatus();
        } else mApp.getRecentlyOpened().remove(fd);
        if (userRunnable != null) userRunnable.run();
    }

    /**
     * Method called when the file is opened.
     *
     * @param success  The result.
     * @param fromOpen Called from open
     */
    @Override
    public void onOpenResult(boolean success, boolean fromOpen) {
        setMenuVisible(mSearchMenu, success);
        boolean checked = mPopup != null && mPopup.getPlainText() != null && mPopup.getPlainText().setEnable(success);
        if (!FileData.isEmpty(mFileData) && mFileData.isOpenFromAppIntent()) {
            if (mPopup != null) mPopup.setSaveMenuEnable(false);
        } else {
            if (mPopup != null) mPopup.setSaveMenuEnable(success);
        }
        if (mPopup != null) {
            mPopup.setMenusEnable(success);
        }
        if (success) {
            mIdleView.setVisibility(View.GONE);
            mPayloadHexHelper.setVisible(!checked);
            mPayloadPlainSwipe.setVisible(checked);
            if (fromOpen) mUnDoRedo.clear();
        } else {
            mIdleView.setVisibility(View.VISIBLE);
            mPayloadHexHelper.setVisible(false);
            mPayloadPlainSwipe.setVisible(false);
            mFileData = null;
            mUnDoRedo.clear();
        }
        setTitle(getResources().getConfiguration());
    }

    /**
     * Sets the activity title.
     *
     * @param cfg Screen configuration.
     */
    public void setTitle(Configuration cfg) {
        UIHelper.setTitle(this, cfg.orientation, true, FileData.isEmpty(mFileData) ? null : mFileData.getName(), mUnDoRedo.isChanged());
        if ((!FileData.isEmpty(mFileData) && !mFileData.isOpenFromAppIntent()))
            mPopup.setSaveMenuEnable(mUnDoRedo.isChanged());
    }


    /**
     * Called by the system when the device configuration changes while your activity is running.
     *
     * @param newConfig The new device configuration. This value cannot be null.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPayloadPlainSwipe.isVisible()) {
            mPayloadPlainSwipe.refresh();
        } else if (mPayloadHexHelper.isVisible())
            mPayloadHexHelper.getAdapter().notifyDataSetChanged();
        // Checks the orientation of the screen
        if (!FileData.isEmpty(mFileData)) {
            setTitle(newConfig);
        }
    }

    /**
     * Handles the click on the popup menu item.
     *
     * @param id The view id.
     */
    public void onPopupItemClick(int id) {
        if (id == R.id.action_open || id == R.id.action_open_sequential) {
            popupActionOpen(id == R.id.action_open_sequential);
        } else if (id == R.id.action_recently_open) {
            mLauncherRecentlyOpen.startActivity();
        } else if (id == R.id.action_save) {
            popupActionSave();
        } else if (id == R.id.action_save_as) {
            popupActionSaveAs();
        } else if (id == R.id.action_close) {
            popupActionClose();
        } else if (id == R.id.action_settings) {
            SettingsActivity.startActivity(this, !FileData.isEmpty(mFileData), mUnDoRedo.isChanged());
        } else if (id == R.id.action_undo) {
            mUnDoRedo.undo();
        } else if (id == R.id.action_redo) {
            mUnDoRedo.redo();
        } else if (id == R.id.action_go_to) {
            popupActionGoTo();
        } else if (mPopup != null) {
            specialPopupActions(id);
        }
    }

    /**
     * Handles the click on the popup menu item.
     *
     * @param id The view id.
     */
    private void specialPopupActions(int id) {
        if (mPopup.getPlainText() != null && mPopup.getPlainText().containsId(id, false)) {
            popupActionPlainText(id, mPopup.getPlainText(), mPopup.getLineNumbers());
        } else if (mPopup.getLineNumbers() != null && mPopup.getLineNumbers().containsId(id, false)) {
            popupActionLineNumbers(id, mPopup.getLineNumbers());
        }
    }

    /**
     * Called when the user select an option menu item.
     *
     * @param item The selected item.
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_more) {
            mPopup.show(findViewById(R.id.action_more));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter).
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LineEntry e = mPayloadHexHelper.getAdapter().getItem(position);
        if (e == null) return;
        if (mPayloadPlainSwipe.isVisible()) {
            UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (Byte b : e.getRaw())
            byteArrayOutputStream.write(b);
        mLauncherLineUpdate.startActivity(byteArrayOutputStream.toByteArray(), position, 1);
    }

    /**
     * Called to handle the click on the back button.
     */
    @Override
    public void onExit() {
        if (mUnDoRedo.isChanged()) {// a save operation is pending?
            Runnable r = this::finish;
            UIHelper.confirmFileChanged(this, mFileData, r, () -> new TaskSave(this, this).execute(new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
        } else {
            finish();
        }
    }

    /* ------------ EXPORTED METHODS ------------ */

    /**
     * Returns the menu RecentlyOpen
     *
     * @return MenuItem
     */
    public TextView getMenuRecentlyOpen() {
        return mPopup == null ? null : mPopup.getMenuRecentlyOpen();
    }

    /**
     * Returns the file data.
     *
     * @return FileData
     */
    public FileData getFileData() {
        return mFileData;
    }

    /**
     * Sets the file data.
     *
     * @param fd FileData
     */
    public void setFileData(FileData fd) {
        mFileData = fd;
    }

    /**
     * Returns the search query.
     *
     * @return String
     */
    public String getSearchQuery() {
        return mSearchQuery;
    }

    /**
     * Returns the PayloadHexHelper
     *
     * @return PayloadHexHelper
     */
    public PayloadHexHelper getPayloadHex() {
        return mPayloadHexHelper;
    }

    /**
     * Returns the PayloadPlainSwipe
     *
     * @return PayloadPlainSwipe
     */
    public PayloadPlainSwipe getPayloadPlain() {
        return mPayloadPlainSwipe;
    }

    /**
     * Returns the LauncherOpen
     *
     * @return LauncherOpen
     */
    public LauncherOpen getLauncherOpen() {
        return mLauncherOpen;
    }

    /**
     * Returns the LauncherLineUpdate
     *
     * @return LauncherLineUpdate
     */
    public LauncherLineUpdate getLauncherLineUpdate() {
        return mLauncherLineUpdate;
    }

    /**
     * Returns the undo/redo.
     *
     * @return UnDoRedo
     */
    public UnDoRedo getUnDoRedo() {
        return mUnDoRedo;
    }

    /**
     * Returns the SequentialOpenDialog.
     *
     * @return SequentialOpenDialog
     */
    public SequentialOpenDialog getSequentialOpenDialog() {
        return mSequentialOpenDialog;
    }

    /* ------------ POPUP ACTIONS ------------ */

    /**
     * Action when the user clicks on the "open" or "sequential opening" menu.
     */
    private void popupActionOpen(boolean sequential) {
        mApp.setSequential(sequential);
        final Runnable r = () -> mLauncherOpen.startActivity();
        if (mUnDoRedo.isChanged()) {// a save operation is pending?
            UIHelper.confirmFileChanged(this, mFileData, r, () -> new TaskSave(this, this).execute(new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
        } else r.run();
    }

    /**
     * Action when the user clicks on the "save" menu.
     */
    private void popupActionSave() {
        if (FileData.isEmpty(mFileData)) {
            UIHelper.toast(this, getString(R.string.open_a_file_before));
            return;
        }
        new TaskSave(this, this).execute(new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), null));
        setTitle(getResources().getConfiguration());
    }

    /**
     * Action when the user clicks on the "save as" menu.
     */
    private void popupActionSaveAs() {
        if (FileData.isEmpty(mFileData)) {
            UIHelper.toast(this, getString(R.string.open_a_file_before));
            return;
        }
        mLauncherSave.startActivity();
    }

    /**
     * Action when the user clicks on the "plain text" menu.
     *
     * @param id          Action id.
     * @param plainText   Plain text checkbox.
     * @param lineNumbers Line numbers checkbox.
     */
    private void popupActionPlainText(int id, PopupCheckboxHelper plainText, PopupCheckboxHelper lineNumbers) {
        if (plainText.containsId(id, true)) plainText.toggleCheck();
        boolean checked = plainText.isChecked();
        mPayloadPlainSwipe.setVisible(checked);
        mPayloadHexHelper.setVisible(!checked);
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) doSearch(mSearchQuery);
        refreshLineNumbers(lineNumbers);
    }

    /**
     * Refreshes the lines number
     *
     * @param lineNumbers Line numbers checkbox.
     */
    private void refreshLineNumbers(PopupCheckboxHelper lineNumbers) {
        if (lineNumbers != null) {
            boolean checked = lineNumbers.isChecked();
            if (mPayloadHexHelper.isVisible()) {
                if (mApp.isLineNumber() && !checked) {
                    lineNumbers.setChecked(true);
                    mPayloadHexHelper.refreshLineNumbers();
                }
                lineNumbers.setEnable(true);
            } else if (mPayloadPlainSwipe.isVisible()) {
                if (checked) {
                    lineNumbers.setChecked(false);
                    mPayloadHexHelper.refreshLineNumbers();
                }
                lineNumbers.setEnable(false);
            }
            mPopup.refreshGoToName();
        }
    }

    /**
     * Action when the user clicks on the "line numbers" menu.
     *
     * @param id          Action id.
     * @param lineNumbers Line numbers checkbox.
     */
    private void popupActionLineNumbers(int id, PopupCheckboxHelper lineNumbers) {
        if (lineNumbers.containsId(id, true)) lineNumbers.toggleCheck();
        boolean checked = lineNumbers.isChecked();
        mApp.setLineNumber(checked);
        if (mPayloadHexHelper.isVisible()) mPayloadHexHelper.refreshLineNumbers();
        mPopup.refreshGoToName();
    }

    /**
     * Action when the user clicks on the "close" menu.
     */
    private void popupActionClose() {
        final Runnable r = () -> {
            onOpenResult(false, false);
            mPayloadPlainSwipe.getAdapter().clear();
            mPayloadHexHelper.getAdapter().clear();
            cancelSearch();
            binding.buttonRecentlyOpen.setEnabled(!mApp.getRecentlyOpened().list().isEmpty());
        };
        if (mUnDoRedo.isChanged()) {// a save operation is pending?
            UIHelper.confirmFileChanged(this, mFileData, r, () -> new TaskSave(this, this).execute(new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
        } else r.run();
    }

    /**
     * Action when the user clicks on the "go to xxx" menu.
     */
    private void popupActionGoTo() {
        if (mPopup.getPlainText().isChecked())
            setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.LINE_PLAIN));
        else if (mPopup.getLineNumbers().isChecked())
            setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.ADDRESS));
        else setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.LINE_HEX));
    }


    public boolean takeUriPermissions(final Context c, final Uri uri, boolean fromDir) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return true;
        boolean success = false;
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            c.getContentResolver().takePersistableUriPermission(uri, takeFlags);
            if (!fromDir) {
                Uri dir = getParentUri(uri);
                if (!hasUriPermission(c, dir, false))
                    try {
                        c.getContentResolver().takePersistableUriPermission(dir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (Exception e) {
                        Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
                    }
            }
            success = true;
        } catch (Exception e) {
            Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
        return success;
    }
    public  Uri getParentUri(final Uri uri) {
        final String filename = getFileName(uri);
        final String encoded = uri.getEncodedPath();
        String parent = encoded.substring(0, encoded.length() - filename.length());
        if (parent.endsWith("%2F"))
            parent = parent.substring(0, parent.length() - 3);
        String path;
        final String documentPrimary = "/document/primary%3A";
        if (parent.startsWith(documentPrimary))
            path = "/tree/primary%3A" + parent.substring(documentPrimary.length());
        else
            path = parent;
        return Uri.parse(uri.getScheme() + "://" + uri.getHost() + path);
    }
    public String getFileName(final Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = MyApplication.getInstance().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                Log.e("TAG", "Exception: " + e.getMessage()/*, e*/);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean hasUriPermission(final Context c, final Uri uri, boolean readPermission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return true;
        final List<UriPermission> list = c.getContentResolver().getPersistedUriPermissions();
        boolean found = false;
        for (UriPermission up : list) {
            if (up.getUri().equals(uri) && ((up.isReadPermission() && readPermission) || (up.isWritePermission() && !readPermission))) {
                found = true;
                break;
            }
        }
        return found;
    }


}

