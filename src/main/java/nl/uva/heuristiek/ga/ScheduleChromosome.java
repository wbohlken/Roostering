package nl.uva.heuristiek.ga;

import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.*;

/**
 * Created by remco on 08/05/15.
 */
public class ScheduleChromosome extends BaseModel implements Chromosome {

    private Schedule mSchedule;
    private Penalty mPenalty;

    public ScheduleChromosome(Context context) {
        super(context);
        mSchedule = new RandomSchedule(context, 0, null);
        mSchedule.doSteps(getActivities().size());
    }

    public ScheduleChromosome(Context context, int[] activitySlots) {
        super(context);
        mSchedule = new StaticSchedule(context, activitySlots);
    }

    @Override
    public Penalty getPenalty() {
        if (mPenalty == null)
            evaluate();
        return mPenalty;
    }

    @Override
    public void evaluate() {
        mPenalty = mSchedule.getPenalty(false);
    }

    public Schedule getSchedule() {
        return mSchedule;
    }

    @Override
    public int compareTo(Chromosome o) {
        return Integer.compare(getPenalty().getTotal(), o.getPenalty().getTotal());
    }
}
