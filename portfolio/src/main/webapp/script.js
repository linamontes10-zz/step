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
 * Adds currently playing song to the page.
 */
async function getCurrentlyPlayingSongAsync() {
  const response = await fetch("/spotify");
  const song = await response.text();
  document.getElementById("currently-playing").innerText = song;
}

setInterval(function() {
  getCurrentlyPlayingSongAsync();
}, 10000);

/**
 * Adds a random fun fact to the page.
 */
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
