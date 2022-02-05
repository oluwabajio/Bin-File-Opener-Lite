package bin.file.opener

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import bin.file.opener.ui.fileviewer.FileViewerFragment
import bin.file.opener.ui.select_file.SelectFileFragment
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val PERMISSIONS_CODE = 1001
    }

    val APP_KEY = "0735ee422c20c34818eb8d9ef96eefe3e8028bd4334ece68"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(bottom_app_bar)



        initAds()
        initStartAppAds()

        if (savedInstanceState == null) {


                start()


            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SelectFileFragment.newInstance())
                .commitNow()
        }
    }

    private fun initStartAppAds() {

    //    StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
        StartAppSDK.init(this, BuildConfig.APPLICATION_ID, true)

    }

    private fun initAds() {
         Appodeal.setTesting(BuildConfig.DEBUG)

        Appodeal.setBannerViewId(R.id.appodealBannerView)
        Appodeal.initialize(this, APP_KEY, Appodeal.BANNER, true);
        Appodeal.setBannerCallbacks(setBannerCallBacks())
        Appodeal.show(this, Appodeal.BANNER_VIEW);

//        MobileAds.initialize(this) {}
//        val mAdView: AdView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
    }

    private fun setBannerCallBacks(): BannerCallbacks? {
        return object : BannerCallbacks {
            override fun onBannerLoaded(height: Int, isPrecache: Boolean) {

            }

            override fun onBannerFailedToLoad() {

            }

            override fun onBannerShown() {

            }

            override fun onBannerShowFailed() {

            }

            override fun onBannerClicked() {

            }

            override fun onBannerExpired() {

            }
        }

    }

    private fun start() {

        Log.e("TAG", "start: path = " + intent?.data?.path)

        if (intent?.data == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SelectFileFragment.newInstance())
                .commitNow()
        } else {
            if (hasPermissions()) {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.container,
                        FileViewerFragment.newInstance(intent?.data?.toString())
                    )
                    .commitNow()
            }else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SelectFileFragment.newInstance())
                    .commitNow()
            }
        }

    }

    private fun hasPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_CODE) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        start()
                    } else {

                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, FileViewerFragment.newInstance(data?.data?.toString()))
            .commitNow()
    }

    override fun onResume() {
        super.onResume()
        Appodeal.show(this, Appodeal.BANNER_VIEW)
    }

    override fun onBackPressed() {
        StartAppAd.onBackPressed(this@MainActivity)
        super.onBackPressed()
    }
}
