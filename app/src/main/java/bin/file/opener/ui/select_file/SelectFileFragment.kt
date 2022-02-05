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
import android.widget.TextView
import android.widget.Toast

import com.google.android.material.snackbar.Snackbar

import bin.file.opener.MainActivity

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

        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
              
                startActivityForResult(intent, PICK_FILE)

            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Permission Required")
                    .setMessage("Without Read Storage Permission, This app is unable to work. Kindly accept permission.") // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { dialog, which ->
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PICK_FILE
                            )
                        })   .setNegativeButton(android.R.string.no, { dialog, which -> dialog.dismiss() })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PICK_FILE
                )
            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Showing the toast message
            Toast.makeText(requireActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        else {
       //     Toast.makeText(requireActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                // not permanently denied
            }
            else {
                // permanently denied
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Need Permissions")
                    .setMessage(
                        "This application need to use some permissions, " +
                                "you can grant them in the application settings."
                    )
                    .setPositiveButton(
                        "GOTO SETTINGS"
                    ) { dialogInterface, i ->
                        dialogInterface.cancel()
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", requireActivity().getPackageName(), null)
                        intent.data = uri
                        startActivityForResult(intent, 133)
                    }
                    .setNegativeButton(
                        "CANCEL"
                    ) { dialogInterface, i -> dialogInterface.cancel() }
                dialog.show()
            }








        }
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
