package timewizard.com.flickrfinder.ui.main

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import timewizard.com.flickrfinder.BuildConfig
import timewizard.com.flickrfinder.R


class PhotoListAdapter(private val context: Context, private val vm: PhotoViewModel) :
    RecyclerView.Adapter<PhotoListAdapter.PhotoViewHolder>() {

    override fun getItemCount(): Int {
        return vm.mNumLoadedPhotos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.lvi_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (position < vm.mNumLoadedPhotos) {
            val photo = vm.mPhotoList.value!![position]
            holder.mTitle.text = photo.title
            holder.mOwner.text = photo.owner

            // load the image into the list item
            val photoUrl = photo.getSmallestPhoto()
            if (!photoUrl.isNullOrBlank()) {
                try {
                    val builder = Picasso.Builder(context)
                    builder.downloader(OkHttp3Downloader(context))
                    builder.build()
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_photo_black)
                        .error(R.drawable.ic_broken_image_black)
                        .into(holder.mThumbnail)
                } catch (e: Exception) {
                    Log.e(BuildConfig.TAG_FLICKR_FINDER, e.message)
                    Log.d(BuildConfig.TAG_FLICKR_FINDER, vm.mPhotoList.value!![position].toString())
                    vm.mErrorMessage.value = "Could not load photo at position: " + position
                }
            }

            holder.mView.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    vm.selectPhoto(position)
                    Log.d(BuildConfig.TAG_FLICKR_FINDER, "Clicked " + photo)
                }
            })
        }
    }

    inner class PhotoViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
        var mThumbnail: ImageView
        var mTitle: TextView
        var mOwner: TextView

        init {
            mThumbnail = mView.findViewById(R.id.photo_image)
            mTitle = mView.findViewById(R.id.photo_title)
            mOwner = mView.findViewById(R.id.photo_owner)
        }
    }
}