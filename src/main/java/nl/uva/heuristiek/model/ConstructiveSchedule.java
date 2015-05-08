package nl.uva.heuristiek.model;

import com.sun.istack.internal.Nullable;
import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.Util;

/**
 * Created by remco on 01/05/15.
 */
public class ConstructiveSchedule extends Schedule {

    protected Integer[] mRandomTimeslots;

    public ConstructiveSchedule(Context context, int flags, @Nullable Integer[] timeslots, ScheduleStateListener listener) {
        super(context, flags, listener);
        if (timeslots == null) {
            mRandomTimeslots = new Integer[20];
            for (int i = 0; i < mRandomTimeslots.length; i++) {
                mRandomTimeslots[i] = i;
            }
            Util.shuffleArray(mRandomTimeslots);
        } else
            mRandomTimeslots = timeslots;
    }

    @Override
    protected int doStep(int activityIndex) {
        Course.Activity activity = getActivities().get(activityIndex);
        int room = Util.bestFitRoom(activity.getStudents().size()), timeslotIndex = 0;
        float treshhold = 1;
        while (checkContraints(activity, room, mRandomTimeslots[timeslotIndex]) < treshhold) {
            if (++timeslotIndex == Constants.TIMESLOT_COUNT) {
                if (room + 1 == Constants.ROOM_COUNT) {
                    treshhold -= 0.1f;
                    if (treshhold <= 0)
                        return -1;
                    room = Util.bestFitRoom(activity.getStudents().size());
                } else {
//                    shuffleArray(randomTimeslots);
                    room++;

                }
                timeslotIndex = 0;
            }
        }
        setActivitySlot(activityIndex, room * 20 + mRandomTimeslots[timeslotIndex]);
        activity.plan(mRandomTimeslots[timeslotIndex], this);
        return activityIndex;
    }

    private double checkContraints(Course.Activity activity, int room, int timeslot) {
        double value = 1;
        if (!isSlotUsed(room * 20 + timeslot)) {
            if (activity.isDayUsed(timeslot / 4)) value -= 0.5;
            final double studentNegativeValue = 1f / (double)activity.getStudents().size();
            for (Integer student : activity.getStudents()) {
                if (!getContext().getStudent(student).isAvailable(timeslot, this)) {
                    value -= studentNegativeValue;
                }
            }
            return value;
        }
        return 0;
    }

}
