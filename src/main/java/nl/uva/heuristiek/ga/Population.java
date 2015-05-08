package nl.uva.heuristiek.ga;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.RandomSchedule;
import nl.uva.heuristiek.model.Schedule;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 29/04/15.
 */
public class Population {

    private static SecureRandom sRandom = new SecureRandom();

    private Context mContext;
    private ArrayList<Chromosome> mChromosomes = new ArrayList<>(Constants.MAX_POPULATION_COUNT);
    private int mBestIndex = -1;

    public Population(Context context) {
        mContext = context;
    }

    public void fillPopulation() {
        while (mChromosomes.size() < Constants.MAX_POPULATION_COUNT) {
            Schedule schedule = new RandomSchedule(mContext, 0, null);
            schedule.plan();
            Chromosome newChromosome = new Chromosome(schedule);
            if (mBestIndex > 0 && newChromosome.getPenalty().getTotal() < mChromosomes.get(mBestIndex).getPenalty().getTotal()) {
                mBestIndex = mChromosomes.size();
            }
            mChromosomes.add(newChromosome);
        }
    }

    public ArrayList<Chromosome> selection() {
        if (mChromosomes.size() < Constants.MIN_POPULATION_COUNT) return null;
        ArrayList<Chromosome> chromosomes = new ArrayList<>(Constants.MAX_POPULATION_COUNT);
        if (Constants.ELITISM) {
            chromosomes.add(mChromosomes.get(mBestIndex));
        }

        int index;
        while (chromosomes.size() < Constants.MIN_POPULATION_COUNT) {
            index = sRandom.nextInt(mChromosomes.size());
            chromosomes.add(mChromosomes.remove(index));
        }
        mChromosomes = chromosomes;
        return null;
    }

    public void reproduce() {
        int parent1Index, parent2Index;
        while (mChromosomes.size() < Constants.MAX_POPULATION_COUNT) {
            parent1Index = sRandom.nextInt(mChromosomes.size());
            parent2Index = sRandom.nextInt(mChromosomes.size());
            mChromosomes.add(new Chromosome(mChromosomes.get(parent1Index), mChromosomes.get(parent2Index)));
        }
    }

    public static Chromosome crossBreed(Chromosome parent1, Chromosome parent2) {
        return null;
    }




}
