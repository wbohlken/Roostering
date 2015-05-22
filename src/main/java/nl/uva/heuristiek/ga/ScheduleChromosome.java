package nl.uva.heuristiek.ga;

import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.*;

/**
 * Created by remco on 08/05/15.
 */
public class ScheduleChromosome extends BaseModel implements Chromosome {

    private Schedule mSchedule;
    private Integer mFitness;

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
    public void evaluate() {
        if (mFitness == null)
            mFitness = mSchedule.getFitness();
    }

    @Override
    public int getFitness() {
        evaluate();
        return mFitness;
    }

    public Schedule getSchedule() {
        return mSchedule;
    }

    @Override
    public int compareTo(Chromosome o) {
        return Integer.compare(o.getFitness(), getFitness());
    }
}
