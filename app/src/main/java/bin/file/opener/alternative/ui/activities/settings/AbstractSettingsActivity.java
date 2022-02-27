package bin.file.opener.alternative.ui.activities.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.R;
import bin.file.opener.alternative.ui.fragments.AbstractSettingsFragment;


public abstract class AbstractSettingsActivity extends AppCompatActivity {

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
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public abstract AbstractSettingsFragment onUserCreate();

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_settings);

    //If you want to insert data in your settings
    AbstractSettingsFragment prefs = onUserCreate();

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings_container, prefs)
        .commit();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
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
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}