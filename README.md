# authguidance.mobilesample.android

### Overview

* A mobile sample using OAuth 2.0 and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement Open Id Connect mobile logins with best usability and reliability**

### Details

* See the [Android Code Sample Overview](https://authguidance.com/2019/09/13/mobile-code-sample-overview/) for an overview of behaviour
* See the [Android Code Sample Instructions](https://authguidance.com/2019/09/29/basicandroidapp-execution/) for details on how to run the code

### Technologies

* Kotlin and Jetpack are used to develop a Single Activity App that connects to a Cloud API and Authorization Server
* Navigation scenarios related to logins and deep links are handled via https://authguidance-examples.com

### Middleware Used

* The [AppAuth-Android Library](https://github.com/openid/AppAuth-Android) is used to implement the Authorization Code Flow (PKCE)
* Android Key Store + Shared Preferences are used to securely store a refresh token on the device after login
* AWS Cognito is used as a Cloud Authorization Server
* AWS API Gateway is used to host our sample OAuth 2.0 Secured API
