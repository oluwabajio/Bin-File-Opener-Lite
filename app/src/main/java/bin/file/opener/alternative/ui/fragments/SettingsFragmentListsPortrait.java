package bin.file.opener.alternative.ui.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.R;


public class SettingsFragmentListsPortrait extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener {
  protected CheckBoxPreference mHexRowHeightAutoPortrait;
  protected Preference mHexRowHeightPortrait;
  protected Preference mHexFontSizePortrait;
  protected CheckBoxPreference mHexRowHeightAutoLineNumbersPortrait;
  protected Preference mHexRowHeightLineNumbersPortrait;
  protected Preference mHexFontSizeLineNumbersPortrait;
  protected CheckBoxPreference mPlainRowHeightAutoPortrait;
  protected Preference mPlainRowHeightPortrait;
  protected Preference mPlainFontSizePortrait;
  protected CheckBoxPreference mLineEditRowHeightAutoPortrait;
  protected Preference mLineEditRowHeightPortrait;
  protected Preference mLineEditFontSizePortrait;

  public SettingsFragmentListsPortrait(AppCompatActivity owner) {
    super(owner);
  }

  /**
   * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
   * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
   * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state,
   *                           this is the state.
   * @param rootKey            If non-null, this preference fragment should be rooted at the
   *                           {@link PreferenceScreen} with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences_lists_portrait, rootKey);

    CheckBoxPreference hexDisplayDataPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_DISPLAY_DATA);
    CheckBoxPreference hexDisplayDataLineNumbersPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_DISPLAY_DATA_LINE_NUMBERS);
    mHexRowHeightAutoPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_ROW_HEIGHT_AUTO);
    mHexRowHeightPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_ROW_HEIGHT);
    mHexFontSizePortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_FONT_SIZE);
    mHexRowHeightAutoLineNumbersPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS);
    mHexRowHeightLineNumbersPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_ROW_HEIGHT_LINE_NUMBERS);
    mHexFontSizeLineNumbersPortrait = findPreference(MyApplication.DLL_PORTRAIT_HEX_FONT_SIZE_LINE_NUMBERS);
    mPlainRowHeightAutoPortrait = findPreference(MyApplication.DLL_PORTRAIT_PLAIN_ROW_HEIGHT_AUTO);
    mPlainRowHeightPortrait = findPreference(MyApplication.DLL_PORTRAIT_PLAIN_ROW_HEIGHT);
    mPlainFontSizePortrait = findPreference(MyApplication.DLL_PORTRAIT_PLAIN_FONT_SIZE);
    mLineEditRowHeightAutoPortrait = findPreference(MyApplication.DLL_PORTRAIT_LINE_EDIT_ROW_HEIGHT_AUTO);
    mLineEditRowHeightPortrait = findPreference(MyApplication.DLL_PORTRAIT_LINE_EDIT_ROW_HEIGHT);
    mLineEditFontSizePortrait = findPreference(MyApplication.DLL_PORTRAIT_LINE_EDIT_FONT_SIZE);

    mHexRowHeightAutoPortrait.setOnPreferenceClickListener(this);
    mHexRowHeightPortrait.setOnPreferenceClickListener(this);
    mHexFontSizePortrait.setOnPreferenceClickListener(this);
    mHexRowHeightAutoLineNumbersPortrait.setOnPreferenceClickListener(this);
    mHexRowHeightLineNumbersPortrait.setOnPreferenceClickListener(this);
    mHexFontSizeLineNumbersPortrait.setOnPreferenceClickListener(this);
    mPlainRowHeightAutoPortrait.setOnPreferenceClickListener(this);
    mPlainRowHeightPortrait.setOnPreferenceClickListener(this);
    mPlainFontSizePortrait.setOnPreferenceClickListener(this);
    mLineEditRowHeightAutoPortrait.setOnPreferenceClickListener(this);
    mLineEditRowHeightPortrait.setOnPreferenceClickListener(this);
    mLineEditFontSizePortrait.setOnPreferenceClickListener(this);

    if (hexDisplayDataPortrait != null)
      hexDisplayDataPortrait.setChecked(mApp.getListSettingsHexPortrait().isDisplayDataColumn());
    if (hexDisplayDataLineNumbersPortrait != null)
      hexDisplayDataLineNumbersPortrait.setChecked(mApp.getListSettingsHexLineNumbersPortrait().isDisplayDataColumn());

    mHexRowHeightAutoPortrait.setChecked(mApp.getListSettingsHexPortrait().isRowHeightAuto());
    mHexRowHeightPortrait.setEnabled(!mApp.getListSettingsHexPortrait().isRowHeightAuto());

    mHexRowHeightAutoLineNumbersPortrait.setChecked(mApp.getListSettingsHexLineNumbersPortrait().isRowHeightAuto());
    mHexRowHeightLineNumbersPortrait.setEnabled(!mApp.getListSettingsHexLineNumbersPortrait().isRowHeightAuto());

    mPlainRowHeightAutoPortrait.setChecked(mApp.getListSettingsPlainPortrait().isRowHeightAuto());
    mPlainRowHeightPortrait.setEnabled(!mApp.getListSettingsPlainPortrait().isRowHeightAuto());

    mLineEditRowHeightAutoPortrait.setChecked(mApp.getListSettingsLineEditPortrait().isRowHeightAuto());
    mLineEditRowHeightPortrait.setEnabled(!mApp.getListSettingsLineEditPortrait().isRowHeightAuto());
  }


  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mHexRowHeightAutoPortrait)) {
      mHexRowHeightPortrait.setEnabled(!mHexRowHeightAutoPortrait.isChecked());
    } else if (preference.equals(mHexRowHeightPortrait)) {
      displayDialog(mHexRowHeightPortrait.getTitle(),
          mApp.getListSettingsHexPortrait().getRowHeight(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          (n) -> mApp.getListSettingsHexPortrait().setRowHeight(n));
    } else if (preference.equals(mHexFontSizePortrait)) {
      displayDialog(mHexFontSizePortrait.getTitle(),
          mApp.getListSettingsHexPortrait().getFontSize(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          (n) -> mApp.getListSettingsHexPortrait().setFontSize(n), true);
    } else if (preference.equals(mHexRowHeightAutoLineNumbersPortrait)) {
      mHexRowHeightLineNumbersPortrait.setEnabled(!mHexRowHeightAutoLineNumbersPortrait.isChecked());
    } else if (preference.equals(mHexRowHeightLineNumbersPortrait)) {
      displayDialog(mHexRowHeightLineNumbersPortrait.getTitle(),
          mApp.getListSettingsHexLineNumbersPortrait().getRowHeight(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          (n) -> mApp.getListSettingsHexLineNumbersPortrait().setRowHeight(n));
    } else if (preference.equals(mHexFontSizeLineNumbersPortrait)) {
      displayDialog(mHexFontSizeLineNumbersPortrait.getTitle(),
          mApp.getListSettingsHexLineNumbersPortrait().getFontSize(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          (n) -> mApp.getListSettingsHexLineNumbersPortrait().setFontSize(n), true);
    } else if (preference.equals(mPlainRowHeightAutoPortrait)) {
      mPlainRowHeightPortrait.setEnabled(!mPlainRowHeightAutoPortrait.isChecked());
    } else if (preference.equals(mPlainRowHeightPortrait)) {
      displayDialog(mPlainRowHeightPortrait.getTitle(),
          mApp.getListSettingsPlainPortrait().getRowHeight(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          (n) -> mApp.getListSettingsPlainPortrait().setRowHeight(n));
    } else if (preference.equals(mPlainFontSizePortrait)) {
      displayDialog(mPlainFontSizePortrait.getTitle(),
          mApp.getListSettingsPlainPortrait().getFontSize(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          (n) -> mApp.getListSettingsPlainPortrait().setFontSize(n), true);
    } else if (preference.equals(mLineEditRowHeightAutoPortrait)) {
      mLineEditRowHeightPortrait.setEnabled(!mLineEditRowHeightAutoPortrait.isChecked());
    } else if (preference.equals(mLineEditRowHeightPortrait)) {
      displayDialog(mLineEditRowHeightPortrait.getTitle(),
          mApp.getListSettingsLineEditPortrait().getRowHeight(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          (n) -> mApp.getListSettingsLineEditPortrait().setRowHeight(n));
    } else if (preference.equals(mLineEditFontSizePortrait)) {
      displayDialog(mLineEditFontSizePortrait.getTitle(),
          mApp.getListSettingsLineEditPortrait().getFontSize(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          (n) -> mApp.getListSettingsLineEditPortrait().setFontSize(n), true);
    }
    return false;
  }

}