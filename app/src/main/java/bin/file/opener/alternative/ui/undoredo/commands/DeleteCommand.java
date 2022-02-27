package bin.file.opener.alternative.ui.undoredo.commands;

import android.util.Log;

import java.util.List;
import java.util.Map;

import bin.file.opener.alternative.models.LineEntry;
import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.alternative.ui.adapters.HexTextArrayAdapter;
import bin.file.opener.alternative.utils.SysHelper;
import bin.file.opener.alternative.ui.undoredo.ICommand;


public class DeleteCommand implements ICommand {
  private final Map<Integer, LineEntry> mList;
  private final MainActivity mActivity;

  public DeleteCommand(final MainActivity activity, final Map<Integer, LineEntry> entries) {
    mList = entries;
    mActivity = activity;
  }

  /**
   * Execute the command.
   */
  public void execute() {
    Log.i(getClass().getName(), "execute");
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */
    List<Integer> list = SysHelper.getMapKeys(mList);

    for (int i = list.size() - 1; i >= 0; i--) {
      int position = list.get(i);
      adapter.getEntries().removeItem(position);
    }
    /* rebuilds origin indexes */
    adapter.getEntries().reloadAllIndexes(0);
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    Log.i(getClass().getName(), "unExecute");
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    for (Integer i : SysHelper.getMapKeys(mList)) {
      LineEntry ld = mList.get(i);
      if (ld != null) {
        adapter.getEntries().addItem(ld.getIndex(), ld);
      }
    }
    /* rebuilds origin indexes */
    adapter.getEntries().reloadAllIndexes(0);

    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }
}
