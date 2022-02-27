package bin.file.opener.alternative.ui.launchers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.alternative.ui.dialog.SequentialOpenDialog;
import bin.file.opener.alternative.ui.tasks.TaskOpen;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.models.FileData;
import bin.file.opener.alternative.utils.SysHelper;


public class LauncherOpen {
  private final MainActivity mActivity;
  private final LinearLayout mMainLayout;
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;

  public LauncherOpen(MainActivity activity, LinearLayout mainLayout) {
    mActivity = activity;
    mMainLayout = mainLayout;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    UIHelper.openFilePickerInFileSelectionMode(mActivity, activityResultLauncherOpen, mMainLayout);
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    activityResultLauncherOpen = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              if (takeUriPermissions(mActivity, data.getData(), false)) {
                processFileOpen(new FileData(mActivity, data.getData(), false, 0L, 0L));
              } else
                UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_permission), getFileName(data.getData())));
            } else {
              Log.e(getClass().getSimpleName(), "Null data!!!");
              MyApplication.getInstance().setSequential(false);
            }
          } else
            MyApplication.getInstance().setSequential(false);
        });
  }

  /**
   * Process the opening of the file
   */
  private void processFileOpen(final FileData fd) {
    processFileOpen(fd, true);
  }


  /**
   * Process the opening of the file
   *
   * @param fd FileData.
   */
  public void processFileOpen(final FileData fd, final boolean addRecent) {
    if (fd != null && fd.getUri() != null && fd.getUri().getPath() != null) {
      final FileData previous = mActivity.getFileData();
      mActivity.setFileData(fd);
      Runnable r = () -> {
        mActivity.getUnDoRedo().clear();
        new TaskOpen(mActivity, mActivity.getPayloadHex().getAdapter(), mActivity, addRecent).execute(mActivity.getFileData());
      };
      if (MyApplication.getInstance().isSequential())
        mActivity.getSequentialOpenDialog().show(mActivity.getFileData(), new SequentialOpenDialog.SequentialOpenListener() {
          @Override
          public void onSequentialOpen() {
            r.run();
          }

          @Override
          public void onSequentialCancel() {
            MyApplication.getInstance().setSequential(false);
            if (previous == null) {
              mActivity.onOpenResult(false, false);
            } else {
              mActivity.setFileData(previous);
              mActivity.setTitle(mActivity.getResources().getConfiguration());
            }
          }
        });
      else
        r.run();
    } else {
      UIHelper.toast(mActivity, mActivity.getString(R.string.error_filename));
    }
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
