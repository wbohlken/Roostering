package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.algorithm.HillClimber;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public abstract class Schedule extends BaseModel implements HillClimber.Target {

    @SuppressWarnings("unused")
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_DESC = 0x0;
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_ASC = 0x1;
    public static final int FLAG_ACTIVITY_SORT_RANDOM = 0x2;

    public static final int FLAG_PLAN_METHOD_CONSTRUCTIVE = 0x0;
    public static final int FLAG_PLAN_METHOD_RANDOM = 0x1 << 2;

    public static final int FLAG_ITERATIVE_METHOD_HILLCLIMBER = 0x0;
    public static final int FLAG_ITERATIVE_METHOD_SIMANN = 0x1 << 5;

    private static final int MASK_ACTIVITY_SORT = 0x3;
    private static final int MASK_PLAN_METHOD = 0x1 << 2;
    private static final int MASK_INTERATIVE_METHOD = 0x3 << 5;
    protected static SecureRandom sRandom = new SecureRandom();

    protected ScheduleStateListener mListener;
    private Penalty mPenalty = null;
    private Set<Integer> mSlotsUsed;

    private int[] mActivitySlots;
    private int mActivitiesPlanned = 0;

    public Schedule(Context context, int flags, ScheduleStateListener listener) {
        super(context);
        mListener = listener;
        mSlotsUsed = new HashSet<>();
        mActivitySlots = new int[context.getActivities().size()];
        Arrays.fill(mActivitySlots, -1);
        Collections.sort(getActivities(), getActivityComparator(flags & MASK_ACTIVITY_SORT));
    }

    public int[] getActivitySlots() {
        return mActivitySlots;
    }

    @Override
    public int getFitness() {
        int fitness = 0;
        if (mActivitiesPlanned == getActivities().size()) fitness += 2000;
        fitness -= getPenalty(true).getTotal();
        fitness += getBonus();
        return fitness;
    }

    private int getBonus() {
        return 0;
    }

    private Comparator<Course.Activity> getActivityComparator(int activitySortFlag) {

        switch (activitySortFlag) {
            case FLAG_ACTIVITY_SORT_RANDOM:
                final Random random = new Random();
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return random.nextInt(2) - 1;
                    }
                };
            case FLAG_ACTIVITY_SORT_STUDENTS_ASC:
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o1.getStudents().size(), o2.getStudents().size());
                    }
                };
            default:
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o2.getStudents().size(), o1.getStudents().size());
                    }
                };
        }

    }

    public void setListener(ScheduleStateListener listener) {
        mListener = listener;
        mListener.redraw(this, false);
    }

    public void plan() {
        for (int activityIndex = 0; activityIndex < getActivities().size(); activityIndex++) {
            doStep(activityIndex);
            mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
            mListener.redraw(this, false);
        }
        mListener.redraw(this, true);
    }

    /**
     *
     * @param steps
     * @return true if there are still steps left
     */
    public boolean doSteps(int steps) {
        try {
            for (int step = 0; step < steps; step++) {
                int activityIndex = doStep(mActivitiesPlanned++);
                mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
                if (steps < 1000 || step % 1000 == 0)
                    mListener.redraw(this, mActivitiesPlanned == getActivities().size());
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return (mActivitiesPlanned == getActivities().size());
    }

    public void doSteps(final int steps, final Callback callback) {
        new SwingWorker<Boolean, Integer>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                for (int step = 0; step < steps && mActivitiesPlanned < mActivitySlots.length; step++) {
                    if (mActivitiesPlanned == getActivities().size()) break;
                    int activityIndex = doStep(mActivitiesPlanned++);
                    publish(activityIndex);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                for (Integer activityIndex : chunks)
                    mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
                mListener.redraw(Schedule.this, mActivitiesPlanned == mActivitySlots.length);
            }

            @Override
            protected void done() {
                callback.done(mActivitiesPlanned == getActivities().size());
            }
        }.execute();
    }

    protected abstract int doStep(int activityIndex);

    public synchronized Penalty getPenalty(boolean forceCalculation) {
        if (mPenalty == null || forceCalculation) {
            synchronized (this) {
                if (mPenalty == null || forceCalculation) {
                    int smallRoomPenalty = 0;
                    List<Course.Activity> activities = getActivities();
                    for (int i = 0; i < activities.size(); i++) {
                        int roomSlot = getRoomSlot(i);
                        int overload = Constants.ROOM_CAPACATIES[roomSlot/20] - activities.get(i).getStudents().size();
                        if (overload < 0) smallRoomPenalty += overload*-1;
                    }
                    int coursePenalty = 0;
                    for (Course course : getCourseMap().values()) {
                        coursePenalty += course.getPenalty(this);
                    }
                    int studentPenalty = 0;
                    for (Student student : getStudents().values()) {
                        studentPenalty += student.getPenalty(this);
                    }
                    mPenalty = new Penalty(coursePenalty, studentPenalty, smallRoomPenalty);
                }
            }
        }
        return mPenalty;
    }

    public int[] getRoomOccupation() {
        int[] roomOccupation = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < (Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT); i++) {
//TODO            if (mSchedule[i] != null) {
//                roomOccupation[i / 20]++;
//            }
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
//       TODO     if (mSchedule[i] != null) {
//                totalSeatOccupation[i] = 100 * mSchedule[i].getStudents().size() / roomCapacity;
//            }
        }

        for (int i = 0; i < totalSeatOccupation.length; i++) {
            seatOccupation[i/20] += totalSeatOccupation[i];
        }

        for (int i = 0; i < seatOccupation.length; i++) {
            seatOccupation[i] = seatOccupation[i] / 20;
        }

        return seatOccupation;
    }

//    public void climbHill(int stepSize) {
//        for (int i = 0; i < stepSize; i++) {
//            Penalty oldPenalty = getPenalty(true);
//            int[] swap = swap();
//            mPenalty = null;
//            Penalty newPenalty = getPenalty(true);
//            if (!accept(newPenalty.getTotal(), oldPenalty.getTotal())) {
//                swap(swap);
//            } else {
//                mListener.activityAdded(mActivitySlots[swap[0]], getActivities().get(swap[0]));
//                mListener.activityAdded(mActivitySlots[swap[1]], getActivities().get(swap[1]));
//            }
//
////            System.out.printf("Old: %s, New: %s\n", oldPenalty.toString(), newPenalty.toString());
//            mListener.redraw(this, true);
//        }
//    }



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

    @Override
    public Integer[] generateRandomSwap() {
        final int activitySize = getActivities().size();
        return new Integer[]{sRandom.nextInt(activitySize),sRandom.nextInt(activitySize)};
    }

    @Override
    public Integer[] swap(Integer[] indices) {
        int roomSlot1 = mActivitySlots[indices[1]];
        mActivitySlots[indices[1]] = mActivitySlots[indices[0]];
        mActivitySlots[indices[0]] = roomSlot1;
        return new Integer[]{indices[1],indices[0]};
    }

    public boolean isComplete() {
        return false;
    }

    public interface ScheduleStateListener {
        void redraw(Schedule schedule, boolean scheduleComplete);

        void activityAdded(int roomSlot, Course.Activity activity);
        void removeActivity(int roomSlot);
    }

    public interface Callback {
        void done(boolean scheduleComplete);
    }

}
