package timewizard.com.flickrfinder.data

import com.google.gson.annotations.SerializedName

class FlickrPhotoPage {

    @SerializedName("page")
    var page: Int = 1
    @SerializedName("pages")
    var totalPages: Int = 1
    @SerializedName("perpage")
    var perPage: Int = 25
    @SerializedName("total")
    var totalPhotos: Long = 0
    @SerializedName("photo")
    var photoList: List<FlickrPhoto>? = null

}