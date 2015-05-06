package nl.uva.heuristiek.model;

import com.sun.istack.internal.Nullable;
import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;

/**
 * Created by remco on 01/05/15.
 */
public class RandomSchedule extends Schedule {

    public RandomSchedule(Context context, int flags, @Nullable Integer[] timeslots) {
        super(context, flags);
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
