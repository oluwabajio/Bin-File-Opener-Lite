package bin.file.opener.ui.select_file

import android.Manifest
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import bin.file.opener.R
import android.content.DialogInterface
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

import androidx.core.app.ActivityCompat

class SelectFileFragment : Fragment() {


    val PICK_FILE = 455
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_file, container, false)
        initListeners(view)
        return view
    }

    private fun initListeners(view: View) {

        view.findViewById<Button>(R.id.btnSelectFile).setOnClickListener {
            selectFile()
        }
    }

    private fun selectFile() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"

        startActivityForResult(intent, PICK_FILE)

    }




//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    companion object {

        @JvmStatic
        fun newInstance(): Fragment {
            return SelectFileFragment()
        }
    }
}
