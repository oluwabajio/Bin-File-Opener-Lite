package bin.file.opener.alternative.ui.launchers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.models.FileData;
import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.alternative.ui.activities.RecentlyOpenActivity;
import bin.file.opener.alternative.ui.tasks.TaskSave;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.utils.SysHelper;


public class LauncherRecentlyOpen {
  private final MainActivity mActivity;
  private final MyApplication mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherRecentlyOpen;

  public LauncherRecentlyOpen(MainActivity activity) {
    mApp = MyApplication.getInstance();
    mActivity = activity;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    RecentlyOpenActivity.startActivity(mActivity, activityResultLauncherRecentlyOpen);
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    activityResultLauncherRecentlyOpen = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            mActivity.setOrphanDialog(null);
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
              Uri uri = data.getData();
              long startOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_START_OFFSET, 0L);
              long endOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_END_OFFSET, 0L);
              FileData fd = new FileData(mActivity, uri, false, startOffset, endOffset);
              if (isFileExists(mActivity.getContentResolver(), uri)) {
                if (hasUriPermission(mActivity, uri, true)) {
                  final Runnable r = () -> mActivity.getLauncherOpen().processFileOpen(fd, true);
                  if (mActivity.getUnDoRedo().isChanged()) {// a save operation is pending?
                    UIHelper.confirmFileChanged(mActivity, mActivity.getFileData(), r, () -> new TaskSave(mActivity, mActivity).execute(
                        new TaskSave.Request(mActivity.getFileData(), mActivity.getPayloadHex().getAdapter().getEntries().getItems(), r)));
                  } else
                    r.run();
                } else {
                  UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_permission), getFileName(uri)));
                  mApp.getRecentlyOpened().remove(fd);
                }
              } else {
                UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_not_found), getFileName(uri)));
                mApp.getRecentlyOpened().remove(fd);
              releaseUriPermissions(mActivity, uri);
              }
            } else if (mApp.getRecentlyOpened().list().isEmpty())
              mActivity.getMenuRecentlyOpen().setEnabled(false);
          }
        });
  }


  public  void releaseUriPermissions(final Context c, final Uri uri) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
      if (hasUriPermission(c, uri, true))
        try {
          final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
          c.getContentResolver().releasePersistableUriPermission(uri, takeFlags);

          Uri dir = getParentUri(uri);
          final List<UriPermission> list = c.getContentResolver().getPersistedUriPermissions();
          int found = 0;
          for (UriPermission up : list) {
            if (up.getUri().equals(dir)) {
              found++;
            }
          }
          if (found == 1) {
            c.getContentResolver().releasePersistableUriPermission(dir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          }
        } catch (Exception e) {
          Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
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

  public static boolean isFileExists(final ContentResolver cr, final Uri uri) {
    ParcelFileDescriptor pfd;
    boolean exists = false;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      if (pfd != null) {
        exists = true;
        pfd.close();
      }
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return exists;
  }

}
