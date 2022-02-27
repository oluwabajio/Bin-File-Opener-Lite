package bin.file.opener.alternative.ui.payload;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import bin.file.opener.alternative.ui.adapters.HexTextArrayAdapter;
import bin.file.opener.alternative.ui.utils.MultiChoiceCallback;
import bin.file.opener.alternative.MyApplication;
import bin.file.opener.R;
import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.alternative.ui.adapters.config.UserConfigLandscape;
import bin.file.opener.alternative.ui.adapters.config.UserConfigPortrait;


public class PayloadHexHelper {
  private MainActivity mActivity;
  private ListView mPayloadHex = null;
  private HexTextArrayAdapter mAdapterHex = null;
  private RelativeLayout mPayloadViewContainer = null;
  private LinearLayout mTitle = null;
  private TextView mTitleLineNumbers = null;
  private TextView mTitleContent = null;

  /**
   * Called when the activity is created.
   *
   * @param activity The owner activity
   */
  public void onCreate(final MainActivity activity) {
    mActivity = activity;
    mPayloadViewContainer = activity.findViewById(R.id.payloadViewContainer);
    mTitle = activity.findViewById(R.id.title);
    mTitleLineNumbers = activity.findViewById(R.id.titleLineNumbers);
    mTitleContent = activity.findViewById(R.id.titleContent);
    mPayloadHex = activity.findViewById(R.id.payloadView);

    mPayloadHex.setVisibility(View.GONE);
    mPayloadViewContainer.setVisibility(View.GONE);
    mTitleLineNumbers.setVisibility(View.GONE);
    mTitleContent.setVisibility(View.GONE);
    mTitle.setVisibility(View.GONE);

    HexTextArrayAdapter.LineNumbersTitle title = new HexTextArrayAdapter.LineNumbersTitle();
    title.titleContent = mTitleContent;
    title.titleLineNumbers = mTitleLineNumbers;

    mAdapterHex = new HexTextArrayAdapter(activity,
        new ArrayList<>(),
        title,
        new UserConfigPortrait(true),
        new UserConfigLandscape(true));
    mPayloadHex.setAdapter(mAdapterHex);
    mPayloadHex.setOnItemClickListener(activity);
    mPayloadHex.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    MultiChoiceCallback multiChoiceCallback = new MultiChoiceCallback(activity, mPayloadHex, mAdapterHex);
    mPayloadHex.setMultiChoiceModeListener(multiChoiceCallback);
  }

  /**
   * Resets the update status.
   */
  public void resetUpdateStatus() {
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      mAdapterHex.manualFilterUpdate(""); /* reset filter */
    mAdapterHex.getEntries().clearFilteredUpdated();
    if (!query.isEmpty())
      mAdapterHex.manualFilterUpdate(query);
    mAdapterHex.notifyDataSetChanged();
  }

  /**
   * Called to refresh the adapter.
   */
  public void refreshAdapter() {
    mAdapterHex.refresh();
  }

  /**
   * Called to refresh the line numbers.
   */
  public void refreshLineNumbers() {
    refreshLineNumbersVisibility();
    mAdapterHex.notifyDataSetChanged();
  }

  /**
   * Returns the hex adapter.
   *
   * @return HexTextArrayAdapter
   */
  public HexTextArrayAdapter getAdapter() {
    return mAdapterHex;
  }

  /**
   * Tests if the list view is visible.
   *
   * @return boolean
   */
  public boolean isVisible() {
    return mPayloadHex.getVisibility() == View.VISIBLE;
  }

  /**
   * Changes the list view visibility.
   *
   * @param b The new value
   */
  public void setVisible(boolean b) {
    mPayloadHex.setVisibility(b ? View.VISIBLE : View.GONE);
    mPayloadViewContainer.setVisibility(b ? View.VISIBLE : View.GONE);
    if (!b) {
      mTitleLineNumbers.setVisibility(View.GONE);
      mTitleContent.setVisibility(View.GONE);
      mTitle.setVisibility(View.GONE);
    } else
      refreshLineNumbersVisibility();
  }

  /**
   * Refreshes line numbers visibility.
   */
  private void refreshLineNumbersVisibility() {
    final boolean checked = MyApplication.getInstance().isLineNumber();
    mTitleLineNumbers.setVisibility(checked ? View.VISIBLE : View.GONE);
    mTitleContent.setVisibility(checked ? View.VISIBLE : View.GONE);
    mTitle.setVisibility(checked ? View.VISIBLE : View.GONE);
  }

  /**
   * Returns the ListView
   *
   * @return ListView
   */
  public ListView getListView() {
    return mPayloadHex;
  }
}
