package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by remco on 08/04/15.
 */
public class Schedule {
    private final ScheduleStateListener mListener;
    private Course.Activity[][][] mCourses;

    int mActivitiesPlanned = 0;

    public Schedule(ScheduleStateListener listener) {
        mCourses = new Course.Activity[Constants.ROOM_COUNT][Constants.DAY_COUNT][Constants.INDEX_COUNT];
        mListener = listener;
        listener.onStateCreated(mCourses);
    }

    public void planCourses(final LinkedList<Course.Activity> activities) {
        new Thread(new Runnable() {
            public void run() {
                int totalActivities = 0;
                for (Course.Activity activity: activities) {
                    planActivity(activity);
                    mListener.onStateChanged();
                }
                System.out.println(String.format("Total activities to plan: %d", totalActivities));
                System.out.println(String.format("Activities planned: %d", mActivitiesPlanned));
            }
        }).start();

    }

    private int bestFitRoom(final int students) {
        for (int i = 0; i < Constants.ROOM_COUNT; i++) {
            if (students < Constants.ROOM_CAPACATIES[i]) return i;
        }
        throw new RuntimeException("No suitable room found");
    }

    private void planActivity(Course.Activity activity) {
        SecureRandom secureRandom = new SecureRandom();
        int room = bestFitRoom(activity.getStudents().size()), day, index;
        int cap = 0;
        do {

            day = secureRandom.nextInt(Constants.DAY_COUNT);
            index = secureRandom.nextInt(Constants.INDEX_COUNT);
            if (cap++ > 5400) break;

        } while (checkContraints(activity, room, day, index) != 1);
        mCourses[room][day][index] = activity;
        activity.plan(room, day, index);
        if (cap < 5400) mActivitiesPlanned++;
        return;
//        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
//            if (Constants.ROOM_CAPACATIES[room] < activity.getStudents().size()) continue;
//            for (int day = 0; day < Constants.DAY_COUNT; day++) {
//                for (int index = 0; index < Constants.INDEX_COUNT; index++) {
//                    if (checkContraints(activity, room,day, index)) {
//                        mCourses[room][day][index] = activity;
//                        activity.plan(room, day, index);
//                        mActivitiesPlanned++;
//                        return;
//                    }
//                }
//            }
//        }
    }

    private double checkContraints(Course.Activity activity, int room, int day, int i) {
        double value = 1;
        if (mCourses[room][day][i] == null) {
            if (activity.isDayUsed(day)) value -= 0.5;
            final double studentNegativeValue = 0.5 / (double)activity.getStudents().size();
            for (Student student : activity.getStudents()) {
                if (!student.isAvailable(day, i, activity.getCourse())) {
                    value -= studentNegativeValue;
                }
            }
            return value;
        }
        return 0;
    }

    public Course.Activity[][] forRoom(int room) {
        return mCourses[room];
    }

    public interface ScheduleStateListener {
        void onStateCreated(Course.Activity[][][] state);
        void onStateChanged();
    }

}
