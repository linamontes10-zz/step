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

/**
 * Adds a random fun fact to the page.
 */

// Spotify API keys to be used for later
var oauth_token = 'BQCebg7W1ZsfuUCdCjdcHz-GDJFMj5GjOhTxvrpNFhu3gvEzYftF3UrSIZ9KHYXVjVdF346qrcaQtd4df7FN0p5sQj2x2p3HpEsCtIDPg4owl4aZx5HfQ4wqWKY3y8l2KakUPS2ZwGKW_PbiPefFMJ3y2FVGfzmz2Kb--w9DxyLBOA';

function minutesAndSeconds(milliseconds) {
  var minutes = Math.floor(milliseconds /60000);
  var seconds = ((milliseconds % 60000) / 1000).toFixed(0)
  return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
}

function getCurrentlyPlayingSong() {
  const response = fetch("https://api.spotify.com/v1/me/player/currently-playing", {
    method: 'GET',
    headers: {
      Authorization: 'Bearer ' + oauth_token
    }
  })

  response.then(handleReponse);
}

setInterval(function() {
  getCurrentlyPlayingSong();
}, 500);

function handleReponse(response) {

  // Set variable equal to response json
  const text = response.json();

  // Pass text to AddSongToPage
  text.then(addSongToPage)
}

function addSongToPage(song) {
  let artists = '';
  let percentage = (song.progress_ms / song.item.duration_ms) * 100;

  for (let iterator = 0; iterator < song.item.album.artists.length; iterator++) {
    if (iterator == song.item.album.artists.length - 1) {
      artists += song.item.album.artists[iterator].name;
      break;
    }
    artists + song.item.album.artists[iterator].name + ',';
  }
  
  document.getElementById('song-title').innerText = song.item.name;
  document.getElementById('song-artists').innerText = artists;
  document.getElementById('song-progress').innerText = minutesAndSeconds(
    song.progress_ms);
  document.getElementById('song-duration').innerText = minutesAndSeconds(
    song.item.duration_ms);
}

function addRandomFunFact() {
  const funFacts =
      ['I am a Capricorn sun, Aquarius Moon, and Cancer rising',
       'I am fluent in Spanish and English',
       'I adopted a dog when I was 15, and his name is Austin!',
       'I am a first-generation college student.'];

  // Pick a random greeting.
  const funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  funFactContainer.innerText = funFact;
}
