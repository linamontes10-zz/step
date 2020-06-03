package com.google.sps.servlets;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.IPlaylistItem;
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

  private static final String accessToken = "BQBvZO6Dp0TDvD-1FXC_vHhrAuKDWAbx4YlBF_08uD7SryHXQZ9tqt_VRYG3F7aEleEBYgv7JUFd6SvQzChkru9j6qfD04txyyEFa_GZMQEiEvNet0Ehb01SE_fA1NKx-XLlHKLAtvQDQTwyu-1qDeli13SjPiHyReluAKZp91HTxs2D";
  
  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setAccessToken(accessToken)
    .build();

  private static final GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = spotifyApi
    .getUsersCurrentlyPlayingTrack()
    .build();

  public static String formatAsMinutesAndSeconds(Integer milliseconds) {
    Integer seconds = (Integer)(milliseconds/ 1000) % 60;
    Integer minutes = (Integer)(milliseconds/ (1000) / 60);
    return String.format("%d:%02d", minutes, seconds);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    try {
      final CompletableFuture<CurrentlyPlaying> currentlyPlayingFuture = getUsersCurrentlyPlayingTrackRequest.executeAsync();

      final CurrentlyPlaying currentlyPlaying = currentlyPlayingFuture.join();

      if (currentlyPlaying == null) {
        response.setContentType("text/html;");
        response.getWriter().println("No song currently playing.");
        return;
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

      response.setContentType("text/html;");
      response.getWriter().println(songName + " by " + artists + " at " +
                                   formatAsMinutesAndSeconds(progressMilliseconds) +
                                   " of " + formatAsMinutesAndSeconds(durationMilliseconds));
    } catch (CompletionException e) {
      response.setContentType("text/html;");
      response.getWriter().println("No song currenly playing.");
    } catch (CancellationException e) {
      System.out.println("Async operation cancelled.");
    }
  }
}
