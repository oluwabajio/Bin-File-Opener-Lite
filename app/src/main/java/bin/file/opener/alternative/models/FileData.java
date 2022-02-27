package bin.file.opener.alternative.models;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.IOException;

import bin.file.opener.alternative.MyApplication;
import bin.file.opener.alternative.utils.SysHelper;


public class FileData {
  protected static final String SEQUENTIAL_SEP = "^";
  private final String mName;
  private final Uri mUri;
  private boolean mOpenFromAppIntent;
  private long mStartOffset;
  private long mEndOffset;
  private long mSize;
  private long mRealSize;
  private boolean mIsNotFound;
  private final boolean mIsAccessError;
  private int mShiftOffset;

  public FileData(final Context ctx, final Uri uri, boolean openFromAppIntent) {
    this(ctx, uri, openFromAppIntent, 0L, 0L);
  }

  public FileData(final Context ctx, final Uri uri, boolean openFromAppIntent, long startOffset, long endOffset) {
    mShiftOffset = 0;
    mUri = uri;
    mName = getFileName(uri);
    mStartOffset = startOffset;
    mEndOffset = endOffset;
    mOpenFromAppIntent = openFromAppIntent;
    if (isSequential())
      mSize = Math.abs(mEndOffset - mStartOffset);
    else
      mSize = getFileSize(ctx.getContentResolver(), uri);
    mRealSize = getFileSize(ctx.getContentResolver(), uri);

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
      DocumentFile sourceFile = DocumentFile.fromSingleUri(ctx, mUri);
      mIsNotFound = (sourceFile == null || !sourceFile.exists());
    }
    if (mIsNotFound) {
      mIsAccessError = false;
      mRealSize = 0;
      mSize = 0;
    } else {
      if (mRealSize == -1) {
        mIsAccessError = false;
        mIsNotFound = true;
        mRealSize = 0;
        mSize = 0;
      } else if (mRealSize == -2) {
        mIsAccessError = true;
        mIsNotFound = false;
        mRealSize = 0;
        mSize = 0;
      } else {
        mIsAccessError = false;
        mIsNotFound = false;
      }
    }
  }

  /**
   * Sets the offset used to shift the text to the end of the line.
   *
   * @param shiftOffset The new value.
   */
  public void setShiftOffset(int shiftOffset) {
    mShiftOffset = shiftOffset;
  }

  /**
   * Returns the offset used to shift the text to the end of the line.
   *
   * @return int
   */
  public int getShiftOffset() {
    return mShiftOffset;
  }

  /**
   * Returns true if a file access error was raised.
   *
   * @return boolean
   */
  public boolean isAccessError() {
    return mIsAccessError;
  }

  /**
   * Returns true if the file was not found.
   *
   * @return boolean
   */
  public boolean isNotFound() {
    return mIsNotFound;
  }

  /**
   * Tests if the file is opened from the application's intent.
   *
   * @return boolean
   */
  public boolean isOpenFromAppIntent() {
    return mOpenFromAppIntent;
  }

  /**
   * Clears the open from app intent flag.
   */
  public void clearOpenFromAppIntent() {
    mOpenFromAppIntent = false;
  }

  /**
   * Tests if the name is empty.
   *
   * @param fd FileData
   * @return boolean
   */
  public static boolean isEmpty(FileData fd) {
    return fd == null || fd.mName == null || fd.mName.isEmpty();
  }

  /**
   * Returns the file name.
   *
   * @return String
   */
  public String getName() {
    return mName;
  }

  @NonNull
  public String toString() {
    String ret = mStartOffset + SEQUENTIAL_SEP + mEndOffset + SEQUENTIAL_SEP;
    ret += mUri.toString();
    return ret;
  }

  /**
   * Test if it's a sequential file or not.
   *
   * @return boolean
   */
  public boolean isSequential() {
    return mStartOffset != 0L || mEndOffset != 0L;
  }

  /**
   * Returns the file uri.
   *
   * @return Uri
   */
  public Uri getUri() {
    return mUri;
  }

  /**
   * Returns the start offset.
   *
   * @return long
   */
  public long getStartOffset() {
    return mStartOffset;
  }

  /**
   * Returns the end offset.
   *
   * @return long
   */
  public long getEndOffset() {
    return mEndOffset;
  }

  /**
   * Sets the start offset.
   *
   * @param startOffset Start offset
   * @param endOffset   End offset
   */
  public void setOffsets(long startOffset, long endOffset) {
    mStartOffset = startOffset;
    mEndOffset = endOffset;
    mSize = Math.abs(mEndOffset - mStartOffset);
  }

  /**
   * Returns the file size.
   *
   * @return long
   */
  public long getSize() {
    return mSize;
  }

  /**
   * Returns the real file size.
   *
   * @return long
   */
  public long getRealSize() {
    return mRealSize;
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

  public  long getFileSize(ContentResolver cr, Uri uri) {
    ParcelFileDescriptor pfd = null;
    long size = 0L;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      if (pfd != null) {
        size = pfd.getStatSize();
        pfd.close();
      }
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage()/*, e*/);
      size = e instanceof FileNotFoundException ? -1 : -2;
    } finally {
      if (pfd != null)
        try {
          pfd.close();
        } catch (IOException e) {
          Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage()/*, e*/);
        }
    }
    return size;
  }

}
