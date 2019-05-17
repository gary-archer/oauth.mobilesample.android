package authguidance.mobilesample.plumbing.utilities

import android.content.Context
import authguidance.mobilesample.R
import authguidance.mobilesample.configuration.Configuration
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
    fun loadConfiguration(context: Context): Configuration {

        // Get the raw resource
        val stream = context.getResources().openRawResource(R.raw.mobile_config)
        val configSource = stream.source().buffer()

        // Read it as JSON text
        val configBuffer = Buffer()
        configSource.readAll(configBuffer)
        val configJson = configBuffer.readString(Charset.forName("UTF-8"))

        // Deserialize it into objects
        val gson = Gson()
        return gson.fromJson<Configuration>(configJson, Configuration::class.java)
    }
}