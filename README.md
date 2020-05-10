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
* AWS Cognito is used as a Cloud Authorization Server
* The iOS device keychain is used to securely store tokens on the device after login
* The Android Key Store is used to store encrypted tokens on the device after login
* AWS API Gateway is used to host our sample OAuth 2.0 Secured API
* AWS Cloudfront is used to host mobile deep linking asset files
