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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommentSentiment {
  Arraylist<String> comments;
  ArrayList<Doubles> sentimentScores;

  CommentSentiment() {
    this.comments = new ArrayList<String>();
    this.sentimentScores = new ArrayList<Double>();
  }
}

/** Servlet that adds comments and determines sentiment using Datastore and NLP */
@WebServlet("/add-comments")
public class AddCommentsServlet extends HttpServlet {

  public final Integer COMMENT_MAXIMUM = 100;
  public final Integer COMMENT_MINIMUM = 0;
  public final Integer DEFAULT_COMMENT_LIMIT = 10;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int commentLimit = getCommentLimit(request);

    CommentSentiment commentSentiment = new CommentSentiment();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Query is sorted in descending order to show most recent comment entitites first
    Query query = new Query("Comment").addSort("timestampMs", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity commentEntity : results.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      String comment = (String) commentEntity.getProperty("comment");
      String name = (String) commentEntity.getProperty("name");
      Double sentimentScore = (Double) entity.getProperty("sentimentScore");
      commentSentiment.comments.add(comment + " by " + name);
      commentSentiment.sentimentScores.add(sentimentScore);
    }

    String jsonComments = convertToJsonUsingGson(commentSentiment);
    response.setContentType("application/json");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentString = request.getParameter("comment-input");
    String nameString = request.getParameter("name-input");
    long timestampMs = System.currentTimeMillis();

    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float sentimentScore = sentiment.getScore();
    languageService.close();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", commentString);
    commentEntity.setProperty("name", nameString);
    commentEntity.setProperty("sentimentScore", sentimentScore);
    commentEntity.setProperty("timestampMs", timestampMs);

    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }

  private String convertToJsonUsingGson(ArrayList<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  /** Returns the comment limit given by the user. */
  private int getCommentLimit(HttpServletRequest request) {
    String commentLimitString = request.getParameter("comment-limit");
 
    int commentLimit;

    try {
      commentLimit = Integer.parseInt(commentLimitString);
    } catch (NumberFormatException e) {
      // Set default value for comment limit if parsing fails
      return DEFAULT_COMMENT_LIMIT;
    }

    // Enforce boundaries on the comment limit as per the HTML input
    if (commentLimit < COMMENT_MINIMUM) {
      commentLimit = COMMENT_MINIMUM;
    } else if (commentLimit > COMMENT_MAXIMUM) {
      commentLimit = COMMENT_MAXIMUM;
    }

    return commentLimit;
  }
}
