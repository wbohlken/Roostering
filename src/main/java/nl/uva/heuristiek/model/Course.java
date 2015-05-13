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
    private ArrayList<Integer> mWorkgroupGroups;
    private ArrayList<Integer> mPracticalGroups;


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

    public synchronized void hillclimbStudents(int amount) {
        ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>() {{
            add(mWorkgroupGroups);
            add(mPracticalGroups);
        }};

        // Loop through both workgroups and practical groups
        for (ArrayList<Integer> group : groups) {
            if (group != null && group.size() > 2 && getStudentFitness() > 0) {
                for (int counter = 0; counter < amount; counter++) {
                    int previousFitnessValue = getStudentFitness();

                    int groupIndex1, groupIndex2;
                    groupIndex1 = groupIndex2 = Random.nextInt(group.size() - 1);

                    while (groupIndex2 == groupIndex1) {
                        groupIndex2 = Random.nextInt(group.size() - 1);
                    }

                    groupIndex1 = group.get(groupIndex1);
                    groupIndex2 = group.get(groupIndex2);

                    int index1 = Random.nextInt(mStudentGroups.get(groupIndex1).size() - 1);
                    int index2 = Random.nextInt(mStudentGroups.get(groupIndex2).size() - 1);

                    // Swap students between the two groups
                    mStudentGroups.get(groupIndex1).remove(index1);
                    mStudentGroups.get(groupIndex2).add(index1);

                    mStudentGroups.get(groupIndex2).remove(index2);
                    mStudentGroups.get(groupIndex1).add(index2);

                    int newFitnessValue = getStudentFitness();

                    // If the new fitness value is worse, switch the students back
                    if (newFitnessValue >= previousFitnessValue) {
                        mStudentGroups.get(groupIndex1).remove(index2);
                        mStudentGroups.get(groupIndex2).add(index2);

                        mStudentGroups.get(groupIndex2).remove(index1);
                        mStudentGroups.get(groupIndex1).add(index1);
                    } else {
                        System.out.println(getCourseId() + " (groups: " + mStudentGroups.size() + ") : Student fitness old: " + previousFitnessValue + ", new: " + newFitnessValue);
                    }
                }
            }
        }
    }

    /**
     * Calculates the unique courses the students follow in each student group
     * @return the sum of all unique courses within each group
     */
    private synchronized int getStudentFitness() {
        int studentFitness = 0;

        for (ArrayList<Integer> students : mStudentGroups) {
            HashMap<String, Boolean> courses = new HashMap<>();
            for (int studentId : students) {
                Student student = getContext().getStudent(studentId);
                // FIXME: Why is student sometimes null?
                if (student != null) {
                    for (int activityId : student.getActivitiesSet()) {
                        String courseId = getContext().getActivities().get(activityId).getCourse().getCourseId();
                        courses.put(courseId, true);
                    }
                }
            }

            studentFitness += courses.size();
        }

        return studentFitness;
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
                    mWorkgroupGroups = new ArrayList<>();
                    mPracticalGroups = new ArrayList<>();
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
                                mWorkgroupGroups.add(j);
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
                                mPracticalGroups.add(j);
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

    public int getUniqueActivityCount() {
        int uniqueIds = 0;
        HashMap<Integer, Boolean> activityIds = new HashMap<>();
        Set<Activity> activities = getCourseActivities();
        for (Activity activity : activities) {
            if (!activityIds.containsKey(activity.getId())) {
                uniqueIds++;
                activityIds.put(activity.getId(), true);
            }
        }

        return uniqueIds;
    }

    public int getBonusPoints(Schedule schedule) {
        int bonusPoints = 0;

        Set<Activity> activities = getCourseActivities();
        HashMap<Integer, Set<Activity>> activityIds = new HashMap<>();

        for (Activity activity : activities) {
            if(!activityIds.containsKey(activity.getId())) {
                activityIds.put(activity.getId(), new HashSet<Activity>());
            }
            activityIds.get(activity.getId()).add(activity);
        }
        boolean[] daysUsed = new boolean[5];
        Collection<Set<Activity>> activitySet = activityIds.values();
        for (Set<Activity> activitiesById : activitySet) {
            int timeslot = 4;
            for(Activity  activity : activitiesById) {
                if (activity.getTimeslot(schedule) / 4 < timeslot) {
                    timeslot = activity.getTimeslot(schedule) / 4;
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
