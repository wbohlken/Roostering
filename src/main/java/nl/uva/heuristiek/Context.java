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
    private Map<Integer, Student> mStudents;
    private ArrayList<Course.Activity> mActivities;

    public Context() {
    }

    public void init(Map<String, Course> courseMap, Map<Integer, Student> students) {
        mCourseMap = courseMap;
        mStudents = students;
        mActivities = new ArrayList<>(courseMap.size()*3);
        for (Course course : courseMap.values()) {
            mActivities.addAll(course.getCourseActivities());
        }
        for (int i = 0; i < mActivities.size(); i++) {
            Course.Activity activity = mActivities.get(i);
            activity.setIndex(i);
            for (Integer student : activity.getStudents()) {
                getStudent(student).addActivity(i);
            }
        }
    }

    public Map<String, Course> getCourseMap() {
        return mCourseMap;
    }

    public Map<Integer, Student> getStudents() {
        return mStudents;
    }

    public ArrayList<Course.Activity> getActivities() {
        return mActivities;
    }

    public Student getStudent(int studentId) {
        return mStudents.get(studentId);
    }
}
