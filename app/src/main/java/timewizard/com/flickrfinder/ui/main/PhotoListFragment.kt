package timewizard.com.flickrfinder.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timewizard.com.flickrfinder.BuildConfig
import timewizard.com.flickrfinder.R

class PhotoListFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoListFragment()
    }

    private var mCurrentPage = 1

    private lateinit var mViewModel: PhotoViewModel

    private lateinit var mSearchMenuItem: MenuItem
    private lateinit var mSearchButton: SearchView
    private lateinit var mPageLeftButton: ImageButton
    private lateinit var mPageRightButton: ImageButton


    private lateinit var mSearchTextView: TextView
    private lateinit var mCountText: TextView
    private lateinit var mListView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "onCreateView")
        return inflater.inflate(R.layout.fragment_photo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "onViewCreated")
        mSearchTextView = view.findViewById(R.id.search_text)
        mCountText = view.findViewById(R.id.count_text)
        mPageLeftButton = view.findViewById(R.id.button_page_left)
        mPageRightButton = view.findViewById(R.id.button_page_right)
        mListView = view.findViewById(R.id.list_view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "onActivityCreated")
        setHasOptionsMenu(true)
        preparePhotoList()
        preparePageButtons()
    }

    override fun onResume() {
        super.onResume()
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "onResume")
        mListView.adapter?.notifyDataSetChanged()
        updateDisplay()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "onPrepareOptionsMenu")
        prepareSearchView(menu)
        menu.findItem(R.id.menu_sort).setVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_relevant-> mViewModel.sortPhotos(PhotoViewModel.Sort.RELEVANT)
            R.id.sort_interesting -> mViewModel.sortPhotos(PhotoViewModel.Sort.INTERESTING)
            R.id.sort_recent -> mViewModel.sortPhotos(PhotoViewModel.Sort.RECENT)
        }
        updateDisplay()
        return super.onOptionsItemSelected(item)
    }

    // set up the recyclerview and listen for updates to the viewmodel
    private fun preparePhotoList() {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "preparingPhotoList")
        mViewModel = ViewModelProviders.of(requireActivity()).get(PhotoViewModel::class.java)
        mListView.layoutManager = LinearLayoutManager(activity)
        mListView.adapter = PhotoListAdapter(activity as Context, mViewModel)
        mViewModel.mPhotoList.observe(this, Observer {
            mListView.adapter?.notifyDataSetChanged()
            mCurrentPage = mViewModel.mCurrentLoadedPhotoPage.value!!
            updateDisplay()
        })
    }

    // add click listeners to the paging buttons
    private fun preparePageButtons() {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "preparingPageButtons")
        mPageLeftButton.setOnClickListener(buttonClickListener)
        mPageRightButton.setOnClickListener(buttonClickListener)
    }

    // set listeners for the search button in the actionbar
    private fun prepareSearchView(menu: Menu) {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "preparingSearchView")
        mSearchMenuItem = menu.findItem(R.id.menu_search)
        mSearchButton = mSearchMenuItem.actionView as SearchView
        mSearchButton.queryHint = getString(R.string.menu_search)
        mSearchButton.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                mViewModel.searchPhotos(query)
                mSearchButton.clearFocus()
                mSearchMenuItem.collapseActionView()
                updateDisplay()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    // update the textviews and buttons based on data from the viewmodel
    private fun updateDisplay() {
        val query = mViewModel.mQuery.value
        var sort = ""
        when (mViewModel.mSortType.value) {
            PhotoViewModel.Sort.RELEVANT -> sort = getString(R.string.sort_relevant)
            PhotoViewModel.Sort.INTERESTING -> sort = getString(R.string.sort_interesting)
            PhotoViewModel.Sort.RECENT-> sort = getString(R.string.sort_recent)
        }
        if (mViewModel.mQuery.value.isNullOrBlank())
            mSearchTextView.text = getString(R.string.search_default_text, sort)
        else
            mSearchTextView.text = getString(R.string.search_query_text, sort, query)

        mPageLeftButton.isEnabled = mCurrentPage > 1
        mPageRightButton.isEnabled = mCurrentPage < mViewModel.mTotalPhotoPages.value!!

        mCountText.text = getString(R.string.search_results_count,
            mViewModel.mLoadedPhotosStart,
            mViewModel.mLoadedPhotosEnd,
            mViewModel.mTotalPhotos.value)
    }

    private val buttonClickListener: View.OnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.button_page_left -> mViewModel.loadPage(mCurrentPage - 1)
                R.id.button_page_right -> mViewModel.loadPage(mCurrentPage + 1)
            }
            updateDisplay()
        }
    }
}