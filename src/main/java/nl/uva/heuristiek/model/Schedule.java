package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;

import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public class Schedule {
    private final ScheduleStateListener mListener;
    private Course.Activity[] mCourses;
    private int mPenalty;
    private Set<Course.Activity> mPenaltyActivities = new HashSet<Course.Activity>();

    int mActivitiesPlanned = 0;

    public Schedule(ScheduleStateListener listener) {
        mCourses = new Course.Activity[Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT];
        mListener = listener;
        listener.onStateCreated(mCourses);
    }

    public void planCourses(final Collection<Course> courses, final LinkedList<Course.Activity> activities) {
        int[] timeSlots = new int[20];
        for (int i = 0; i < timeSlots.length; i++) {
            timeSlots[i] = i;
        }
        shuffleArray(timeSlots);
        for (Course.Activity activity : activities) {
            planActivity(activity, timeSlots);
            mListener.onStateChanged();
        }
        for (Course course : courses) {
            mPenalty += course.getPenalty();
        }
//                System.out.println(String.format("Total activities to plan: %d", activities.size()));
//
//                System.out.println(String.format("Activities planned: %d", mActivitiesPlanned));
//                System.out.println(String.format("Total penalty: %d", mPenalty));
        mListener.onScheduleComplete(mPenalty, activities.size(), mActivitiesPlanned);

    }

    static void shuffleArray(int[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private int bestFitRoom(final int students) {
        for (int i = 0; i < Constants.ROOM_COUNT; i++) {
            if (students < Constants.ROOM_CAPACATIES[i]) return i;
        }
        throw new RuntimeException("No suitable room found");
    }

    private void planActivity(Course.Activity activity, int[] randomTimeslots) {
        int room = bestFitRoom(activity.getStudents().size()), timeslotIndex = 0;
        float treshhold = 1;
        while (checkContraints(activity, room, randomTimeslots[timeslotIndex]) < treshhold) {
            if (++timeslotIndex == Constants.TIMESLOT_COUNT) {
                if (room + 1 == Constants.ROOM_COUNT) {
                    treshhold -= 0.1f;
                    if (treshhold <= 0)
                        return;
                    room = bestFitRoom(activity.getStudents().size());
                } else {
                    room++;
                }
                timeslotIndex = 0;
            }
        }
        mCourses[room*20+randomTimeslots[timeslotIndex]] = activity;
        int penalty = activity.plan(randomTimeslots[timeslotIndex]);
        mPenalty += penalty;
        if (penalty > 0)
            mPenaltyActivities.addAll(activity.getCourse().getPanaltyActivities());

        mActivitiesPlanned++;
    }

    public Set<Course.Activity> getPenaltyActivities() {
        return mPenaltyActivities;
    }

    private double checkContraints(Course.Activity activity, int room, int timeslot) {
        double value = 1;
        if (mCourses[room*20+timeslot] == null) {
            if (activity.isDayUsed(timeslot / 4)) value -= 0.5;
            final double studentNegativeValue = value / (double)activity.getStudents().size();
            for (Student student : activity.getStudents()) {
                if (!student.isAvailable(timeslot)) {
                    value -= studentNegativeValue;
                }
            }
            return value;
        }
        return 0;
    }

    public int getPenalty() {
        return mPenalty;
    }

    public int[] getRoomOccupation() {
        int[] roomOccupation = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < (Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT); i++) {
            if (mCourses[i] != null) {
                roomOccupation[i / 20]++;
            }
        }

        int[] roomOccupationPercentage = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < Constants.ROOM_COUNT; i++) {
            roomOccupationPercentage[i] = roomOccupation[i] * 100 / 20;
        }

        return roomOccupationPercentage;
    }

    public int[] getSeatOccupationPerRoom() {
        int[] totalSeatOccupation = new int[Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT];
        int[] seatOccupation = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < (Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT); i++) {
            totalSeatOccupation[i] = 0;
            int roomCapacity = Constants.ROOM_CAPACATIES[i/20];
            if (mCourses[i] != null) {
                totalSeatOccupation[i] = 100 * mCourses[i].getStudents().size() / roomCapacity;
            }
        }

        for (int i = 0; i < totalSeatOccupation.length; i++) {
            seatOccupation[i/20] += totalSeatOccupation[i];
        }

        for (int i = 0; i < seatOccupation.length; i++) {
            seatOccupation[i] = seatOccupation[i] / 20;
        }

        return seatOccupation;
    }

    public Course.Activity[] getActivities() {
        return mCourses;
    }

    public interface ScheduleStateListener {
        void onStateCreated(Course.Activity[] state);
        void onStateChanged();
        void onScheduleComplete(int penalty, int totalActivities, int plannedActivities);
    }

}
