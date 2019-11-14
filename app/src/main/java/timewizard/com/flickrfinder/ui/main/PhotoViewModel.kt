package timewizard.com.flickrfinder.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.android.volley.VolleyError
import timewizard.com.flickrfinder.BuildConfig
import timewizard.com.flickrfinder.data.ApiBuilder
import timewizard.com.flickrfinder.data.FlickrPhoto
import timewizard.com.flickrfinder.data.FlickrPhotoResponse
import timewizard.com.flickrfinder.network.RestClient


class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    enum class Sort {
        RELEVANT, INTERESTING, RECENT
    }

    // List of photos currently displayed in the list
    val mPhotoList = MutableLiveData<List<FlickrPhoto>>()
    // Photo chosen to be viewed at higher resolution
    val mSelectedPhoto = MutableLiveData<Int>(-1)

    // Parameters for the photo list
    val mQuery = MutableLiveData<String>()
    val mSortType = MutableLiveData<Sort>(Sort.INTERESTING)

    // Most recent Json response fetched
    val mLastResponse = MutableLiveData<String>()
    // Error message, tied to observer in the activity
    val mErrorMessage = MutableLiveData<String>()

    // current counts for photos and pages
    val mNumLoadedPhotos: Int
        get() {
            if (mPhotoList.value != null)
                return mPhotoList.value!!.size
            return 0
        }
    val mCurrentLoadedPhotoPage = MutableLiveData<Int>(0)

    // beginning and end of the currently loaded photos
    val mLoadedPhotosStart: Long
        get() {
            return (mCurrentLoadedPhotoPage.value!! - 1) * 25L + 1
        }
    val mLoadedPhotosEnd: Long
        get() {
            return mLoadedPhotosStart + mNumLoadedPhotos - 1
        }

    // photo and page totals
    val mTotalPhotos = MutableLiveData<Long>(0)
    val mTotalPhotoPages = MutableLiveData<Int>(1)

    // endpoint used to fetch the photos
    val mPhotosUrlEndpoint: String
        get()  {
            if (mQuery.value.isNullOrBlank()) {
                when (mSortType.value) {
                    Sort.RELEVANT, Sort.INTERESTING ->
                        return ApiBuilder.getPopularPhotosURL(mCurrentLoadedPhotoPage.value)
                    Sort.RECENT->
                        return ApiBuilder.getRecentPhotosURL(mCurrentLoadedPhotoPage.value)
                }
            }
            return ApiBuilder.getPhotoSearchURL(mQuery.value, mSortType.value, mCurrentLoadedPhotoPage.value)
        }

    // access methods to prepare a photo to be viewed in higher resolution
    fun clearSelectedPhoto() {
        mSelectedPhoto.value = -1
    }

    fun selectPhoto(p: Int) {
        if (p >= 0 && p < mNumLoadedPhotos) {
            mSelectedPhoto.value = p
        }
    }

    fun getSelectedPhoto(): FlickrPhoto? {
        val p: Int = mSelectedPhoto.value as Int
        if (p >= 0 && !mPhotoList.value.isNullOrEmpty())
            return mPhotoList.value!![mSelectedPhoto.value as Int]
        return null
    }

    // fetch data for a new search query
    fun searchPhotos(query: String) {
        if (!mQuery.value.equals(query)) {
            mQuery.value = query
            mCurrentLoadedPhotoPage.value = 1
            fetchData()
        }
    }

    // fetch data using new sorting parameters
    fun sortPhotos(sort: Sort) {
        if (mSortType.value != sort) {
            mSortType.value = sort
            mCurrentLoadedPhotoPage.value = 1
            fetchData()
        }
    }

    // load the next page of the current search
    fun loadPage(p: Int) {
        if (p > 0 && mCurrentLoadedPhotoPage.value!! != p && p <= mTotalPhotoPages.value!!) {
            mCurrentLoadedPhotoPage.value = p
            fetchData()
        }
    }

    // generic data fetch
    private fun fetchData() {
        clearSelectedPhoto()
        RestClient.instance(getApplication()).sendGetRequest(mPhotosUrlEndpoint, onDataLoaded, onDataError)
    }

    // callback when a Json response is received
    private val onDataLoaded = object : Response.Listener<String> {
        override fun onResponse(response: String) {
            mLastResponse.value = response
            val photoResponse = RestClient.instance(getApplication()).gson.fromJson(response, FlickrPhotoResponse::class.java)
            if (photoResponse == null || photoResponse.page == null) {
                return
            }
            val photoPage = photoResponse.page
            mTotalPhotos.value = photoPage!!.totalPhotos
            mTotalPhotoPages.value = photoPage.totalPages
            if (mPhotoList.value.isNullOrEmpty() || mCurrentLoadedPhotoPage.value == 1) {
                mPhotoList.value = mutableListOf()
            }
            if (!photoPage.photoList.isNullOrEmpty()) {
                mPhotoList.value = photoPage.photoList
            }
        }
    }

    // error callback after data fetch
    private val onDataError = object : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError) {
            Log.e(BuildConfig.TAG_FLICKR_FINDER, error.message)
            mErrorMessage.value = "Error occurred while loading data"
        }
    }

    override fun toString(): String {
        var s = "query: " + mQuery.value + "\n" +
                "sort: " + mSortType.value.toString() + "\n\n" +
                "page: " + mCurrentLoadedPhotoPage.value + "\n" +
                "totalPages: " + mTotalPhotoPages.value + "\n" +
                "totalPhotos: " + mTotalPhotos.value + "\n" +
                "loadedPhotos: " + mNumLoadedPhotos + "\n\n" +
                "photos: " + mPhotoList.value + "\n\n"
        s += "]\n\nresponse: " + mLastResponse.value
        return s
    }
}
