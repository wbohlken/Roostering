package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Context;

/**
 * Created by remco on 08/05/15.
 */
public class StaticSchedule extends Schedule {

    public StaticSchedule(Context context, int[] activitySlots) {
        super(context, activitySlots);
    }

    @Override
    protected int doStep(int activityIndex) {
        throw new UnsupportedOperationException();
    }
}
