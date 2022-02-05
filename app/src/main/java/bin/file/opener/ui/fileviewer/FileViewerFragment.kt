package bin.file.opener.ui.fileviewer

import android.app.ProgressDialog
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import bin.file.opener.BottomNavigationDrawerFragment
import bin.file.opener.R
import com.startapp.sdk.adsbase.StartAppAd
import kotlinx.android.synthetic.main.file_viewer_fragment.*
import kotlin.math.roundToInt


class FileViewerFragment : Fragment() {


    companion object {
        fun newInstance(path: String?): FileViewerFragment {
            val frag = FileViewerFragment()
            val args = Bundle()
            args.putString("path", path)
            frag.arguments = args
            return frag
        }
    }

    private val mViewModel: FileViewerViewModel by lazy {
        ViewModelProvider(this).get(FileViewerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        mViewModel.mFilePath = arguments?.getString("path", "") ?: ""
          }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        StartAppAd.showAd(activity)
        return inflater.inflate(R.layout.file_viewer_fragment, container, false)
    }

    @ExperimentalUnsignedTypes
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupView()
        observeViewModel()

        mViewModel.start(requireActivity())
    }

    @ExperimentalUnsignedTypes
    override fun onResume() {
        super.onResume()
 //       mViewModel.refreshDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_menu, menu)
    }

    @ExperimentalUnsignedTypes
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(requireActivity().supportFragmentManager, bottomNavDrawerFragment.tag)
                true
            }
            R.id.app_bar_refresh -> {
                mViewModel.refreshFileRead()
                true
            }
            R.id.app_bar_goto -> {
                showGotoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView() {
        tv_bytes.setOnClickListener {

        }
    }

    private fun observeViewModel() {
        mViewModel.mTitleString.observe(viewLifecycleOwner) {
            tv_title.text = it
        }

        mViewModel.mPosString.observe(viewLifecycleOwner) {
            tv_position.text = it
        }

        mViewModel.mBytesStyledString.observe(viewLifecycleOwner) {
            tv_bytes.text = it
            Log.e("TAG", "observeViewModel: mbyte called "+ it.substring(0, 12), )
        }

        mViewModel.mAsciiStyledString.observe(viewLifecycleOwner) {
            tv_ascii.text = it
        }
    }

    private fun showGotoDialog() {
        val etInput = EditText(activity)
        etInput.inputType = InputType.TYPE_CLASS_NUMBER
        etInput.hint = "0"

        val container = FrameLayout(requireContext())
        etInput.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = 24.dp
            rightMargin = 24.dp
        }
        container.addView(etInput)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Go to")
            .setView(container)
            .setPositiveButton("Go") { _, _ ->
                val lineNum = try {
                    Integer.parseInt(etInput.text.toString()) / mViewModel.mBytesPerLine
                } catch (e: NumberFormatException) {
                    0
                }
                scrollToLine(lineNum)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun scrollToLine(lineNum: Int) {
        file_viewer_scroller.post {
            var actualLineNum = lineNum
            if (actualLineNum > tv_bytes.lineCount)
                actualLineNum = tv_bytes.lineCount
            val y = tv_bytes.layout.getLineTop(actualLineNum) + tv_title.height
            file_viewer_scroller.scrollTo(0, y)
        }
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()