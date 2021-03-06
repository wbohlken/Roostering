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
    private Set<Student> mStudents = new HashSet<>();
    private Set<Activity> mLectureActivities;
    private ArrayList<Set<Student>> mStudentGroups;
    private Map<Integer, Set<Activity>> mGroupActivities;
    private Set<Activity> mAllActivities;
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

    private int getTotalActivities() {
        return mLectureCount+mWorkGroupCount+mPracticumCount;
    }


    public int getPenalty() {
        int activityPenalty = 0;
        HashSet<Integer> lectureDays = new HashSet<>(5);
        HashSet<Integer> days = new HashSet<>();
        if (mLectureActivities != null) {
            for (Activity activity : mLectureActivities) {
                lectureDays.add(activity.getDay());
            }
        }
        if (mStudentGroups == null || (mStudentGroups.size() == 1 && mLectureCount > 0)) {
            activityPenalty += (mLectureCount - lectureDays.size())*10;
        } else {
            for (int i = 0; i < mStudentGroups.size(); i++) {
                days.addAll(lectureDays);
                if (mGroupActivities != null) {
                    for (Activity activity : mGroupActivities.get(i))
                        days.add(activity.getDay());
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

    public int getUniqueActivityCount() {
        int uniqueIds = 0;
        HashMap<Integer, Boolean> activityIds = new HashMap<>();
        Set<Activity> activities = getActivities();
        for (Activity activity : activities) {
            if (!activityIds.containsKey(activity.getId())) {
                uniqueIds++;
                activityIds.put(activity.getId(), true);
            }
        }

        return uniqueIds;
    }

    public Set<Activity> getActivities() {
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
        return mAllActivities;
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

        public void plan(int timeslot) {
            mTimeslot = timeslot;
            mDayUsed[mId][timeslot /4] = true;
            for (Student student : mStudents) {
                student.setBusy(this, timeslot);
            }
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

        public int getTimeslot() {
            return mTimeslot;
        }
    }

    public int getBonusPoints() {
        int bonusPoints = 0;

        Set<Activity> activities = getActivities();
        HashMap<Integer, Set<Activity>> activityIds = new HashMap<>();

        for (Activity activity : activities) {
            if(!activityIds.containsKey(activity.getId())) {
                activityIds.put(activity.getId(), new HashSet<>());
            }
            activityIds.get(activity.getId()).add(activity);
        }
        boolean[] daysUsed = new boolean[5];
        Collection<Set<Activity>> activitySet = activityIds.values();
        for (Set<Activity> activitiesById : activitySet) {
            int timeslot = 4;
            for(Activity  activity : activitiesById) {
                if (activity.getTimeslot() / 4 < timeslot) {
                    timeslot = activity.getTimeslot() / 4;
                }
            }
            daysUsed[timeslot] = true;
//            System.out.println(this.getName() + " " + timeslot);
        }


        switch (getUniqueActivityCount()) {
            case 2:
                if ((daysUsed[0] && daysUsed[3]) || (daysUsed[1] && daysUsed[4])) {
                    bonusPoints = 20;
                }
                break;

            case 3:
                if (daysUsed[0] && daysUsed[2] && daysUsed[4]) {
                    bonusPoints = 20;
                }
                break;

            case 4:
                if (daysUsed[0] && daysUsed[1] && daysUsed[3] && daysUsed[4]) {
                    bonusPoints = 20;
                }
                break;
            case 5:
                if (daysUsed[0] && daysUsed[1] && daysUsed[2] && daysUsed[3] && daysUsed[4]) {
                    bonusPoints = 20;
                }
                break;
        }

        return bonusPoints;
    }
}
