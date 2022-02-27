package bin.file.opener.alternative.ui.activities.settings;

import android.content.Context;
import android.content.Intent;

import bin.file.opener.alternative.ui.fragments.AbstractSettingsFragment;
import bin.file.opener.alternative.ui.fragments.SettingsFragmentListsLandscape;


public class SettingsListsLandscapeActivity extends AbstractSettingsActivity {

  /**
   * Starts an activity.
   *
   * @param c Android context.
   */
  public static void startActivity(final Context c) {
    Intent intent = new Intent(c, SettingsListsLandscapeActivity.class);
    c.startActivity(intent);
  }

  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public AbstractSettingsFragment onUserCreate() {
    return new SettingsFragmentListsLandscape(this);
  }

}