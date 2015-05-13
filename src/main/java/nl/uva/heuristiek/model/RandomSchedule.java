package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;

/**
 * Created by remco on 01/05/15.
 */
public class RandomSchedule extends Schedule {

    public RandomSchedule(Context context, int flags, ScheduleStateListener listener) {
        super(context, listener);
    }

    @Override
    protected int doStep(int activityIndex) {
        int i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
        while (isSlotUsed(i))
            i = sRandom.nextInt(Constants.ROOMSLOT_COUNT);
        setActivitySlot(activityIndex, i);
        getActivities().get(activityIndex).plan(i % 20, this);
        return activityIndex;
    }
}
