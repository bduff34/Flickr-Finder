package timewizard.com.flickrfinder.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import timewizard.com.flickrfinder.BuildConfig;
import timewizard.com.flickrfinder.R;
import timewizard.com.flickrfinder.network.RestClient;
import timewizard.com.flickrfinder.ui.main.PhotoFragment;
import timewizard.com.flickrfinder.ui.main.PhotoListFragment;
import timewizard.com.flickrfinder.ui.main.PhotoViewModel;

public class MainActivity extends AppCompatActivity {

    private PhotoViewModel mViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PhotoListFragment.Companion.newInstance())
                    .commitNow();
        }
        prepareViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_photo_list, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        invalidateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStop() {
        super.onStop();
        RestClient.Companion.instance(this).getRequestQueue().cancelAll(BuildConfig.TAG_FLICKR_FINDER);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0){
            mViewModel.clearSelectedPhoto();
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }

    private void viewPhoto() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("back")
                .replace(R.id.container, PhotoFragment.Companion.newInstance())
                .commit();
    }

    private void prepareViewModel() {
        Log.d(BuildConfig.TAG_FLICKR_FINDER, "preparingViewModel");
        mViewModel = ViewModelProviders.of(this).get(PhotoViewModel.class);
        mViewModel.getMSelectedPhoto().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer i) {
                if (mViewModel.getSelectedPhoto() != null) {
                    Log.d(BuildConfig.TAG_FLICKR_FINDER, "Replacing Fragment to View Photo");
                    Log.d(BuildConfig.TAG_FLICKR_FINDER, mViewModel.getSelectedPhoto().toString());
                    viewPhoto();
                }
            }
        });
        mViewModel.getMErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String i) {
                Toast.makeText(MainActivity.this, mViewModel.getMErrorMessage().getValue(), Toast.LENGTH_SHORT);
            }
        });
        mViewModel.loadPage(1);
    }
}
