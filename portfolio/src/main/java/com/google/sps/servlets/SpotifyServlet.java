// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.sps.SpotifyAuth;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/spotify")
public class SpotifyServlet extends HttpServlet {
  public static SpotifyAuth spotifyAuthKeys = new SpotifyAuth();
  private static final String clientId = spotifyAuthKeys.getClientId();
  private static final String clientSecret = spotifyAuthKeys.getClientSecret();

  private static final String redirectUriString = "https://linamontes-step-2020.ue.r.appspot.com/spotify";
  private static final URI redirectUri = SpotifyHttpManager.makeUri(redirectUriString);
  private static String oauthToken = "";

  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setClientId(clientId)
    .setClientSecret(clientSecret)
    .setRedirectUri(redirectUri)
    .build();

  private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
    .scope("user-read-currently-playing")
    .show_dialog(true)
    .build();

  private static AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(oauthToken)
    .build();

  private static GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = spotifyApi
    .getUsersCurrentlyPlayingTrack()
    .build();

  private static AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
    .build();

  public static String authorizationCodeUri() {
    final URI uri = authorizationCodeUriRequest.execute();
    return uri.toString();
  }

  public static String formatAsMinutesAndSeconds(Integer milliseconds) {
    Integer seconds = (Integer)(milliseconds / 1000) % 60;
    Integer minutes = (Integer)(milliseconds / (1000) / 60);
    return String.format("%d:%02d", minutes, seconds);
  }

  public static void authorizationCode() {
    try {
      final AuthorizationCodeCredentials authCodeCreds = authorizationCodeRequest.execute();
      spotifyApi.setAccessToken(authCodeCreds.getAccessToken());
      spotifyApi.setRefreshToken(authCodeCreds.getRefreshToken());
      System.out.println("Expires in: " + authCodeCreds.getExpiresIn());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public static void authorizationCodeRefresh() {
    try {
      final AuthorizationCodeCredentials authCodeCreds = authorizationCodeRefreshRequest.execute();

      spotifyApi.setAccessToken(authCodeCreds.getAccessToken());
      spotifyApi.setRefreshToken(authCodeCreds.getRefreshToken());

      System.out.println("Expires in: " + authCodeCreds.getExpiresIn());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public static String getCurrentTrack() {
    try {
      final CurrentlyPlaying currentlyPlaying = getUsersCurrentlyPlayingTrackRequest.execute();

      if (currentlyPlaying == null) {
        return "No song currently playing.";
      }

      String finalSongString = "";
      IPlaylistItem item = currentlyPlaying.getItem();

      if (item instanceof Track) {
        String songName = "";
        songName = ((Track)item).getName();
        ArrayList<String> artistsList = new ArrayList<String>();

        for (ArtistSimplified artist : ((Track)item).getArtists()){
          artistsList.add(artist.getName());
        }

        String artists =  String.join(", ", artistsList);
        Integer progressMilliseconds = currentlyPlaying.getProgress_ms();
        Integer durationMilliseconds = ((Track)item).getDurationMs();
        finalSongString = songName + " by " + artists + " at " +
                          formatAsMinutesAndSeconds(progressMilliseconds) +
                          " of " + formatAsMinutesAndSeconds(durationMilliseconds);
      } else {
        finalSongString = "No song currently playing.";
      }
      return finalSongString;
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
      authorizationCodeRefresh();
      authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      getUsersCurrentlyPlayingTrackRequest = spotifyApi.getUsersCurrentlyPlayingTrack().build();
      return "Fetching currently playing song...";
    } catch (CompletionException e) {
      return "No song currently playing.";
    } catch (CancellationException e) {
      return "Error receiving currently playing song.";
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String callbackAuth = request.getParameter("code");

    if (oauthToken.isEmpty() && callbackAuth == null) {
      response.sendRedirect(authorizationCodeUri());
      return;
    } else if (oauthToken.isEmpty() && callbackAuth != null) {
      oauthToken = callbackAuth;
      authorizationCodeRequest = spotifyApi.authorizationCode(oauthToken).build();
      authorizationCode();
      getUsersCurrentlyPlayingTrackRequest = spotifyApi.getUsersCurrentlyPlayingTrack().build();
      authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      response.sendRedirect("/misc.html");
      return;
    }
    String responseString = getCurrentTrack();
    response.setContentType("text/html;");
    response.getWriter().println(responseString);
  }
}
