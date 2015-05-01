package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.model.Student;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 29/04/15.
 */
public class Context {

    private Map<String, Course> mCourseMap;
    private ArrayList<Student> mStudents;
    private ArrayList<Course.Activity> mActivities;
    private Schedule mSchedule;

    public void init(Map<String, Course> courseMap, ArrayList<Student> students) {
        mCourseMap = courseMap;
        mStudents = students;
        mActivities = new ArrayList<>(courseMap.size()*3);
        for (Course course : courseMap.values()) {
            mActivities.addAll(course.getCourseActivities());
        }
        for (int i = 0; i < mActivities.size(); i++) {
            Course.Activity activity = mActivities.get(i);
            activity.setIndex(i);
            Set<Student> activityStudents = activity.getStudents();
            for (Student student : activityStudents) {
                student.addActivity(i);
            }
        }
    }

    public void setSchedule(Schedule schedule) {
        mSchedule = schedule;
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
        return mSchedule.getActivitySlots();
    }

    public void setActivitySlot(int position, int roomSlot) {
        mSchedule.setActivitySlot(position, roomSlot);

    }

    public int getRoomSlot(int activityIndex) {
        return mSchedule.getRoomSlot(activityIndex);
    }
}
