package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

public class Student {
    String mStudentId, mFirstName, mLastName;
    boolean[][] mTimeSlots;

    public Student(String[] csvRow) {
        mTimeSlots = new boolean[Constants.DAY_COUNT][Constants.INDEX_COUNT];

        mLastName = csvRow[0];
        mFirstName = csvRow[1];
        mStudentId = csvRow[2];
    }

    public boolean isAvailable(int day, int index) {
        return !mTimeSlots[day][index];
    }

    public void setBusy(int day, int index, boolean busy) {
        mTimeSlots[day][index] = busy;
    }
}
