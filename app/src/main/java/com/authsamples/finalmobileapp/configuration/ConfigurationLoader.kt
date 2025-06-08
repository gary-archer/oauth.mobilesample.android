package com.authsamples.finalmobileapp.configuration

import android.content.Context
import com.authsamples.finalmobileapp.R
import com.google.gson.Gson
import okio.Buffer
import okio.buffer
import okio.source
import java.nio.charset.Charset

/*
 * A helper class to load the application configuration
 */
class ConfigurationLoader {

    /*
     * Load configuration from the resource
     */
    fun load(context: Context): Configuration {

        // Get the raw resource
        val stream = context.resources.openRawResource(R.raw.mobile_config)
        val configSource = stream.source().buffer()

        // Read it as JSON text
        val configBuffer = Buffer()
        configSource.readAll(configBuffer)
        val configJson = configBuffer.readString(Charset.forName("UTF-8"))

        // Deserialize it into objects
        return Gson().fromJson(configJson, Configuration::class.java)
    }
}
