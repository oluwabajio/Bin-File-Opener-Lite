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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import java.util.List;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.models.FileData;
import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.alternative.ui.dialog.SaveDialog;
import bin.file.opener.alternative.ui.tasks.TaskSave;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.utils.SysHelper;


public class LauncherSave {
  private final MainActivity mActivity;
  private ActivityResultLauncher<Intent> activityResultLauncherSave;
  private final SaveDialog mSaveDialog;

  public LauncherSave(MainActivity activity) {
    mActivity = activity;
    mSaveDialog = new SaveDialog(activity,
        activity.getString(R.string.action_save_title));
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    UIHelper.openFilePickerInDirectorSelectionMode(activityResultLauncherSave);
  }

  /**
   * Registers result launcher for the activity for saving a file.
   */
  private void register() {
    activityResultLauncherSave = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              if (!mActivity.getFileData().isOpenFromAppIntent())
                takeUriPermissions(mActivity, data.getData(), true);
              processFileSaveWithDialog(data.getData());
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }


  /**
   * Process the saving of the file
   *
   * @param uri Uri data.
   */
  private void processFileSaveWithDialog(final Uri uri) {
    mActivity.setOrphanDialog(mSaveDialog.show(mActivity.getFileData().getName(), (dialog, content, layout) -> {
      mActivity.setOrphanDialog(null);
      final String s_file = content.getText().toString();
      if (s_file.trim().isEmpty()) {
        layout.setError(mActivity.getString(R.string.error_filename));
        return;
      }
      processFileSave(uri, s_file);
      dialog.dismiss();
    }));
  }


  /**
   * Process the saving of the file
   *
   * @param uri      Uri data.
   * @param filename The filename
   */
  private void processFileSave(final Uri uri, final String filename) {
    DocumentFile sourceDir = DocumentFile.fromTreeUri(mActivity, uri);
    if (sourceDir == null) {
      UIHelper.toast(mActivity, mActivity.getString(R.string.uri_exception));
      Log.e(getClass().getSimpleName(), "1 - Uri exception: '" + uri + "'");
      return;
    }

    DocumentFile file = null;
    for (DocumentFile f : sourceDir.listFiles()) {
      if (f.getName() != null && f.getName().endsWith(filename)) {
        file = f;
        break;
      }
    }

    if (file != null) {
      UIHelper.showConfirmDialog(mActivity, mActivity.getString(R.string.action_save_title),
          mActivity.getString(R.string.confirm_overwrite),
          (view) -> {
            new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(mActivity.getFileData(),
                mActivity.getPayloadHex().getAdapter().getEntries().getItems(), null));
            mActivity.setTitle(mActivity.getResources().getConfiguration());
          });
    } else {
      DocumentFile d_file = sourceDir.createFile("application/octet-stream", filename);
      if (d_file == null) {
        UIHelper.toast(mActivity, mActivity.getString(R.string.uri_exception));
        Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uri + "'");
      } else {
        FileData fd = new FileData(mActivity, d_file.getUri(), false);
        mActivity.setFileData(fd);
        new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(mActivity.getFileData(),
            mActivity.getPayloadHex().getAdapter().getEntries().getItems(), null));
        mActivity.setTitle(mActivity.getResources().getConfiguration());
      }
    }
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
