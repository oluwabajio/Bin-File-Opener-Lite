package bin.file.opener.alternative.ui.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;


public abstract class AbstractBaseMainActivity extends AppCompatActivity {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private SearchView mSearchView = null;
  private AlertDialog mOrphanDialog = null;
  protected MyApplication mApp = null;

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
    mApp = MyApplication.getInstance();


      mApp.setApplicationLanguage("en-US");



//    /* permissions */
//    boolean requestPermissions = true;
//    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//          ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//        requestPermissions = false;
//      }
//    }
//    if (requestPermissions)
//      ActivityCompat.requestPermissions(this, new String[]{
//          Manifest.permission.WRITE_EXTERNAL_STORAGE,
//          Manifest.permission.READ_EXTERNAL_STORAGE
//      }, 1);
  }

  protected void setSearchView(MenuItem si) {
    // Searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    if (searchManager != null) {
      mSearchView = (SearchView) si.getActionView();
      mSearchView.setSearchableInfo(searchManager
          .getSearchableInfo(getComponentName()));
      mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
          return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
          doSearch(s);
          return true;
        }
      });
    }
  }

  /**
   * Sets the visibility of the menu item.
   *
   * @param menu    MenuItem
   * @param visible If true then the item will be visible; if false it is hidden.
   */
  protected void setMenuVisible(final MenuItem menu, final boolean visible) {
    if (menu != null)
      menu.setVisible(visible);
  }

  /**
   * Sets the orphan dialog.
   *
   * @param orphan The dialog.
   */
  public void setOrphanDialog(AlertDialog orphan) {
    if (mOrphanDialog != null && mOrphanDialog.isShowing()) {
      mOrphanDialog.dismiss();
    }
    mOrphanDialog = orphan;
  }

  protected void closeOrphanDialog() {
    if (mOrphanDialog != null) {
      if (mOrphanDialog.isShowing())
        mOrphanDialog.dismiss();
      mOrphanDialog = null;
    }
  }

  /**
   * Performs the research.
   *
   * @param queryStr The query string.
   */
  public abstract void doSearch(String queryStr);

  /**
   * Cancels search.
   */
  protected void cancelSearch() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      doSearch("");
      mSearchView.setIconified(true);
    }
  }

  /**
   * Called to handle the exit of the application.
   */
  protected abstract void onExit();

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      cancelSearch();
    } else {
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        onExit();
        return;
      } else {
        UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
      }
      mLastBackPressed = System.currentTimeMillis();
    }
  }
}
