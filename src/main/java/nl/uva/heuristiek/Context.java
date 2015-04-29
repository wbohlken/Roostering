package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 29/04/15.
 */
public class Context {

    private static SecureRandom sRandom = new SecureRandom();

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
        Arrays.fill(mActivitySlots, -1);
        mSlotsUsed = new HashSet<>(Constants.ROOMSLOT_COUNT);
        for (int i = 0; i < mActivities.size(); i++) {
            Course.Activity activity = mActivities.get(i);
            activity.setIndex(i);
            Set<Student> activityStudents = activity.getStudents();
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

    public int getRoomSlot(int activityIndex) {
        return mActivitySlots[activityIndex];
    }

    public int[] swap() {
        int i1 = sRandom.nextInt(mActivities.size());
        int i2 = sRandom.nextInt(mActivities.size());

        int roomSlot1 = mActivitySlots[i1];
        mActivitySlots[i1] = mActivitySlots[i2];
        mActivitySlots[i2] = roomSlot1;
        return new int[]{i1,i2};
    }

    public void revert(int[] swap) {
        int roomSlot1 = mActivitySlots[swap[1]];
        mActivitySlots[swap[1]] = mActivitySlots[swap[0]];
        mActivitySlots[swap[0]] = roomSlot1;
    }
}
