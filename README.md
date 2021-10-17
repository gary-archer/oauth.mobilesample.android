# oauth.mobilesample.android

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/0eafe484d5164e0a8ba0628c96784524)](https://www.codacy.com/gh/gary-archer/oauth.mobilesample.android/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.mobilesample.android&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.mobilesample.android/badge.svg?targetFile=app/build.gradle)](https://snyk.io/test/github/gary-archer/oauth.mobilesample.android?targetFile=app/build.gradle)

### Overview

* A mobile sample using OAuth and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement Open Id Connect mobile logins with best usability and reliability**

### Details

* See the [Android Code Sample Overview](https://authguidance.com/2019/09/13/mobile-code-sample-overview/) for an overview of behaviour
* See the [Android Code Sample Instructions](https://authguidance.com/2019/09/29/basicandroidapp-execution/) for details on how to run the code

### Technologies

* Kotlin and Jetpack are used to develop a Single Activity App that connects to a Cloud API and Authorization Server

### Middleware Used

* The [AppAuth-Android Library](https://github.com/openid/AppAuth-Android) is used to implement the Authorization Code Flow (PKCE) via a Claimed HTTPS Scheme
* AWS API Gateway is used to host the back end OAuth Secured Web API
* AWS Cognito is used as the default Authorization Server for the Mobile App and API
* The Android Key Store is used to store encrypted tokens on the device after login
* AWS S3 and Cloudfront are used to serve mobile deep linking asset files and interstitial web pages
