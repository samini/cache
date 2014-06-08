cache
=====

Caché: Caching Location-Enhanced Content to Improve User Privacy

If you use or modify the code, especially for research purposes, please refer to the paper.

The ACM MobiSys 2011 paper is available at the following URLs:
http://dl.acm.org/citation.cfm?id=2000015
http://saminicmu.appspot.com/papers/Amini11_Cache_Digital.pdf [mirror]
http://cmuchimps.org/publications/cache_caching_location-enhanced_content_to_improve_user_privacy_2011/pub_download [mirror]

For an extended version of the paper, please refer to the tech report. For citation purposes, please only refer to the ACM MobiSys 2011 paper. The tech report is available at the following URLs:
http://kettle.ubiq.cs.cmu.edu/~samini/papers/Amini10_Cache_TechReport.pdf
http://kettle.ubiq.cs.cmu.edu/~samini/papers/Amini10_Cache_TechReport.pdf [mirror]

Both the actual framework and the sample Cache-enhanced app are Android projects. They can be setup and built most easily using ADT:
http://developer.android.com/sdk/index.html

Since the framework uses an aidl to auto-generate the Android service Java code, it is recommended to use a bundled version of ADT rather than the ADT plugin for eclipse. The ADT eclipse plugin sometimes has issues auto-generating code from aidl.

Notes for users:

* Framework Updates:

The framework uses Google's Geocoding API. For more information on the API, refer to:
http://developers.google.com/maps/documentation/geocoding/

For the framework, CacheServiceV1/src/edu/cmu/ece/cache/framework/location/geocode/GeoCode.java needs to be updated to conform with Google's latest Geocoding API.

* Sample App Updates:

The sample app uses the Yelp developer API. For more information on the API and API id, refer to:
http://www.yelp.com/developers/documentation/technical_overview

For the sample application, please provide a Google Maps API key in the following file:
CacheYelpAppV1/res/layout/yelp_map_activity.xml

The YELP API id should be updated in the following files (Look for FIXMEs):
CacheYelpAppV1/src/edu/cmu/ece/cache/sample/yelp/Main.java
CacheYelpAppV1/src/edu/cmu/ece/cache/sample/yelp/YelpContentRequest.java
