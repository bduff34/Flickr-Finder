package timewizard.com.flickrfinder.data

import com.google.gson.annotations.SerializedName
import java.util.*

class FlickrPhoto {

    @SerializedName("id")
    var photoId: Long = 0
    @SerializedName("dateupload")
    var uploadDate: Date? = null
    @SerializedName("title")
    var title: String = ""
    @SerializedName("ownername")
    var owner: String = ""
    @SerializedName("url_m")
    var mediumUrl: String = ""
    @SerializedName("url_l")
    var largeUrl: String = ""
    @SerializedName("url_o")
    var originalUrl: String = ""

    fun getSmallestPhoto(): String {
        if (!mediumUrl.isNullOrBlank())
            return mediumUrl
        else if (!largeUrl.isNullOrBlank())
            return largeUrl
        else if (!originalUrl.isNullOrBlank())
            return originalUrl

        return ""
    }

    fun getBestPhoto(): String {
        if (!largeUrl.isNullOrBlank())
            return largeUrl
        if (!originalUrl.isNullOrBlank())
            return originalUrl
        if (!mediumUrl.isNullOrBlank())
            return mediumUrl
        return ""
    }

    override fun toString(): String {
        return "photo: { id: " + photoId.toString() +
                ", uploadDate: " + uploadDate +
                ", title: " + title +
                ", owner: " + owner +
                ", mediumUrl: " + mediumUrl +
                ", largeUrl: " + largeUrl +
                ", originalUrl: " + originalUrl + " }"
    }
}