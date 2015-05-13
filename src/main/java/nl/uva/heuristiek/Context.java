package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;
import nl.uva.heuristiek.util.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by remco on 29/04/15.
 */
public class Context {

    @SuppressWarnings("unused")
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_DESC = 0x0;
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_ASC = 0x1;
    public static final int FLAG_ACTIVITY_SORT_RANDOM = 0x2;

    private static final int MASK_ACTIVITY_SORT = 0x3;

    private Map<String, Course> mCourseMap;
    private Map<Integer, Student> mStudents;
    private ArrayList<Course.Activity> mActivities;

    public Context() {
    }

    public void init(Map<String, Course> courseMap, Map<Integer, Student> students, ActivitySortMethod sortMethod) {
        mCourseMap = courseMap;
        mStudents = students;
        mActivities = new ArrayList<>(courseMap.size()*3);
        for (Course course : courseMap.values()) {
            mActivities.addAll(course.getCourseActivities());
        }
        Collections.sort(mActivities, sortMethod.getComparator());
        for (int i = 0; i < mActivities.size(); i++) {
            Course.Activity activity = mActivities.get(i);
            activity.setIndex(i);
            for (Integer student : activity.getStudents()) {
                getStudent(student).addActivity(i);
            }
        }
    }

    public void sortActivities(ActivitySortMethod sortMethod) {
        Collections.sort(mActivities, sortMethod.getComparator());
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

    public enum ActivitySortMethod {
        ASCENDING_STUDENT_SIZE {
            @Override
            public Comparator<Course.Activity> getComparator() {
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o1.getStudents().size(), o2.getStudents().size());
                    }
                };
            }
        }, DESCENDING_STUDENT_SIZE {
            @Override
            public Comparator<Course.Activity> getComparator() {
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o2.getStudents().size(), o1.getStudents().size());
                    }
                };
            }
        }, RANDOM {
            @Override
            public Comparator<Course.Activity> getComparator() {
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Random.nextInt(2) - 1;
                    }
                };
            }
        };

        public abstract Comparator<Course.Activity> getComparator();
    }
}
