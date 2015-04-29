package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.Util;

import java.awt.*;
import java.util.*;

/**
 * Created by remco on 07/04/15.
 */
public class Course extends BaseModel {

    private static final int TYPE_ACTIVITY_LECTURE = 0;
    private static final int TYPE_ACTIVITY_WORKGROUP = 1;
    private static final int TYPE_ACTIVITY_PRACTICAL = 2;

    private String mCourseId;
    private String mName;
    private int mLectureCount;
    private int mWorkGroupCount;
    private int mPracticumCount;

    private int mGroupCount;
    private Set<Student> mStudents = new HashSet<>();
    private Set<Activity> mLectureActivities;
    private ArrayList<Set<Student>> mStudentGroups;
    private Map<Integer, Set<Activity>> mGroupActivities;
    private boolean[][] mDayUsed;
    private Set<Activity> mPenaltyActivities;


    public Course(Context context, String[] csvRow) {
        super(context);
        mCourseId = csvRow[0];
        mName = csvRow[1];
        mLectureCount = Util.tryParseInt(csvRow[2]);
        mWorkGroupCount = Util.tryParseInt(csvRow[3]);
        int workGroupCapacity = Util.tryParseInt(csvRow[4]);
        mPracticumCount = Util.tryParseInt(csvRow[5]);
        int practicumCapacity = Util.tryParseInt(csvRow[6]);
        mGroupCount = Math.max(workGroupCapacity, practicumCapacity);
    }

    private int getTotalActivities() {
        return mLectureCount+mWorkGroupCount+mPracticumCount;
    }


    public int getPenalty() {
        int activityPenalty = 0;
        HashSet<Integer> lectureDays = new HashSet<>(5);
        HashSet<Integer> days = new HashSet<>();
        if (mLectureActivities != null) {
            for (Activity activity : mLectureActivities) {
                int day = activity.getDay();
                if (day == -1) return 0;
                lectureDays.add(day);
            }
        }
        if (mStudentGroups == null || (mStudentGroups.size() == 1 && mLectureCount > 0)) {
            activityPenalty += (mLectureCount - lectureDays.size())*10;
        } else {
            for (int i = 0; i < mStudentGroups.size(); i++) {
                days.addAll(lectureDays);
                if (mGroupActivities != null) {
                    for (Activity activity : mGroupActivities.get(i)) {
                        int day = activity.getDay();
                        if (day == -1) return 0;
                        days.add(day);
                    }
                    activityPenalty += (getTotalActivities() - days.size()) * 10;
                }
                days.clear();
            }
        }

        return activityPenalty;
//        mPenaltyActivities = new HashSet<Activity>();
//        int penalty = 0;
//        if (mGroupActivities != null) {
//            Set<Integer>[] d = new Set[5];
//            for (Activity activity : mLectureActivities) {
//                if (d[activity.getDay()] == null) {
//                    d[activity.getDay()] = new HashSet<Integer>();
//                    d[activity.getDay()].add(activity.mId);
//                } else if (!d[activity.mTimeslot / 4].contains(activity.mId)) {
//                    penalty += 10;
//                    mPenaltyActivities.add(activity);
//                    d[activity.getDay()].add(activity.mId);
//                }
//            }
//        }
//        return penalty;
    }

    public String getCourseId() {
        return mCourseId;
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

    public Set<Activity> getCourseActivities() {
        if (mStudents.size() == 0) return new HashSet<>();
        if (mStudentGroups == null) fillGroups();
        int activityId = 0;
        Set<Activity> allActivities = new HashSet<>();
        if (mGroupActivities == null) {
            mLectureActivities = new HashSet<>();
            mGroupActivities = new HashMap<>();
            for (int i = 0; i < mLectureCount; i++) {
                Activity activity = new Activity(TYPE_ACTIVITY_LECTURE, mStudents, activityId++);
                mLectureActivities.add(activity);
                allActivities.add(activity);
            }
            for (int i = 0; i < mWorkGroupCount; i++) {
                for (int j = 0; j < mStudentGroups.size(); j++) {
                    if (!mGroupActivities.containsKey(j))
                        mGroupActivities.put(j, new HashSet<Activity>());
                    Activity activity = new Activity(TYPE_ACTIVITY_WORKGROUP, mStudentGroups.get(j), activityId);
                    mGroupActivities.get(j).add(activity);
                    allActivities.add(activity);
                }
                activityId++;
            }
            for (int i = 0; i < mPracticumCount; i++) {
                for (int j = 0; j < mStudentGroups.size(); j++) {
                    if (!mGroupActivities.containsKey(j))
                        mGroupActivities.put(j, new HashSet<Activity>());
                    Activity activity = new Activity(TYPE_ACTIVITY_PRACTICAL, mStudentGroups.get(j), activityId);
                    mGroupActivities.get(j).add(activity);
                    allActivities.add(activity);
                }
                activityId++;
            }
            mDayUsed = new boolean[activityId][Constants.DAY_COUNT];
        }
        return allActivities;
    }

    private int determineGroupSize() {
        return mGroupCount;
    }

    public Set<Student> getCourseStudents() {
        return mStudents;
    }

    public ArrayList<Set<Student>> getStudentGroups() {
        return mStudentGroups;
    }

    public class Activity {
        private int mId;
        private int mType;
        private int mTimeslot = -1;
        private Set<Student> mStudents;

        public Activity(int type, Set<Student> students, int id) {
            mId = id;
            mType = type;
            mStudents = students;
        }

        public Set<Student> getStudents() {
            return mStudents;
        }

        public void plan(int timeslot) {
            mTimeslot = timeslot;
            mDayUsed[mId][timeslot /4] = true;
            for (Student student : mStudents) {
                student.setBusy(this, timeslot);
            }
        }

        public int getDay() {
            if (mTimeslot == -1) return -1;
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

        public int getTimeslot() {
            return mTimeslot;
        }
    }
}
