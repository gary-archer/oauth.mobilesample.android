package com.authguidance.basicmobileapp.views.utilities

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.button.MaterialButton

/*
 * Override the material button to support long press handling
 */
class LongPressableButton : MaterialButton {

    private var longPressStartTime: Long? = null
    private val longPressMilliseconds = 2000
    private var customClickListener: ((longClicked: Boolean) -> Unit)? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    /*
     * Receive a callback
     */
    fun setCustomClickListener(listener: (long: Boolean) -> Unit) {
        this.customClickListener = listener
    }

    /*
     * Handle the touch event
     */
    @Suppress("ReturnCount")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                if (this.isEnabled) {
                    this.handlePress()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (this.isEnabled) {
                    performClick()
                    this.handleRelease()
                }
                return true
            }
        }

        return false
    }

    /*
     * Override this to avoid accessibility warnings
     */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /*
     * When clicked, measure the start time
     */
    private fun handlePress() {

        this.longPressStartTime = System.currentTimeMillis()
    }

    /*
     * When released, measure the end time to determine whether a long click and call back the parent fragment
     */
    private fun handleRelease() {

        val longClicked = this.isLongPress()
        this.customClickListener?.invoke(longClicked)
    }

    /*
     * Return true if a long press has occurred
     */
    private fun isLongPress(): Boolean {

        val start = this.longPressStartTime ?: return false

        // Get the time taken and then reset
        val timeTaken = System.currentTimeMillis() - start
        this.longPressStartTime = null

        // A long press occurs when the touch has taken longer than 2 seconds
        return timeTaken > longPressMilliseconds
    }
}
