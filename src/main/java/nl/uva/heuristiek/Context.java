package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by remco on 29/04/15.
 */
public class Context {
    private Map<String, Course> mCourseMap;
    private ArrayList<Student> mStudents;
    private ArrayList<Course.Activity> mActivities;
    private Set<Integer> mSlotsUsed;
    private int[] mActivitySlots;

    public void init(Map<String, Course> courseMap, ArrayList<Student> students) {
        mCourseMap = courseMap;
        mStudents = students;
        mActivities = new ArrayList<>(courseMap.size()*3);
        for (Course course : courseMap.values()) {
            mActivities.addAll(course.getCourseActivities());
        }
        mActivitySlots = new int[mActivities.size()];
        mSlotsUsed = new HashSet<>(Constants.ROOMSLOT_COUNT);
        for (int i = 0; i < mActivities.size(); i++) {
            Set<Student> activityStudents = mActivities.get(i).getStudents();
            for (Student student : activityStudents) {
                student.addActivity(i);
            }
        }
    }

    public Map<String, Course> getCourseMap() {
        return mCourseMap;
    }

    public ArrayList<Student> getStudents() {
        return mStudents;
    }

    public ArrayList<Course.Activity> getActivities() {
        return mActivities;
    }

    public int[] getActivitySlots() {
        return mActivitySlots;
    }

    public void setActivitySlot(int position, int roomSlot) {
        mActivitySlots[position] = roomSlot;
        mSlotsUsed.add(roomSlot);
    }

    public boolean isSlotUsed(int roomSlot) {
        return mSlotsUsed.contains(roomSlot);
    }
}
