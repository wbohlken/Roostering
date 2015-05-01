package nl.uva.heuristiek.model;

import com.sun.istack.internal.Nullable;
import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public class Schedule extends BaseModel {

    public static final int FLAG_ACTIVITY_SORT_STUDENTS_DESC = 0x0;
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_ASC = 0x1;
    public static final int FLAG_ACTIVITY_SORT_RANDOM = 0x2;

    public static final int FLAG_PLAN_METHOD_CONSTRUCTIVE = 0x0;
    public static final int FLAG_PLAN_METHOD_RANDOM = 0x1 << 2;

    private static final int MASK_ACTIVITY_SORT = 0x3;
    private static final int MASK_PLAN_METHOD = 0x1 << 2;
    private final int mFlags;


    private ScheduleStateListener mListener;
    private Penalty mPenalty = null;
    private Integer[] mTimeslots;

    private Set<Integer> mSlotsUsed;
    private int[] mActivitySlots;

    private static SecureRandom sRandom = new SecureRandom();
    private int mStepActivityIndex = 0;

    public Schedule(Context context, int flags) {
        this(context, flags, null);
    }

    public Schedule(Context context, int flags, @Nullable Integer[] timeslots) {
        super(context);
        context.setSchedule(this);
        mSlotsUsed = new HashSet<>();
        mActivitySlots = new int[context.getActivities().size()];
        Arrays.fill(mActivitySlots, -1);
        mFlags = flags;
        Collections.sort(getActivities(), getActivityComparator(flags & MASK_ACTIVITY_SORT));
        if (timeslots == null) {
            mTimeslots = new Integer[20];
            for (int i = 0; i < mTimeslots.length; i++) {
                mTimeslots[i] = i;
            }
            shuffleArray(mTimeslots);
        } else
            mTimeslots = timeslots;

    }

    public int[] getActivitySlots() {
        return mActivitySlots;
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
        mListener.onStateChanged(getContext(), this);
    }

    public Integer[] getTimeslots() {
        return mTimeslots;
    }

    private void planCourses() {
        final int count = getActivities().size();
        for (int i = 0; i < count; i++) {
            planActivity(getActivities().get(i), i);
            mListener.onStateChanged(getContext(), this);
        }
        mListener.onScheduleComplete(this);

    }

    public void plan() {
        switch (mFlags & MASK_PLAN_METHOD) {
            case FLAG_PLAN_METHOD_RANDOM:
                planRandom();
                break;
            default:
                planCourses();

        }
    }

    public boolean planStep(int size) {
        boolean isComplete;
        switch (mFlags & MASK_PLAN_METHOD) {
            case FLAG_PLAN_METHOD_RANDOM:
                isComplete = !planStepRandom(size);
                return isComplete;
            default:
                isComplete = !planStepCourses(size);
        }
        if (isComplete)
            mListener.onScheduleComplete(this);
        return !isComplete;
    }

    private void planRandom() {
        for (int activityIndex = 0; activityIndex < getActivities().size(); activityIndex++) {
            int i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
            while (isSlotUsed(i))
                i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
            getContext().setActivitySlot(activityIndex, i);
            getActivities().get(activityIndex).plan(i % 20);
        }
    }

    public boolean planStepRandom(int size) {
        for (int steps = 0; steps < size; steps++) {
            try {
                int i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
                while (isSlotUsed(i))
                    i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
                getContext().setActivitySlot(mStepActivityIndex, i);
                getActivities().get(mStepActivityIndex).plan(i % 20);
                mListener.onStateChanged(getContext(), this);
                mStepActivityIndex++;
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }
        return (mStepActivityIndex == getActivities().size());
    }

    public boolean planStepCourses(int size) {
        return false;
    }

    static void shuffleArray(Integer[] ar)
    {
        SecureRandom rnd = new SecureRandom();
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

    private void planActivity(Course.Activity activity, int activityIndex) {
        int room = bestFitRoom(activity.getStudents().size()), timeslotIndex = 0;
        float treshhold = 1;
        while (checkContraints(activity, room, mTimeslots[timeslotIndex]) < treshhold) {
            if (++timeslotIndex == Constants.TIMESLOT_COUNT) {
                if (room + 1 == Constants.ROOM_COUNT) {
                    treshhold -= 0.1f;
                    if (treshhold <= 0)
                        return;
                    room = bestFitRoom(activity.getStudents().size());
                } else {
//                    shuffleArray(randomTimeslots);
                    room++;

                }
                timeslotIndex = 0;
            }
        }
        getContext().setActivitySlot(activityIndex, room*20+mTimeslots[timeslotIndex]);
        activity.plan(mTimeslots[timeslotIndex]);
    }

    private double checkContraints(Course.Activity activity, int room, int timeslot) {
        double value = 1;
        if (!isSlotUsed(room * 20 + timeslot)) {
            if (activity.isDayUsed(timeslot / 4)) value -= 0.5;
            final double studentNegativeValue = 1f / (double)activity.getStudents().size();
            for (Student student : activity.getStudents()) {
                if (!student.isAvailable(timeslot)) {
                    value -= studentNegativeValue;
                }
            }
            return value;
        }
        return 0;
    }

    public Penalty getPenalty(boolean forceCalculation) {
        if (mPenalty == null || forceCalculation) {
            int coursePenalty = 0;
            for (Course course : getCourseMap().values()) {
                coursePenalty += course.getPenalty();
            }
            int studentPenalty = 0;
            for (Student student : getStudents()) {
                studentPenalty += student.getPenalty();
            }
            mPenalty = new Penalty(coursePenalty, studentPenalty);
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

    public void climbHill(int stepSize) {
        for (int i = 0; i < stepSize; i++) {
            mPenalty = null;
            Penalty oldPenalty = getPenalty(false);
            int[] swap = swap();
            mPenalty = null;
            Penalty newPenalty = getPenalty(false);
            if (newPenalty.getTotal() > oldPenalty.getTotal()) {
                swap(swap);
            }
            System.out.printf("Old: %s, New: %s\n", oldPenalty.toString(), newPenalty.toString());
            mListener.onScheduleComplete(this);
        }
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
        final int activitiesSize = getActivities().size();
        return swap(new int[]{sRandom.nextInt(activitiesSize), sRandom.nextInt(activitiesSize)});

    }

    public int[] swap(int[] indices) {
        int roomSlot1 = mActivitySlots[indices[1]];
        mActivitySlots[indices[1]] = mActivitySlots[indices[0]];
        mActivitySlots[indices[0]] = roomSlot1;
        return new int[]{indices[1],indices[0]};
    }

    public interface ScheduleStateListener {
        void onStateChanged(Context context, Schedule schedule);
        void onScheduleComplete(Schedule schedule);
    }

}
