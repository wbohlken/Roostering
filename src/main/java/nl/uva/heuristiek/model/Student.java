package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Student extends BaseModel {
    String mStudentId, mFirstName, mLastName;
    Set<Integer> mActivities;

    public Student(Context context, String[] csvRow) {
        super(context);
        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
        mActivities = new HashSet<>();
    }

    public void addActivity(int activityIndex) {
        mActivities.add(activityIndex);
    }

    public boolean isAvailable(int timeSlot) {
        for (Integer activityIndex : mActivities) {
            int activityTimeSlot = getContext().getRoomSlot(activityIndex) % 20;
            if (timeSlot == activityTimeSlot) return false;
        }
        return true;
    }

    public int getPenalty() {
        Set<Integer> timeslots = new HashSet<>();
        int penalty = 0;
        for (Integer activityIndex : mActivities) {
            if (!timeslots.add(getActivities().get(activityIndex).getTimeslot()))
                penalty++;
        }
        return penalty;
    }
}
