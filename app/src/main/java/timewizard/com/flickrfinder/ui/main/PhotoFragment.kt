package timewizard.com.flickrfinder.ui.main

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import timewizard.com.flickrfinder.BuildConfig
import timewizard.com.flickrfinder.R


class PhotoFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoFragment()
    }

    private lateinit var mViewModel: PhotoViewModel

    private lateinit var mSearchMenuItem: MenuItem
    private lateinit var mSearchButtonView: SearchView

    private lateinit var mPhotoView: PhotoView
    private lateinit var mLoading: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPhotoView = view.findViewById(R.id.photo_image)
        mLoading = view.findViewById(R.id.loading)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        preparePhotoView()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        prepareSearchView(menu)
        menu.findItem(R.id.menu_sort).setVisible(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> returnToListView()
        }
        return super.onOptionsItemSelected(item)
    }

    // Create the viewmodel and load the photo into the photoview
    private fun preparePhotoView() {
        mViewModel = ViewModelProviders.of(requireActivity()).get(PhotoViewModel::class.java)
        val photo = mViewModel.getSelectedPhoto()
        if (photo != null) {
            mLoading.visibility = View.VISIBLE
            Picasso.Builder(activity!!.baseContext)
                .downloader(OkHttp3Downloader(activity!!.baseContext))
                .build()
                .load(photo.getBestPhoto())
                .error(R.drawable.ic_broken_image_white)
                .into(mPhotoView, object: Callback {
                    override fun onSuccess() {
                        Log.d(BuildConfig.TAG_FLICKR_FINDER, "Loaded image successfully: " + photo.getBestPhoto())
                        mLoading.visibility = View.INVISIBLE
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(BuildConfig.TAG_FLICKR_FINDER, e?.message)
                        mViewModel.mErrorMessage.value = "Could not load photo: " + photo.photoId
                        mLoading.visibility = View.INVISIBLE
                    }
                })
        }
    }

    // set listeners for the search button in the actionbar
    private fun prepareSearchView(menu: Menu) {
        mSearchMenuItem = menu.findItem(R.id.menu_search)
        mSearchButtonView = mSearchMenuItem.actionView as SearchView
        mSearchButtonView.queryHint = getString(timewizard.com.flickrfinder.R.string.menu_search)
        mSearchButtonView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                mViewModel.searchPhotos(query)
                mSearchButtonView.clearFocus()
                mSearchMenuItem.collapseActionView()
                returnToListView()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    // update the viewmodel and tell the activity to pop this fragment off the backstack
    private fun returnToListView() {
        mViewModel.clearSelectedPhoto()
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "Returning to List View")
        Log.d(BuildConfig.TAG_FLICKR_FINDER, mViewModel.getSelectedPhoto().toString())
        activity?.onBackPressed()
    }
}