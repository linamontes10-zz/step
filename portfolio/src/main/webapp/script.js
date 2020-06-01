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

function formatAsMinutesAndSeconds(milliseconds) {
  const minutes = Math.floor(milliseconds / 60000);
  const seconds = ((milliseconds % 60000) / 1000).toFixed(0)
  return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
}

function getCurrentlyPlayingSong() {
  $.get("./auth.txt", function(data) {
    let oauthToken = data;
    const response = fetch("https://api.spotify.com/v1/me/player/currently-playing", {
      method: "GET",
      headers: {
        Authorization: "Bearer " + oauthToken
      }
    })
    response.then(handleReponse);
  });
}

setInterval(function() {
  getCurrentlyPlayingSong();
}, 10000);

function handleReponse(response) {

  const text = response.json();

  text.then(addSongToPage)
}

function addSongToPage(song) {
  let artists = "";

  for (let iterator = 0; iterator < song.item.album.artists.length; iterator++) {
    if (iterator == song.item.album.artists.length - 1) {
      artists += song.item.album.artists[iterator].name;
      break;
    }
    artists += song.item.album.artists[iterator].name + ", ";
  }
  
  document.getElementById("currently-playing").innerText = song.item.name + " by "
    + artists + " at " + formatAsMinutesAndSeconds(song.progress_ms) + " of " +
    formatAsMinutesAndSeconds(song.item.duration_ms);
}

function addRandomFunFact() {
    const funFacts =
      ['I am a Capricorn sun, Aquarius Moon, and Cancer rising.',
        'I am fluent in Spanish and English.',
        'I adopted a dog when I was 15, and his name is Austin!',
        'I am a first-generation college student.',
        'I used to play soccer competetively in high school.'];
  
    // Pick a random greeting.
    const funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

    // Add it to the page.
    const funFactContainer = document.getElementById('fun-fact-container');
    funFactContainer.innerText = funFact;
}

async function addContent() {
  const response = await fetch('/data');
  const hello = await response.text();
  document.getElementById('data').innerHTML = hello;
}
