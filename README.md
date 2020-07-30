# authguidance.mobilesample.android

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/authguidance.mobilesample.android/badge.svg?targetFile=app/build.gradle)](https://snyk.io/test/github/gary-archer/authguidance.mobilesample.android?targetFile=app/build.gradle)

### Overview

* A mobile sample using OAuth 2.0 and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement Open Id Connect mobile logins with best usability and reliability**

### Details

* See the [Android Code Sample Overview](https://authguidance.com/2019/09/13/mobile-code-sample-overview/) for an overview of behaviour
* See the [Android Code Sample Instructions](https://authguidance.com/2019/09/29/basicandroidapp-execution/) for details on how to run the code

### Technologies

* Kotlin and Jetpack are used to develop a Single Activity App that connects to a Cloud API and Authorization Server

### Middleware Used

* The [AppAuth-Android Library](https://github.com/openid/AppAuth-Android) implements Authorization Code Flow (PKCE) via a Claimed HTTPS Scheme
* AWS Cognito is used as a Cloud Authorization Server
* The Android Key Store is used to store encrypted tokens on the device after login
* AWS API Gateway is used to host the back end OAuth 2.0 Secured Web API
* AWS S3 and Cloudfront are used to serve mobile deep linking asset files and interstitial web pages
