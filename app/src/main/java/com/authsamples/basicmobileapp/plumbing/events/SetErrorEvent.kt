package com.authsamples.basicmobileapp.plumbing.events

import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * An event to supply error details to the error summary view
 */
class SetErrorEvent(val containingViewName: String, val error: UIError?) {
}
