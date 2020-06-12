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

  public final int duration = 0;
  public final Boolean notInclusive = false;
  public final Boolean inclusive = true;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();

    List<TimeRange> availableEventTimes = new ArrayList<>();
    events.stream()
          .sorted((firstRange, secondRange) -> firstRange.getWhen().start() - secondRange.getWhen().start())
          .forEach(e-> {
            boolean attendee = !Collections.disjoint(e.getAttendees(), attendees);
            if (attendee) {
              availableEventTimes.add(e.getWhen());
            }
          });

    availableEventTimes.add(TimeRange.fromStartDuration(TimeRange.END_OF_DAY, duration));
    List<TimeRange> availableMeetingTimes = new ArrayList<TimeRange>();
    int meetingStartRequest = TimeRange.START_OF_DAY;

    for (int iterator = 0; iterator < availableEventTimes.size() - 1; iterator++) {
      int timeRangeStart = availableEventTimes.get(iterator).start();
      TimeRange availableTimeRange = TimeRange.fromStartEnd(meetingStartRequest, timeRangeStart, notInclusive);
      availableMeetingTimes.add(availableTimeRange);
      meetingStartRequest = availableEventTimes.get(iterator).end();

      while (iterator < availableEventTimes.size() - 1 && availableEventTimes.get(iterator + 1).start() <= meetingStartRequest) {
        iterator++;
        meetingStartRequest = Math.max(meetingStartRequest, availableEventTimes.get(iterator).end());
      }
    }

    availableMeetingTimes.add(TimeRange.fromStartEnd(meetingStartRequest, TimeRange.END_OF_DAY, inclusive));

    return availableMeetingTimes.stream()
                                .filter(time -> time.duration() >= request.getDuration())
                                .collect(Collectors.toList());
  }
}
