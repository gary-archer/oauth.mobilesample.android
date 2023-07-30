package com.authsamples.basicmobileapp.plumbing.utilities

import com.google.gson.Gson
import okhttp3.Response
import java.lang.IllegalStateException

class HttpResponseDeserializer {

    fun <T> readBody(response: Response, runtimeType: Class<T>): T {

        if (response.body == null) {
            throw IllegalStateException("Unable to deserialize HTTP response into type ${runtimeType.simpleName}")
        }

        return Gson().fromJson(response.body?.string(), runtimeType)
    }
}
