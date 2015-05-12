package nl.uva.heuristiek.ga;

import nl.uva.heuristiek.model.Penalty;

/**
 * Created by remco on 08/05/15.
 */
public interface Chromosome extends Comparable<Chromosome> {
    Penalty getPenalty();
    void evaluate();
}
