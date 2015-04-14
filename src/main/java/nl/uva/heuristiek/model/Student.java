package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

import java.util.ArrayList;
import java.util.List;

public class Student {
    String mStudentId, mFirstName, mLastName;
    List[] mTimeSlots;

    public Student(String[] csvRow) {
        mTimeSlots = new ArrayList[Constants.TIMESLOT_COUNT];

        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
    }

    public Student(Student other) {
        mStudentId = other.mStudentId;
        mFirstName = other.mFirstName;
        mLastName = other.mLastName;
        mTimeSlots = new ArrayList[Constants.TIMESLOT_COUNT];
    }

    public boolean isAvailable(int timeSlot) {
        return mTimeSlots[timeSlot] == null;
    }

    public int setBusy(Course course, int timeslot) {
        int penalty = 0;
        if (mTimeSlots[timeslot] != null && mTimeSlots[timeslot].size() >0)
            penalty++;
        else if (mTimeSlots[timeslot] == null)
            mTimeSlots[timeslot] = new ArrayList();
        mTimeSlots[timeslot].add(course.getCourseId());
        return penalty;
    }
}
