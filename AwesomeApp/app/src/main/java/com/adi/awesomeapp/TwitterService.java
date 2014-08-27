package com.adi.awesomeapp;

import java.util.List;

import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface TwitterService {
    @POST("/oauth/request_token")
    Observable<List<String>> requestToken(
            @Header("Authorization") String authorization
    );

    @POST("/oauth/access_token")
    Observable<List<String>> accessToken(
            @Query("oauth_verifier") String oauthVerifier,
            @Header("Authorization") String authorization
    );

    @POST("/1.1/statuses/update_with_media.json")
    @Headers({"Content-Type:multipart/form-data"})
    Observable<List<String>> tweetPic(
            @Query("status") String status,
            @Query("media[]") String image,
            @Header("Authorization") String authorization
    );
}
