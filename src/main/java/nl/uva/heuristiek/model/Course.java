package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Util;

import java.awt.*;
import java.util.*;

/**
 * Created by remco on 07/04/15.
 */
public class Course {

    private static final int TYPE_ACTIVITY_LECTURE = 0;
    private static final int TYPE_ACTIVITY_WORKGROUP = 1;
    private static final int TYPE_ACTIVITY_PRACTICAL = 2;

    private String mCourseId;
    private String mName;
    private int mLectureCount;
    private int mWorkGroupCount;
    private int mPracticumCount;

    private int mGroupCount;
    private Set<Student> mStudents = new HashSet<Student>();
    private ArrayList<Set<Student>> mStudentGroups;
    private Set<Activity> mActivities;
    private boolean[][] mDayUsed;
    private Set<Activity> mPenaltyActivities;


    public Course(String[] csvRow) {
        mCourseId = csvRow[0];
        mName = csvRow[1];
        mLectureCount = Util.tryParseInt(csvRow[2]);
        mWorkGroupCount = Util.tryParseInt(csvRow[3]);
        int workGroupCapacity = Util.tryParseInt(csvRow[4]);
        mPracticumCount = Util.tryParseInt(csvRow[5]);
        int practicumCapacity = Util.tryParseInt(csvRow[6]);
        mGroupCount = Math.max(workGroupCapacity, practicumCapacity);
    }

    public Course(Course other) {
        mCourseId = other.mCourseId;
        mName = other.mName;
        mLectureCount = other.mLectureCount;
        mWorkGroupCount = other.mWorkGroupCount;
        mPracticumCount = other.mPracticumCount;
        mGroupCount = other.mGroupCount;
        mStudents = Util.deepClone(other.mStudents);
    }

    public int getPenalty() {
        mPenaltyActivities = new HashSet<Activity>();
        int penalty = 0;
        if (mActivities != null) {
            Set<Integer>[] d = new Set[5];
            for (Activity activity : mActivities) {
                if (d[activity.getDay()] == null) {
                    d[activity.getDay()] = new HashSet<Integer>();
                    d[activity.getDay()].add(activity.mId);
                } else if (!d[activity.mTimeslot / 4].contains(activity.mId)) {
                    penalty += 10;
                    mPenaltyActivities.add(activity);
                    d[activity.getDay()].add(activity.mId);
                }
            }
        }
        return penalty;
    }

    public Set<Activity> getPanaltyActivities() {
        return mPenaltyActivities;
    }

    public String getCourseId() {
        return mCourseId;
    }

    public String getName() {
        return mName;
    }

    public int getLectureCount() {
        return mLectureCount;
    }

    public int getWorkGroupCount() {
        return mWorkGroupCount;
    }

    public int getPracticumCount() {
        return mPracticumCount;
    }

    public void addStudent(Student student) {
        mStudents.add(student);
    }

    public void fillGroups() {
        mStudentGroups = new ArrayList<Set<Student>>();
        int groupSize = determineGroupSize();
        Set<Student> currentGroup = new HashSet<Student>(groupSize);
        for (Student student : mStudents) {
            currentGroup.add(student);
            if (currentGroup.size() == groupSize) {
                mStudentGroups.add(currentGroup);
                currentGroup = new HashSet<Student>(groupSize);
            }
        }
        if (currentGroup.size() > 0) {
            if (mStudentGroups.size() == 0) {
                mStudentGroups.add(currentGroup);
            } else {
                Set<Student> lastGroup = mStudentGroups.get(mStudentGroups.size() - 1);
                if (currentGroup.size() + lastGroup.size() < groupSize) {
                    for (Student student : currentGroup) {
                        lastGroup.add(student);
                    }
                } else {
                    mStudentGroups.add(currentGroup);
                }
            }
        }
    }

    public Set<Activity> getActivities() {
        if (mStudents.size() == 0) return new HashSet<Activity>();
        if (mStudentGroups == null) fillGroups();
        int activityId = 0;
        if (mActivities == null) {
            mActivities = new HashSet<Activity>();
            for (int i = 0; i < mLectureCount; i++) {
                mActivities.add(new Activity(TYPE_ACTIVITY_LECTURE, mStudents, activityId++));
            }
            for (int i = 0; i < mWorkGroupCount; i++) {
                for (Set<Student> students : mStudentGroups)
                    mActivities.add(new Activity(TYPE_ACTIVITY_WORKGROUP, students, activityId));
                activityId++;
            }
            for (int i = 0; i < mPracticumCount; i++) {
                for (Set<Student> students : mStudentGroups)
                    mActivities.add(new Activity(TYPE_ACTIVITY_PRACTICAL, students, activityId));
                activityId++;
            }
            mDayUsed = new boolean[activityId][Constants.DAY_COUNT];
        }
        return mActivities;
    }

    private int determineGroupSize() {
        return mGroupCount;
    }

    public Set<Student> getStudents() {
        return mStudents;
    }

    public ArrayList<Set<Student>> getStudentGroups() {
        return mStudentGroups;
    }

    public class Activity {
        private int mId;
        private int mType;
        private int mTimeslot;
        private Set<Student> mStudents;

        public Activity(int type, Set<Student> students, int id) {
            mId = id;
            mType = type;
            mStudents = students;
        }

        public Set<Student> getStudents() {
            return mStudents;
        }

        public int plan(int timeslot) {
            mTimeslot = timeslot;
            int penalty = 0;
            mDayUsed[mId][timeslot /4] = true;
            for (Student student : mStudents) {
                penalty += student.setBusy(Course.this, timeslot);
            }
            return penalty;
        }

        public int getDay() {
            return mTimeslot/4;
        }

        public String getName() {
            return mName;
        }

        boolean isDayUsed(int day) {
            for (int i = 0; i < mDayUsed.length; i++) {
                if (i == mId) continue;
                else if (mDayUsed[i][day]) return true;
            }
            return false;
        }

        public Course getCourse() {
            return Course.this;
        }


        public Color getColor() {
            return new Color(mName.hashCode());
        }

        public int getId() {
            return mId;
        }
    }
}
