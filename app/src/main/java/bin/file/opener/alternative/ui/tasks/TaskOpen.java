package bin.file.opener.alternative.ui.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.ui.adapters.HexTextArrayAdapter;
import bin.file.opener.alternative.ui.utils.UIHelper;
import bin.file.opener.R;
import bin.file.opener.alternative.models.FileData;
import bin.file.opener.alternative.models.LineEntry;
import bin.file.opener.alternative.utils.SysHelper;


public class TaskOpen extends ProgressTask<ContentResolver, FileData, TaskOpen.Result> {
  private static final String TAG = TaskOpen.class.getSimpleName();
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW_16 * 20000;
  private final HexTextArrayAdapter mAdapter;
  private final OpenResultListener mListener;
  private InputStream mInputStream = null;
  private final boolean mAddRecent;
  private final ContentResolver mContentResolver;
  private final Context mContext;

  public static class Result {
    private List<LineEntry> listHex = null;
    private String exception = null;
    private long startOffset = 0;
  }

  public interface OpenResultListener {
    void onOpenResult(boolean success, boolean fromOpen);
  }

  public TaskOpen(final Activity activity,
                  final HexTextArrayAdapter adapter,
                  final OpenResultListener listener, final boolean addRecent) {
    super(activity, true);
    mContext = activity;
    mContentResolver = activity.getContentResolver();
    mAdapter = adapter;
    mListener = listener;
    mAddRecent = addRecent;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public ContentResolver onPreExecute() {
    super.onPreExecute();
    mAdapter.clear();
    return mContentResolver;
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final Result result) {
    super.onPostExecute(result);
    if (isCancelled())
      UIHelper.toast(mContext, mContext.getString(R.string.operation_canceled));
    else if (result.exception != null)
      UIHelper.toast(mContext, mContext.getString(R.string.exception) + ": " + result.exception);
    else {
      if (result.listHex != null) {
        mAdapter.setStartOffset(result.startOffset);
        mAdapter.addAll(result.listHex);
      }
    }
    if (mListener != null)
      mListener.onOpenResult(result.exception == null && !isCancelled(), true);
    super.onPostExecute(result);
  }

  /**
   * Closes the stream.
   */
  private void close() {
    if (mInputStream != null) {
      try {
        mInputStream.close();
      } catch (final IOException e) {
        Log.e(TAG, "Exception: " + e.getMessage(), e);
      }
      mInputStream = null;
    }
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    close();
    if (mListener != null)
      mListener.onOpenResult(false, true);
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param contentResolver ContentResolver.
   * @param fd              FileData.
   * @return The result.
   */
  @Override
  public Result doInBackground(ContentResolver contentResolver, FileData fd) {
    //final Activity activity = mActivityRef.get();
    final Result result = new Result();
    final List<LineEntry> list = new ArrayList<>();
    try {
      result.startOffset = fd.getStartOffset();
      final MyApplication app = MyApplication.getInstance();
      /* Size + stream */
      mTotalSize = fd.getSize();
      publishProgress(0L);
      mInputStream = contentResolver.openInputStream(fd.getUri());

      if (mInputStream != null) {
        int maxLength = moveCursorIfSequential(fd, result);

        if (result.exception == null) {
          /* prepare buffer */
          final byte[] data = new byte[maxLength];
          int reads;
          long totalSequential = fd.getStartOffset();
          evaluateShiftOffset(fd, totalSequential);
          boolean first = true;
          /* read data */
          while (!isCancelled() && (reads = mInputStream.read(data)) != -1) {
            try {
              SysHelper.formatBuffer(list, data, reads, mCancel,
                  MyApplication.getInstance().getNbBytesPerLine(), first ? fd.getShiftOffset() : 0);
              first = false;
            } catch (IllegalArgumentException iae) {
              result.exception = iae.getMessage();
              break;
            }
            publishProgress((long) reads);
            if (fd.isSequential()) {
              totalSequential += reads;
              if (totalSequential >= fd.getEndOffset())
                break;
            }
          }
          /* prepare result */
          if (result.exception == null) {
            result.listHex = list;
            if (mAddRecent && !mCancel.get())
              app.getRecentlyOpened().add(fd);
          }
        }
      }
    } catch (final Exception e) {
      result.exception = e.getMessage();
    } finally {
      close();
    }
    return result;
  }

  private int moveCursorIfSequential(FileData fd, Result result) throws IOException {
    int maxLength = MAX_LENGTH;
    if (fd.isSequential()) {
      if (mInputStream.skip(fd.getStartOffset()) != fd.getStartOffset()) {
        result.exception = "Unable to skip file data!";
      }
      maxLength = fd.getSize() < MAX_LENGTH ? (int) fd.getSize() : MAX_LENGTH;
    }
    return maxLength;
  }

  private void evaluateShiftOffset(FileData fd, long totalSequential) {
    if(totalSequential != 0) {
      final int nbBytesPerLine = MyApplication.getInstance().getNbBytesPerLine();
      final long count = totalSequential / nbBytesPerLine;
      final long remain = totalSequential - (count * nbBytesPerLine);
      fd.setShiftOffset((int)remain);
    }
  }
}
