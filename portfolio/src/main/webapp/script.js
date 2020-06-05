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
// var client_id = '444dda318d8949cb9282d95a9885a7c6';
// var client_secret = '9f81ba6cf5748ef9d24a7ddb1e8bc4c';
// var redirect_uri = 'https://8080-dot-12317386-dot-devshell.appspot.com/misc.html';

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

/**
 * Fetches comments from the server and adds them to the DOM.
 */
async function displayComments() {
  const commentLimit = document.getElementById("comment-limit").value;
  const response = await fetch(`/add-comments?comment-limit=${commentLimit}`);
  const comments = await response.json();
  let commentsResult;

  if (comments.length) {
    commentsResult = comments.join('\n\n');
  } else {
    commentsResult = "Uh-oh, there are no comments do display! You can add one below.";
  }

  console.log(comments);
  document.getElementById('comments-container').innerText = commentsResult;
}

/**
 * Deletes comments from the server, removes them from the DOM, and updates the display.
 */
async function deleteComments() {
  const result = window.confirm("Are you sure you want to delete all the comments?")

  if (result) {
    await fetch('/delete-comments', {method: 'POST'});
    displayComments();
  }
}
