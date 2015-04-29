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
    List[] mTimeSlots;

    public Student(Context context, String[] csvRow) {
        super(context);
        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
        mTimeSlots = new ArrayList[Constants.TIMESLOT_COUNT];
        mActivities = new HashSet<>();
    }

    public void addActivity(int activityIndex) {
        mActivities.add(activityIndex);
    }

    public boolean isAvailable(int timeSlot) {
        return mTimeSlots[timeSlot] == null;
    }

    public int setBusy(Course.Activity activity, int timeslot) {
        int penalty = 0;
        if (mTimeSlots[timeslot] != null && mTimeSlots[timeslot].size() >0)
            penalty++;
        else if (mTimeSlots[timeslot] == null)
            mTimeSlots[timeslot] = new ArrayList();
        mTimeSlots[timeslot].add(activity.getCourse().getCourseId());
        return penalty;
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
