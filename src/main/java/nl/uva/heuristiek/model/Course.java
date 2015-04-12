package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Util;

import java.util.*;

/**
 * Created by remco on 07/04/15.
 */
public class Course {

    private static final int TYPE_ACTIVITY_LECTURE = 0;
    private static final int TYPE_ACTIVITY_WORKGROUP = 1;
    private static final int TYPE_ACTIVITY_PRACTICAL = 2;

    private String mName;

    private int mLectureCount;
    private int mWorkGroupCount;
    private int mPracticumCount;

    private int mGroupCount;

    private Set<Student> mStudents = new HashSet<Student>();
    private ArrayList<Set<Student>> mStudentGroups;
    private int mCourseId;
    private Set<Activity> mActivities;
    private boolean[] mDayUsed = new boolean[Constants.DAY_COUNT];


    public Course(int courseId, String[] csvRow) {
        mCourseId = courseId;
        mName = csvRow[0];
        mLectureCount = Util.tryParseInt(csvRow[1]);
        mWorkGroupCount = Util.tryParseInt(csvRow[2]);
        int workGroupCapacity = Util.tryParseInt(csvRow[3]);
        mPracticumCount = Util.tryParseInt(csvRow[4]);
        int practicumCapacity = Util.tryParseInt(csvRow[5]);
        mGroupCount = Math.max(workGroupCapacity, practicumCapacity);
    }

    public int getCourseId() {
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
        if (mStudentGroups == null) fillGroups();
        if (mActivities == null) {
            mActivities = new HashSet<Activity>();
            for (int i = 0; i < mLectureCount; i++) {
                mActivities.add(new Activity(TYPE_ACTIVITY_LECTURE, mStudents));
            }
            for (int i = 0; i < mWorkGroupCount; i++) {
                for (Set<Student> students : mStudentGroups)
                    mActivities.add(new Activity(TYPE_ACTIVITY_WORKGROUP, students));
            }
            for (int i = 0; i < mPracticumCount; i++) {
                for (Set<Student> students : mStudentGroups)
                    mActivities.add(new Activity(TYPE_ACTIVITY_PRACTICAL, students));
            }
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
        private int mType;
        private Set<Student> mStudents;

        public Activity(int type, Set<Student> students) {
            mType = type;
            mStudents = students;
        }

        public Set<Student> getStudents() {
            return mStudents;
        }

        public void plan(int room, int day, int index) {
            mDayUsed[day] = true;
            for (Student student : mStudents) {
                student.setBusy(day, index, Course.this);
            }
        }

        public String getName() {
            return mName;
        }

        boolean isDayUsed(int day) {
            return mDayUsed[day];
        }

        public Course getCourse() {
            return Course.this;
        }
    }
}
