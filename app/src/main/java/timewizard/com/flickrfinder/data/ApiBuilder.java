package timewizard.com.flickrfinder.data;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import timewizard.com.flickrfinder.BuildConfig;
import timewizard.com.flickrfinder.ui.main.PhotoViewModel;

public class ApiBuilder {

    public static String getPhotoSearchURL(String query, PhotoViewModel.Sort sorting, Integer page) {
        return new FlickrUrlBuilder()
                .searchPhotosUrl()
                .addSearchTerm(query)
                .addExtraFields()
                .addSorting(sorting)
                .addPage(page)
                .addJsonFormat()
                .buildUrl();
    }

    public static String getPopularPhotosURL(Integer page) {
        return new FlickrUrlBuilder()
                .popularPhotosUrl()
                .addExtraFields()
                .addPage(page)
                .addJsonFormat()
                .buildUrl();
    }

    public static String getRecentPhotosURL(Integer page) {
        return new FlickrUrlBuilder()
                .recentPhotosUrl()
                .addExtraFields()
                .addPage(page)
                .addJsonFormat()
                .buildUrl();
    }

    public static String getPhotoInfoURL(String photoId) {
        return new FlickrUrlBuilder()
                .photoInfoUrl()
                .addPhotoId(photoId)
                .addJsonFormat()
                .buildUrl();
    }

    public static String getPhotoSizesURL(String photoId) {
        return new FlickrUrlBuilder()
                .photoSizesUrl()
                .addPhotoId(photoId)
                .addJsonFormat()
                .buildUrl();
    }


    private static class FlickrUrlBuilder {

        private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=%s&api_key=%s";

        private static final String FLICKR_METHOD_PHOTO_SEARCH = "flickr.photos.search";
        private static final String FLICKR_METHOD_PHOTO_INTERESTING = "flickr.interestingness.getList";
        private static final String FLICKR_METHOD_PHOTO_RECENT = "flickr.photos.getRecent";

        private static final String FLICKR_METHOD_PHOTO_INFO = "flickr.photos.getInfo";
        private static final String FLICKR_METHOD_PHOTO_SIZES = "flickr.photos.getSizes";

        private static final String FLICKR_PARAM_PHOTO_ID = "photo_id";
        private static final String FLICKR_PARAM_SEARCH_TEXT = "text";
        private static final String FLICKR_PARAM_SORT = "sort";
        private static final String FLICKR_PARAM_PHOTOS_PAGE = "page";
        private static final String FLICKR_PARAM_PHOTOS_PER_PAGE = "per_page";
        private static final String FLICKR_PARAM_EXTRAS = "extras";
        private static final String FLICKR_PARAM_FORMAT = "format";
        private static final String FLICKR_PARAM_NO_JSON_CALLBACK = "nojsoncallback";

        private static final String FLICKR_VALUE_SORT_RELEVANCE = "relevance";
        private static final String FLICKR_VALUE_SORT_INTERESTING = "interestingness-desc";
        private static final String FLICKR_VALUE_SORT_DATE = "date-posted-desc";

        private static final String FLICKR_VALUE_EXTRA_OWNER_NAME= "owner_name";
        private static final String FLICKR_VALUE_EXTRA_UPLOAD_DATE = "date_upload";
        private static final String FLICKR_VALUE_EXTRA_MEDIUM_URL = "url_m";
        private static final String FLICKR_VALUE_EXTRA_LARGE_URL = "url_l";
        private static final String FLICKR_VALUE_EXTRA_ORIGINAL_URL = "url_o";

        private static final String FLICKR_VALUE_DEFAULT_PHOTOS_PER_PAGE = "25";
        private static final String FLICKR_VALUE_FORMAT_JSON = "json";



        private String url;
        private HashMap params;

        public FlickrUrlBuilder() {
            params = new HashMap();
        }

        public FlickrUrlBuilder searchPhotosUrl() {
            this.url = constructBaseURL(FLICKR_METHOD_PHOTO_SEARCH);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder popularPhotosUrl() {
            this.url = constructBaseURL(FLICKR_METHOD_PHOTO_INTERESTING);
            return FlickrUrlBuilder.this;
        }
        public FlickrUrlBuilder recentPhotosUrl() {
            this.url = constructBaseURL(FLICKR_METHOD_PHOTO_RECENT);
            return FlickrUrlBuilder.this;
        }
        public FlickrUrlBuilder photoInfoUrl() {
            this.url = constructBaseURL(FLICKR_METHOD_PHOTO_INFO);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder photoSizesUrl() {
            this.url = constructBaseURL(FLICKR_METHOD_PHOTO_SIZES);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addPhotoId(String id) {
            addParam(FLICKR_PARAM_PHOTO_ID, id);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addSearchTerm(String search) {
            addParam(FLICKR_PARAM_SEARCH_TEXT, search);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addSorting(PhotoViewModel.Sort sorting) {
            if (sorting == PhotoViewModel.Sort.RELEVANT)
                addParam(FLICKR_PARAM_SORT, FLICKR_VALUE_SORT_RELEVANCE);
            else if (sorting == PhotoViewModel.Sort.INTERESTING)
                addParam(FLICKR_PARAM_SORT, FLICKR_VALUE_SORT_INTERESTING);
            else if (sorting == PhotoViewModel.Sort.RECENT)
                addParam(FLICKR_PARAM_SORT, FLICKR_VALUE_SORT_DATE);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addPage(int page) {
            if (page < 1) {
                page = 1;
            }
            addParam(FLICKR_PARAM_PHOTOS_PAGE, String.valueOf(page));
            addParam(FLICKR_PARAM_PHOTOS_PER_PAGE, FLICKR_VALUE_DEFAULT_PHOTOS_PER_PAGE);
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addExtraFields() {
            addParam(FLICKR_PARAM_EXTRAS, TextUtils.join(",",
                    new String[] {FLICKR_VALUE_EXTRA_OWNER_NAME,
                            FLICKR_VALUE_EXTRA_UPLOAD_DATE,
                            FLICKR_VALUE_EXTRA_MEDIUM_URL,
                            FLICKR_VALUE_EXTRA_LARGE_URL,
                            FLICKR_VALUE_EXTRA_ORIGINAL_URL}));
            return FlickrUrlBuilder.this;
        }

        public FlickrUrlBuilder addJsonFormat() {
            addParam(FLICKR_PARAM_FORMAT, FLICKR_VALUE_FORMAT_JSON);
            addParam(FLICKR_PARAM_NO_JSON_CALLBACK, "1");
            return FlickrUrlBuilder.this;
        }

        public String buildUrl() {
            constructParams();
            return this.url;
        }

        private void addParam(String param, String value) {
            params.put(param, value);
        }

        private String constructBaseURL(String method) {
            return String.format(FLICKR_BASE_URL, method, BuildConfig.FLICKR_API_KEY);
        }

        private void constructParams() {
            Set paramSet = params.keySet();
            Iterator<String> iter = paramSet.iterator();

            while (iter.hasNext()) {
                String param = iter.next();
                this.url += "&" + param + "=" + params.get(param);
            }
        }
    }
}
