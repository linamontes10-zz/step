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

/**
 * Formats comments with sentiment.
 */
function formatSentimentComments(commentsJson) {

  console.log(commentsJson.sentimentScores);

  for (comment in commentsJson.comments) {
    let commentNumber = commentsJson.comments.length - comment;
    let sentimentScore = commentsJson.sentimentScores[comment];
    let gif = document.createElement('img');
    let commentDiv = document.createElement('div');
    commentDiv.setAttribute('id', `comment-${commentNumber}`);
    commentDiv.setAttribute('class', 'comment');

    if (sentimentScore > 0.4) {
      gif.setAttribute('src', '/images/happy.gif');
    } else if (sentimentScore < -0.4) {
      gif.setAttribute('src', '/images/sad.gif');
    } else {
      gif.setAttribute('src', '/images/neutral.gif');
    }
    document.getElementById('comments-container').appendChild(commentDiv);
    document.getElementById(`comment-${commentNumber}`).innerText = commentsJson.comments[comment];
    document.getElementById(`comment-${commentNumber}`).appendChild(gif);
  }
}

/**
 * Fetches comments from the server and adds them to the DOM.
 */
async function displayComments() {
  const commentLimit = document.getElementById("comment-limit").value;
  const response = await fetch(`/add-comments?comment-limit=${commentLimit}`);
  const commentsJson = await response.json();
  const noCommentsMessage = "Uh-oh, there are no comments to display! You can add one below.";

  if (commentsJson) {
    formatSentimentComments(commentsJson);
  } else {
    document.getElementById('comments-container').innerText = noCommentsMessage;
  }
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
