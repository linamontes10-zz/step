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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  public final int DURATION = 0;
  public final Boolean NOT_INCLUSIVE = false;
  public final Boolean INCLUSIVE = true;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();

    List<TimeRange> availableEventTimes = new ArrayList<>();
    events.stream()
          .sorted((firstRange, secondRange) ->
                   firstRange.getWhen().start() - secondRange.getWhen().start())
          .forEach(e -> {
            boolean attendeeInEvent = !Collections.disjoint(e.getAttendees(), attendees);
            if (attendeeInEvent) {
              availableEventTimes.add(e.getWhen());
            }
          });

    availableEventTimes.add(TimeRange.fromStartDuration(TimeRange.END_OF_DAY, DURATION));
    List<TimeRange> availableMeetingTimes = new ArrayList<TimeRange>();
    int lastRangeEnd = TimeRange.START_OF_DAY;

    for (int i = 0; i < availableEventTimes.size() - 1; i++) {
      int timeRangeStart = availableEventTimes.get(i).start();
      TimeRange availableTimeRange = TimeRange.fromStartEnd(lastRangeEnd, timeRangeStart, NOT_INCLUSIVE);
      availableMeetingTimes.add(availableTimeRange);
      lastRangeEnd = availableEventTimes.get(i).end();

      while (i < availableEventTimes.size() - 1 &&
             availableEventTimes.get(i + 1).start() <= lastRangeEnd) {
        i++;
        lastRangeEnd = Math.max(lastRangeEnd, availableEventTimes.get(i).end());
      }
    }

    availableMeetingTimes.add(TimeRange.fromStartEnd(lastRangeEnd, TimeRange.END_OF_DAY, INCLUSIVE));

    return availableMeetingTimes.stream()
                                .filter(time -> time.duration() >= request.getDuration())
                                .collect(Collectors.toList());
  }
}
