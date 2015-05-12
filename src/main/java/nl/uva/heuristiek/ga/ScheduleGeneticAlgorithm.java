package nl.uva.heuristiek.ga;

import com.sun.istack.internal.NotNull;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.BaseModel;
import nl.uva.heuristiek.util.*;
import nl.uva.heuristiek.util.Random;

import java.util.*;

/**
 * Created by remco on 08/05/15.
 */
public class ScheduleGeneticAlgorithm extends BaseAlgorithm<ScheduleChromosome> {

    public ScheduleGeneticAlgorithm(@NotNull Context context, @NotNull Config config) {
        super(context, config);
    }

    @Override
    public ScheduleChromosome newChromsome() {
        return new ScheduleChromosome(getContext());
    }

    @Override
    public ScheduleChromosome crossbreed(ScheduleChromosome parent1, ScheduleChromosome parent2) {
        if (parent1 == parent2) return null;
        int[] parent1ActivitySlots = parent1.getSchedule().getActivitySlots();
        int[] parent2ActivitySlots = parent2.getSchedule().getActivitySlots();

        final int length = parent1ActivitySlots.length;

        int[] newActivitySlots = Arrays.copyOf(parent1ActivitySlots, length);

        for (int i = length / 2; i < length; i++) {
            newActivitySlots[i] = parent2ActivitySlots[i];
        }
        return new ScheduleChromosome(getContext(), newActivitySlots);
    }

    @Override
    public void mutate(ScheduleChromosome chromosome) {
        int schedulSize = getActivities().size();
        chromosome.getSchedule().swap(new Integer[]{Random.nextInt(schedulSize), Random.nextInt(schedulSize)});
    }

    @Override
    protected ScheduleChromosome selectParent() {
        final List<ScheduleChromosome> chromosomes = getChromosomes();
        return chromosomes.get(nl.uva.heuristiek.util.Random.nextInt(chromosomes.size()));
    }

    @Override
    public void eliminate() {
        final List<ScheduleChromosome> chromosomes = getChromosomes();
        Collections.sort(chromosomes);
        while (chromosomes.size() > getConfig().getMinPopulation())
            chromosomes.remove(chromosomes.size()-1);
    }
}
