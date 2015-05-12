package nl.uva.heuristiek.ga;

/**
 * Created by remco on 08/05/15.
 */
public class Config {
    private int mMinPopulation, mMaxPopulation;

    public int getMinPopulation() {
        return mMinPopulation;
    }

    public Config setMinPopulation(int minPopulation) {
        mMinPopulation = minPopulation;
        return this;
    }

    public int getMaxPopulation() {
        return mMaxPopulation;
    }

    public Config setMaxPopulation(int maxPopulation) {
        mMaxPopulation = maxPopulation;
        return this;
    }
}
