package bin.file.opener.alternative.ui.adapters.config;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.ui.adapters.SearchableListArrayAdapter;


public class UserConfigPortrait implements SearchableListArrayAdapter.UserConfig {
  private final MyApplication mApp;
  private final boolean mIsHexList;

  public UserConfigPortrait(boolean isHexList) {
    mIsHexList = isHexList;
    mApp = MyApplication.getInstance();
  }

  @Override
  public float getFontSize() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().getFontSize();
      return mApp.getListSettingsHexPortrait().getFontSize();
    }
    return mApp.getListSettingsPlainPortrait().getFontSize();
  }

  @Override
  public int getRowHeight() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().getRowHeight();
      return mApp.getListSettingsHexPortrait().getRowHeight();
    }
    return mApp.getListSettingsPlainPortrait().getRowHeight();
  }

  @Override
  public boolean isRowHeightAuto() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().isRowHeightAuto();
      return mApp.getListSettingsHexPortrait().isRowHeightAuto();
    }
    return mApp.getListSettingsPlainPortrait().isRowHeightAuto();
  }

  @Override
  public boolean isDataColumnNotDisplayed() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return !mApp.getListSettingsHexLineNumbersPortrait().isDisplayDataColumn();
      return !mApp.getListSettingsHexPortrait().isDisplayDataColumn();
    }
    return false;
  }
}
