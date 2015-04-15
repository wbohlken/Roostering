package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Student {
    String mStudentId, mFirstName, mLastName;
    Set<Course.Activity> mActivities;
    List[] mTimeSlots;

    public Student(String[] csvRow) {
        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
        mTimeSlots = new ArrayList[Constants.TIMESLOT_COUNT];
        mActivities = new HashSet<Course.Activity>();
    }

    public Student(Student other) {
        mStudentId = other.mStudentId;
        mFirstName = other.mFirstName;
        mLastName = other.mLastName;
        mTimeSlots = new ArrayList[Constants.TIMESLOT_COUNT];
        mActivities = new HashSet<>();
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
        mActivities.add(activity);
        return penalty;
    }

    public int getPenalty() {
        Set<Integer> timeslots = new HashSet<>();
        int penalty = 0;
        for (Course.Activity activity : mActivities) {
            if (!timeslots.add(activity.getTimeslot()))
                penalty++;
        }
        return penalty;
    }
}
