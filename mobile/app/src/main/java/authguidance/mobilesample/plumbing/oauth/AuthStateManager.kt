package authguidance.mobilesample.plumbing.oauth

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.AnyThread
import android.util.Log

import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse

import org.json.JSONException

import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

/**
 * The AppAuth sample's class for persisting tokens
 * An example persistence mechanism for an [AuthState] instance.
 * This stores the instance in a shared preferences file, and provides thread-safe access and
 * mutation.
 */
class AuthStateManager private constructor(context: Context) {

    private val mPrefs: SharedPreferences
    private val mPrefsLock: ReentrantLock
    private val mCurrentAuthState: AtomicReference<AuthState>

    val current: AuthState
        @AnyThread
        get() {
            if (mCurrentAuthState.get() != null) {
                return mCurrentAuthState.get()
            }

            val state = readState()
            return if (mCurrentAuthState.compareAndSet(null, state)) {
                state
            } else {
                mCurrentAuthState.get()
            }
        }

    init {
        mPrefs = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
        mPrefsLock = ReentrantLock()
        mCurrentAuthState = AtomicReference()
    }

    @AnyThread
    fun replace(state: AuthState): AuthState {
        writeState(state)
        mCurrentAuthState.set(state)
        return state
    }

    @AnyThread
    fun updateAfterAuthorization(
        response: AuthorizationResponse?,
        ex: AuthorizationException?
    ): AuthState {
        val current = current
        current.update(response, ex)
        return replace(current)
    }

    @AnyThread
    fun updateAfterTokenResponse(
        response: TokenResponse?,
        ex: AuthorizationException?
    ): AuthState {
        val current = current
        current.update(response, ex)
        return replace(current)
    }

    @AnyThread
    fun updateAfterRegistration(
        response: RegistrationResponse,
        ex: AuthorizationException?
    ): AuthState {
        val current = current
        if (ex != null) {
            return current
        }

        current.update(response)
        return replace(current)
    }

    @AnyThread
    private fun readState(): AuthState {
        mPrefsLock.lock()
        try {
            val currentState = mPrefs.getString(KEY_STATE, null) ?: return AuthState()

            try {
                return AuthState.jsonDeserialize(currentState)
            } catch (ex: JSONException) {
                Log.w(TAG, "Failed to deserialize stored auth state - discarding")
                return AuthState()
            }

        } finally {
            mPrefsLock.unlock()
        }
    }

    @AnyThread
    private fun writeState(state: AuthState?) {
        mPrefsLock.lock()
        try {
            val editor = mPrefs.edit()
            if (state == null) {
                editor.remove(KEY_STATE)
            } else {
                editor.putString(KEY_STATE, state.jsonSerializeString())
            }

            if (!editor.commit()) {
                throw IllegalStateException("Failed to write state to shared prefs")
            }
        } finally {
            mPrefsLock.unlock()
        }
    }

    companion object {

        private val INSTANCE_REF = AtomicReference(WeakReference<AuthStateManager>(null))

        private val TAG = "AuthStateManager"

        private val STORE_NAME = "AuthState"
        private val KEY_STATE = "state"

        @AnyThread
        fun getInstance(context: Context): AuthStateManager {
            var manager: AuthStateManager? = INSTANCE_REF.get().get()
            if (manager == null) {
                manager = AuthStateManager(context.applicationContext)
                INSTANCE_REF.set(WeakReference(manager))
            }

            return manager
        }
    }
}
