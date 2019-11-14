package timewizard.com.flickrfinder.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import timewizard.com.flickrfinder.BuildConfig
import timewizard.com.flickrfinder.R
import timewizard.com.flickrfinder.network.RestClient
import timewizard.com.flickrfinder.ui.main.PhotoFragment
import timewizard.com.flickrfinder.ui.main.PhotoListFragment
import timewizard.com.flickrfinder.ui.main.PhotoViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var mViewModel: PhotoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PhotoListFragment.newInstance())
                .commitNow()
        }
        prepareViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.menu_photo_list, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        invalidateOptionsMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStop() {
        super.onStop()
        RestClient.instance(this).requestQueue.cancelAll(BuildConfig.TAG_FLICKR_FINDER)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0){
            mViewModel.clearSelectedPhoto()
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    private fun viewPhoto() {
        supportFragmentManager.beginTransaction()
            .addToBackStack("back")
            .replace(R.id.container, PhotoFragment.newInstance())
            .commit()
    }

    private fun prepareViewModel() {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "preparingViewModel")
        mViewModel = ViewModelProviders.of(this).get(PhotoViewModel::class.java)
        mViewModel.mSelectedPhoto.observe(this, Observer {
            if (mViewModel.getSelectedPhoto() != null) {
                Log.d(BuildConfig.TAG_FLICKR_FINDER, "Replacing Fragment to View Photo")
                Log.d(BuildConfig.TAG_FLICKR_FINDER, mViewModel.getSelectedPhoto().toString())
                viewPhoto()
            }
        })
        mViewModel.mErrorMessage.observe(this, Observer {
            Toast.makeText(this, mViewModel.mErrorMessage.value, Toast.LENGTH_SHORT)
        })
        mViewModel.loadPage(1)
    }
}
