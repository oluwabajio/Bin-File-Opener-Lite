package bin.file.opener

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import bin.file.opener.R
import com.google.android.material.navigation.NavigationView


class BottomNavigationDrawerFragment: BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navigation_view = requireActivity().findViewById<NavigationView>(R.id.navigation_view)
         navigation_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_menu_choose_file -> {
//                    findNavController().navigate(R.id.action_open_settings)

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"

                    val PICK_PDF_FILE = 455
                    startActivityForResult(intent, PICK_PDF_FILE)
                    dismiss()
                }
                R.id.bottom_menu_settings -> {
                    dismiss()
                    val intent = Intent(activity, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }
}