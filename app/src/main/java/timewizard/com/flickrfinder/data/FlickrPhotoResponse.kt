package timewizard.com.flickrfinder.data

import com.google.gson.annotations.SerializedName

class FlickrPhotoResponse {

    @SerializedName("photos")
    var page: FlickrPhotoPage? = null
    @SerializedName("stat")
    var status: String = ""
}