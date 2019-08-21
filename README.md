# authguidance.mobilesample.android

### Overview

* A mobile sample using OAuth 2.0 and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement Open Id Connect mobile logins and to integrate with an OAuth 2.0 secured API**

### Details

* TODO - link to blog

### Technologies

* Kotlin is used to develop a Single Activity App

### Middleware Used

* The [AppAuth-Android Library](https://github.com/openid/AppAuth-Android) is used to implement the Authorization Code Flow (PKCE)
* The [Android Account Manager](https://developer.android.com/training/id-auth/authenticate) library is used by the Mobile App for secure storage of OAuth tokens
* AWS Cognito is used as a Cloud Authorization Server
* AWS API Gateway is used to host our sample OAuth 2.0 Secured API
