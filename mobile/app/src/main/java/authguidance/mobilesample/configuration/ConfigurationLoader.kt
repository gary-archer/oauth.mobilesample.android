package authguidance.mobilesample.configuration

import android.content.Context
import authguidance.mobilesample.R
import com.google.gson.Gson
import okio.Buffer
import okio.buffer
import okio.source
import org.json.JSONObject
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
        val configJson = JSONObject(configBuffer.readString(Charset.forName("UTF-8")))
        val configJsonText = configJson.toString()

        // Deserialize it into objects
        val gson = Gson()
        return gson.fromJson<Configuration>(configJsonText, Configuration::class.java)
    }
}