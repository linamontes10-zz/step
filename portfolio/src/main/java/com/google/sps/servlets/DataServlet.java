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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data using Datastore */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Query is sorted in descending order to show most recent comment entitites first
    Query query = new Query("Comment").addSort("timestampMs", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    ArrayList<String> comments = new ArrayList<String>();
    
    for (Entity commentEntity : results.asIterable()) {
      String comment = (String) commentEntity.getProperty("comment");
      String name = (String) commentEntity.getProperty("name");
      comments.add(comment + " by " + name);
    }

    String jsonComments = convertToJsonUsingGson(comments);
    response.setContentType("application/json");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentString = request.getParameter("comment-input");
    String nameString = request.getParameter("name-input");
    long timestampMs = System.currentTimeMillis();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", commentString);
    commentEntity.setProperty("name", nameString);
    commentEntity.setProperty("timestampMs", timestampMs);

    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }

  private String convertToJsonUsingGson(ArrayList<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
