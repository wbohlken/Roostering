package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.Util;
import nl.uva.heuristiek.util.*;
import nl.uva.heuristiek.util.Random;

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
    private ArrayList<Integer> mStudents = new ArrayList<>();
    private Set<Activity> mLectureActivities;
    private ArrayList<ArrayList<Integer>> mStudentGroups;
    private Map<Integer, Set<Activity>> mGroupActivities;
    private boolean[][] mDayUsed;
    private Set<Activity> mPenaltyActivities;
    private Set<Activity> mAllActivities;


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


    public int getPenalty(Schedule schedule) {
        int activityPenalty = 0;
        HashSet<Integer> lectureDays = new HashSet<>(5);
        HashSet<Integer> days = new HashSet<>();
        if (mLectureActivities != null) {
            for (Activity activity : mLectureActivities) {
                int day = activity.getDay(schedule);
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
                        int day = activity.getDay(schedule);
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
        mStudents.add(student.getId());
    }

    public void fillGroups() {
        mStudentGroups = new ArrayList<>();
        int groupSize = determineGroupSize();
        ArrayList<Integer> currentGroup = new ArrayList<>(groupSize);
        for (Integer student : mStudents) {
            currentGroup.add(student);
            if (currentGroup.size() == groupSize) {
                mStudentGroups.add(currentGroup);
                currentGroup = new ArrayList<>(groupSize);
            }
        }
        if (currentGroup.size() > 0) {
            if (mStudentGroups.size() == 0) {
                mStudentGroups.add(currentGroup);
            } else {
                ArrayList<Integer> lastGroup = mStudentGroups.get(mStudentGroups.size() - 1);
                if (currentGroup.size() + lastGroup.size() < groupSize) {
                    for (Integer student : currentGroup) {
                        lastGroup.add(student);
                    }
                } else {
                    mStudentGroups.add(currentGroup);
                }
            }
        }
    }

    public Set<Activity> getCourseActivities() {
        if (mAllActivities == null) {
            synchronized (this) {
                if (mAllActivities == null) {
                    if (mStudents.size() == 0) return new HashSet<>();
                    if (mStudentGroups == null) fillGroups();
                    int activityId = 0;
                    mAllActivities = new HashSet<>();
                    if (mGroupActivities == null) {
                        mLectureActivities = new HashSet<>();
                        mGroupActivities = new HashMap<>();
                        for (int i = 0; i < mLectureCount; i++) {
                            Activity activity = new Activity(TYPE_ACTIVITY_LECTURE, mStudents, activityId++);
                            mLectureActivities.add(activity);
                            mAllActivities.add(activity);
                        }
                        for (int i = 0; i < mWorkGroupCount; i++) {
                            for (int j = 0; j < mStudentGroups.size(); j++) {
                                if (!mGroupActivities.containsKey(j))
                                    mGroupActivities.put(j, new HashSet<Activity>());
                                Activity activity = new Activity(TYPE_ACTIVITY_WORKGROUP, mStudentGroups.get(j), activityId);
                                mGroupActivities.get(j).add(activity);
                                mAllActivities.add(activity);
                            }
                            activityId++;
                        }
                        for (int i = 0; i < mPracticumCount; i++) {
                            for (int j = 0; j < mStudentGroups.size(); j++) {
                                if (!mGroupActivities.containsKey(j))
                                    mGroupActivities.put(j, new HashSet<Activity>());
                                Activity activity = new Activity(TYPE_ACTIVITY_PRACTICAL, mStudentGroups.get(j), activityId);
                                mGroupActivities.get(j).add(activity);
                                mAllActivities.add(activity);
                            }
                            activityId++;
                        }
                        mDayUsed = new boolean[activityId][Constants.DAY_COUNT];
                    }
                }
            }
        }
        return mAllActivities;
    }

    private int determineGroupSize() {
        return mGroupCount;
    }

    public ArrayList<Integer> getCourseStudents() {
        return mStudents;
    }

    public ArrayList<ArrayList<Integer>> getStudentGroups() {
        return mStudentGroups;
    }

    public class Activity {
        private int mId;
        private int mType;
        private ArrayList<Integer> mStudents;
        private int mIndex;

        public Activity(int type, ArrayList<Integer> students, int id) {
            mId = id;
            mType = type;
            mStudents = students;
        }

        public ArrayList<Integer> getStudents() {
            return mStudents;
        }

        public void plan(int timeslot, Schedule schedule) {
            mDayUsed[mId][getTimeslot(schedule) / 4] = true;
        }

        public int getDay(Schedule schedule) {
            int timeSlot = getRoomSlot(schedule);
            if (timeSlot == -1) return -1;
            return timeSlot%20/4;
        }

        public int getRoomSlot(Schedule schedule) {
            return schedule.getActivitySlots()[mIndex];
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

        public int getTimeslot(Schedule schedule) {
            return getRoomSlot(schedule) % 20;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }
    }
}
