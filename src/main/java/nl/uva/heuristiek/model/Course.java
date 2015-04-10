package nl.uva.heuristiek.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by remco on 07/04/15.
 */
public class Course {

    private static final int TYPE_ACTIVITY_LECTURE = 0;
    private static final int TYPE_ACTIVITY_WORKGROUP = 1;
    private static final int TYPE_ACTIVITY_PRACTICAL = 2;

    private String mName;

    private int mHoorcolleges;
    private int mWorkGroups;
    private int mPractica;

    private int mMaxWerkcollgesStudenten;
    private int mMaxPracticaStudenten;

    private Activity[][] mActivities;

    private Set<Student> mStudents = new HashSet<Student>();


    public Course(String[] csvRow) {
        mName = csvRow[0];
        mHoorcolleges = Integer.parseInt(csvRow[1]);
        mWorkGroups = Integer.parseInt(csvRow[2]);
        mMaxWerkcollgesStudenten = Integer.parseInt(csvRow[3]);
        mPractica = Integer.parseInt(csvRow[4]);
        mMaxPracticaStudenten = Integer.parseInt(csvRow[5]);

        if (mHoorcolleges > 0) {
            mActivities[TYPE_ACTIVITY_LECTURE] = new Activity[mHoorcolleges];
        }
        if (mWorkGroups > 0)
            mActivities[TYPE_ACTIVITY_WORKGROUP] = new Activity[mWorkGroups];
    }

    public void addStudent(Student student) {
        mActivities[TYPE_ACTIVITY_LECTURE][0].addStudent(student);
    }

    public Set<Student> getStudents() {
        return mStudents;
    }

    public class Activity {
        Set<Student> mStudents = new HashSet<Student>();
        int mCapacity;

        public boolean addStudent(Student student) {
            if (mCapacity != -1 && mCapacity == mStudents.size()) return false;
            mStudents.add(student);
            return true;
        }
    }
}
