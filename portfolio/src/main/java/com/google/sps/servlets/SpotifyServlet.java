package com.google.sps.servlets;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/spotify")
public class SpotifyServlet extends HttpServlet {

  private static final String clientId = "444dda318d8949cb9282d95a9885a7c6";
  private static final String clientSecret = "9f81b8a6cf5748ef9d24a7ddb1e8bc4c";
  private static final String redirectUriString = "https://8080-b0939ef0-d7b7-4da5-abf9-d0bf54e01dcf.us-east1.cloudshell.dev/spotify";
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
    Integer seconds = (Integer)(milliseconds/ 1000) % 60;
    Integer minutes = (Integer)(milliseconds/ (1000) / 60);
    return String.format("%d:%02d", minutes, seconds);
  }

  public static void authorizationCode() {
    try {
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
      spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
      System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public static void authorizationCodeRefresh() {
    try {
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
 
      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
      spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
 
      System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public static String getCurrentTrack() {
    try {
      final CompletableFuture<CurrentlyPlaying> currentlyPlayingFuture = getUsersCurrentlyPlayingTrackRequest.executeAsync();

      final CurrentlyPlaying currentlyPlaying = currentlyPlayingFuture.join();

      if (currentlyPlaying == null) {
        return "No song currently playing.";
      }

      IPlaylistItem item = currentlyPlaying.getItem();
      String songName = "";
      String artists = "";
      
      if (item instanceof Track) {
        songName = ((Track)item).getName();

        for (int iterator = 0; iterator < ((Track)item).getArtists().length; iterator++) {
          if (iterator == ((Track)item).getArtists().length - 1) {
            artists += ((Track)item).getArtists()[iterator].getName();
            break;
          }
          artists += ((Track)item).getArtists()[iterator].getName() + ", ";
        }
      }

      Integer progressMilliseconds = currentlyPlaying.getProgress_ms();
      Integer durationMilliseconds = ((Track)item).getDurationMs();

      String currentSongString = songName + " by " + artists + " at " +
                                 formatAsMinutesAndSeconds(progressMilliseconds) +
                                 " of " + formatAsMinutesAndSeconds(durationMilliseconds);

      return currentSongString;
    } catch (CompletionException e) {
      return "No song currenly playing.";
    } catch (CancellationException e) {
      return "Error receiving currenly playing song.";
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    if (oauthToken.isEmpty() && request.getParameter("code") == null) {
      response.sendRedirect(authorizationCodeUri());
      return;
    } else if (oauthToken.isEmpty() && request.getParameter("code") != null) {
      oauthToken = request.getParameter("code");
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
