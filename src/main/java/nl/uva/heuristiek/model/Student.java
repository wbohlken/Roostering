package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Context;

import java.util.HashSet;
import java.util.Set;

public class Student extends BaseModel {
    int mStudentId;
    String mFirstName, mLastName;
    Set<Integer> mActivities;

    public Student(Context context, String[] csvRow) {
        super(context);
        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = Integer.parseInt(csvRow[2]);
        mActivities = new HashSet<>();
    }

    public Set<Integer> getActivitiesSet() {
        return mActivities;
    }

    public void addActivity(int activityIndex) {
        mActivities.add(activityIndex);
    }

    public boolean isAvailable(int timeSlot, Schedule schedule) {
        for (Integer activityIndex : mActivities) {
            int activityTimeSlot = schedule.getRoomSlot(activityIndex) % 20;
            if (timeSlot == activityTimeSlot) return false;
        }
        return true;
    }

    public int getPenalty(Schedule schedule) {
        Set<Integer> timeslots = new HashSet<>();
        int penalty = 0;
        for (Integer activityIndex : mActivities) {
            if (!timeslots.add(getActivities().get(activityIndex).getTimeslot(schedule)))
                penalty++;
        }
        return penalty;
    }

    public Set<Integer> getActivitiesIndices() {
        return mActivities;
    }

    public int getId() {
        return mStudentId;
    }
}
