package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

public class Student {
    String mStudentId, mFirstName, mLastName;
    int[][] mTimeSlots;

    public Student(String[] csvRow) {
        mTimeSlots = new int[Constants.DAY_COUNT][Constants.INDEX_COUNT];

        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
    }

    public boolean isAvailable(int day, int index, Course c) {
        boolean available = mTimeSlots[day][index] == 0;
        for (int timeslot : mTimeSlots[day]) {
            if (timeslot == c.getCourseId()) return false;
        }
        return available;
    }

    public void setBusy(int day, int index, Course course) {
        mTimeSlots[day][index] = course.getCourseId();
    }
}
