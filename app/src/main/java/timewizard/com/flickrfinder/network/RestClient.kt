package timewizard.com.flickrfinder.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.*
import java.lang.reflect.Type
import java.util.*


class RestClient constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RestClient? = null
        fun instance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RestClient(context).also {
                    INSTANCE = it
                }
            }
    }

    val gson: Gson by lazy {
        GsonBuilder().registerTypeAdapter(Date::class.java, object: JsonDeserializer<Date> {
            @Throws(JsonParseException::class)
            override fun deserialize(json: JsonElement,
                                     typeOfT: Type,
                                     context: JsonDeserializationContext): Date {
                                                return Date(json.asJsonPrimitive.asLong)
            }
        }).create()
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun sendGetRequest(url: String,
                       responseListener: Response.Listener<String>,
                       errorListener: Response.ErrorListener) {
        val request = StringRequest(Request.Method.GET, url, responseListener, errorListener)
        request.setTag(timewizard.com.flickrfinder.BuildConfig.TAG_FLICKR_FINDER)
        requestQueue.add(request)
    }
}