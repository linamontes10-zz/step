package com.google.sps.servlets;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/spotify")
public class SpotifyServlet extends HttpServlet {

  private static final String accessToken = "BQBkiQ-D00IcAaqRGEkwsLqYVvDbH5xV_s9E8jTm_FKIMn6ODW1JEbHnQrHa-BllPuEGrL2quCYYbYYFbXaECAly1xLe3CHNPt40PHbw7nZJbOvixGr068NYTNH5ZPUTR9M_kVqOaObexA1sjZ72i21m7qu-hAvu_l0GI4ColOK36g";
  
  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setAccessToken(accessToken)
    .build();

  private static final GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = spotifyApi
    .getUsersCurrentlyPlayingTrack()
    .build();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    try {
      final CompletableFuture<CurrentlyPlaying> currentlyPlayingFuture = getUsersCurrentlyPlayingTrackRequest.executeAsync();

      final CurrentlyPlaying currentlyPlaying = currentlyPlayingFuture.join();

      response.setContentType("text/html;");
      response.getWriter().println("<h1>Hello Spotify!</h1>");
      response.getWriter().println(currentlyPlaying.getItem());
    } catch (CompletionException e) {
      System.out.println("Error: " + e.getCause().getMessage());
    } catch (CancellationException e) {
      System.out.println("Async operation cancelled.");
    }
  }
}