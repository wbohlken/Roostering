package nl.uva.heuristiek.ga;

import nl.uva.heuristiek.model.Penalty;
import nl.uva.heuristiek.model.Schedule;

/**
 * Created by remco on 22/04/15.
 */
public class Chromosome {
    private int[] mActivitySlots;
    private Penalty mPenalty;

    public Chromosome(Schedule schedule) {
        mActivitySlots = schedule.getActivitySlots();
        mPenalty = schedule.getPenalty(false);
    }

    public Chromosome(int[] activitySlots, Penalty penalty) {
        mActivitySlots = activitySlots;
        mPenalty = penalty;
    }

    public Chromosome(Chromosome parent1, Chromosome parent2) {

    }

    public int[] getActivitySlots() {
        return mActivitySlots;
    }

    public Penalty getPenalty() {
        return mPenalty;
    }

    public void mutate() {

    }
}
