package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.algorithm.HillClimber;
import nl.uva.heuristiek.util.*;
import nl.uva.heuristiek.util.Random;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public abstract class Schedule extends BaseModel implements HillClimber.Target {



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
    private final Object sPenaltyLock = new Object();
    private Set<Integer> mSlotsUsed;

    private int[] mActivitySlots;
    private int mActivitiesPlanned = 0;

    public Schedule(Context context, ScheduleStateListener listener) {
        super(context);
        mListener = listener;
        mSlotsUsed = new HashSet<>();
        mActivitySlots = new int[context.getActivities().size()];
        Arrays.fill(mActivitySlots, -1);
    }

    public Schedule(Context context, int[] acitivitySlots) {
        super(context);
        mActivitySlots = acitivitySlots;
        mActivitiesPlanned = mActivitySlots.length;
    }

    public int[] getActivitySlots() {
        return mActivitySlots;
    }

    @Override
    public int getFitness() {
        int fitness = 0;
        if (mActivitiesPlanned == getActivities().size()) fitness += 2000;
        fitness -= getPenalty().getTotal();
        fitness += getBonus();
        return fitness;
    }

    private int getBonus() {
        int bonus = 0;
        for (Course course : getCourseMap().values()) {
            bonus += course.getBonusPoints(this);
        }
        return bonus;
    }



    public void plan() {
        for (int activityIndex = 0; activityIndex < getActivities().size(); activityIndex++) {
            doStep(activityIndex);
            if (mListener != null) {
                mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
                mListener.redraw(this, false);
            }
        }
        if (mListener != null)
            mListener.redraw(this, true);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mActivitySlots);
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
                if (mListener != null)
                    mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
                if (mListener != null && (steps < 1000 || step % 1000 == 0))
                    mListener.redraw(this, mActivitiesPlanned == getActivities().size());
            }
            if (steps > 0) {
                synchronized (sPenaltyLock) {
                    mPenalty = null;
                }
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

                if (chunks.size() > 0) {
                    synchronized (sPenaltyLock) {
                        mPenalty = null;
                    }
                }
                if (mListener != null) {
                    for (Integer activityIndex : chunks)
                        mListener.activityAdded(mActivitySlots[activityIndex], getActivities().get(activityIndex));
                    mListener.redraw(Schedule.this, mActivitiesPlanned == mActivitySlots.length);
                }
            }

            @Override
            protected void done() {
                callback.done(mActivitiesPlanned == getActivities().size());
            }
        }.execute();
    }

    protected abstract int doStep(int activityIndex);

    public Penalty getPenalty() {
        synchronized (sPenaltyLock) {
            if (mPenalty == null) {
                int smallRoomPenalty = 0;
                List<Course.Activity> activities = getActivities();
                int[] usedRoomSlots = new int[Constants.ROOMSLOT_COUNT];
                for (int i = 0; i < activities.size(); i++) {
                    int roomSlot = getRoomSlot(i);
                    if (roomSlot >= 0) {
                        usedRoomSlots[roomSlot]++;
                        int overload = Constants.ROOM_CAPACATIES[roomSlot / 20] - activities.get(i).getStudents().size();
                        if (overload < 0) smallRoomPenalty += overload * -1;
                    }
                }
                int roomSlotPenalty = 0;
                for(int roomSlotUsage : usedRoomSlots) {
                    if (roomSlotUsage > 1)
                    roomSlotPenalty += 100 * (roomSlotUsage - 1);
                }
                int coursePenalty = 0;
                for (Course course : getCourseMap().values()) {
                    coursePenalty += course.getPenalty(this);
                }
                int studentPenalty = 0;
                for (Student student : getStudents().values()) {
                    studentPenalty += student.getPenalty(this);
                }
                mPenalty = new Penalty(coursePenalty, studentPenalty, smallRoomPenalty, roomSlotPenalty);
            }

            return mPenalty;
        }
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
        return new Integer[]{sRandom.nextInt(activitySize), sRandom.nextInt(activitySize)};
    }

    @Override
    public Integer[] swap(Integer[] indices) {
        int roomSlot1 = mActivitySlots[indices[1]];
        mActivitySlots[indices[1]] = mActivitySlots[indices[0]];
        mActivitySlots[indices[0]] = roomSlot1;
        mPenalty = null;
        return new Integer[]{indices[1], indices[0]};
    }

    public boolean isComplete() {
        return false;
    }

    public interface ScheduleStateListener {
        void redraw(Schedule schedule, boolean scheduleComplete);
        void activityAdded(int roomSlot, Course.Activity activity);
    }

    public interface Callback {
        void done(boolean scheduleComplete);
    }

    public enum Type {
        Constructive, Random
    }

}
