package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

import java.util.Collection;

/**
 * Created by remco on 08/04/15.
 */
public class Schedule {
    private Course[][][] mCourses;

    public Schedule(Collection<Course> vakken) {
        mCourses = new Course[Constants.ROOM_COUNT][Constants.DAY_COUNT][Constants.INDEX_COUNT];
        for (Course course : vakken) {
            for (int room = 0; room < Constants.ROOM_COUNT; room++) {
                for (int day = 0; day < Constants.DAY_COUNT; day++) {
                    for (int i = 0; i < Constants.INDEX_COUNT; i++) {
                        if (checkContraints(course, room, day, i)) {
                            mCourses[room][day][i] = course;
                            for (Student student : course.getStudents()) {
                                student.setBusy(day, i, true);
                            }
                            room = Constants.ROOM_COUNT;
                            day = Constants.DAY_COUNT;
                            i = Constants.INDEX_COUNT;
                        }
                    }
                }
            }
        }
    }

    private boolean checkContraints(Course course, int room, int day, int i) {
        if (mCourses[room][day][i] == null) {
            for (Student student : course.getStudents()) {
                if (!student.isAvailable(day, i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
