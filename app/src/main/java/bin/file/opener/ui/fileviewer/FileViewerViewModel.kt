package bin.file.opener.ui.fileviewer

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import bin.file.opener.R
import java.io.File
import java.lang.NumberFormatException
import kotlin.math.log
import kotlin.text.Typography.nbsp
import android.os.AsyncTask
import java.io.ByteArrayOutputStream
import java.util.*
import android.app.ProgressDialog


class FileViewerViewModel : ViewModel() {

    enum class DisplayMode {
        DECIMAL,
        HEX
    }

    private var mPrefs: SharedPreferences? = null
    private var mAppContext: Activity? = null

    private var mIgnoreTextColor = Color.LTGRAY

    private var mDisplayMode = DisplayMode.DECIMAL
    private var mCellSize = 3

    var mBytesPerLine = 8
        private set

    private var mFileBytes = ByteArray(0)

    var mFilePath = ""

    private val _mTitleString = MutableLiveData<String>()
    val mTitleString: LiveData<String>
        get() = _mTitleString

    private val _mBytesStyledString = MutableLiveData<SpannableStringBuilder>()
    val mBytesStyledString: LiveData<SpannableStringBuilder>
        get() = _mBytesStyledString

    private val _mPosString = MutableLiveData<String>()
    val mPosString: LiveData<String>
        get() = _mPosString

    private val _mAsciiStyledString = MutableLiveData<SpannableStringBuilder>()
    val mAsciiStyledString: LiveData<SpannableStringBuilder>
        get() = _mAsciiStyledString


    @ExperimentalUnsignedTypes
    fun start(appContext: Activity) {

        Thread(Runnable {
            mAppContext = appContext
            if (mFilePath.isNotEmpty())
                refreshFileRead()
        }).start()


    }

    private fun setupDisplay() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mAppContext!!)
        val dimIgnore = mPrefs?.getBoolean("dim_zeroes", true) ?: true
        val prefsDisplayMode = mPrefs?.getString("binary_format", "dec")
        val prefsBytesPerLine = mPrefs?.getString("bytes_per_line", "8") ?: "8"

        mIgnoreTextColor =
            if (dimIgnore) mAppContext!!.getColor(R.color.ignoreTextColor) else mAppContext!!.getColor(
                R.color.primaryTextColor
            )

        mDisplayMode = when (prefsDisplayMode) {
            "dec" -> DisplayMode.DECIMAL
            "hex" -> DisplayMode.HEX
            else -> DisplayMode.DECIMAL
        }

        mBytesPerLine = try {
            Integer.parseInt(prefsBytesPerLine)
        } catch (e: NumberFormatException) {
            8
        }

        mCellSize = when (mDisplayMode) {
            DisplayMode.DECIMAL -> 3
            DisplayMode.HEX -> 2
        }
    }

    @ExperimentalUnsignedTypes
    fun refreshFileRead() {
        mAppContext?.runOnUiThread {
            _mTitleString.value = mFilePath.substring(mFilePath.lastIndexOf(File.separator) + 1)
            Log.e("TAG", "refreshDisplay: after title")
        }

        readFile()
        Log.e("TAG", "refreshDisplay: before refreshDisplay")
        refreshDisplay()

    }

    @ExperimentalUnsignedTypes
    fun refreshDisplay() {
        setupDisplay()

        val bytesString = byteArrayToString(mFileBytes)


    }

    private fun readFile() {

        var inputStream = mAppContext?.contentResolver?.openInputStream(Uri.parse(mFilePath))
        mFileBytes = inputStream!!.readBytes()
    }

    @ExperimentalUnsignedTypes
    private fun byteArrayToString(arr: ByteArray): SpannableStringBuilder {


        val sb = SpannableStringBuilder()
        var sByte: String

        val lengthtoShow = if (arr.size > 5000) 5000 else arr.size


        Log.e("TAG", "refreshDisplay: array size = ${arr.size}")


        val sb2 = StringBuilder()
        val numRows = lengthtoShow / mBytesPerLine

        val spanSb = SpannableStringBuilder()



        arr.forEachIndexed { index, byte ->

            var byte = arr[index]
            sByte = when (mDisplayMode) {
                DisplayMode.DECIMAL -> byte.toUByte().toString()
                DisplayMode.HEX -> String.format("%02X", byte)
            }

            while (sByte.length < mCellSize)
                sByte = "0$sByte"
            sb.append(sByte)



            if (byte.compareTo(0) == 0)
                sb.setSpan(
                    ForegroundColorSpan(mIgnoreTextColor),
                    sb.length - sByte.length,
                    sb.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

            if (index != 0 && (index + 1) % mBytesPerLine == 0)
                sb.append(System.lineSeparator())
            else
                sb.append(nbsp)










            sb2.append(index * mBytesPerLine)
            if (index < numRows) sb2.append(System.lineSeparator())
            if (index == numRows) {
                mAppContext?.runOnUiThread {
                    _mPosString.value = sb2.toString()
                }
            }







            if (byte in 32..126)
                spanSb.append(byte.toChar())
            else {
                spanSb.append("·")
                spanSb.setSpan(
                    ForegroundColorSpan(mIgnoreTextColor),
                    spanSb.length - 1,
                    spanSb.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            if (index != 0 && (index + 1) % mBytesPerLine == 0)
                spanSb.append(System.lineSeparator())



            if (index == 10) Log.e("TAG", "byteArrayToString: sb at 10 $sb")
            if (index == lengthtoShow) {
                mAppContext?.runOnUiThread {
                    _mBytesStyledString.value = sb
                    _mAsciiStyledString.value = spanSb
                    Log.e("TAG", "refreshDisplay: inside viewModelScopeMain1 i dey "+   sb.substring(0, 10))
                }

                return@forEachIndexed
            }

        }

        return sb
    }
}
