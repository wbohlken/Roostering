package nl.uva.heuristiek.algorithm;

import javax.swing.*;
import java.util.List;

/**
 * Created by remco on 07/05/15.
 */
public class HillClimber {

    Target mTarget;
    Type mType;

    private int mTemperature;

    public HillClimber(Target target) {
        mTarget = target;
        mType = Type.HillClimber;
    }

    public HillClimber(Target target, int initialTemperature) {
        mTarget = target;
        mType = Type.SimulatedAnnealing;
        mTemperature = initialTemperature;
    }

    public void climb(final int steps, final Callback callback) {
        new SwingWorker<Void, Integer>() {

            @Override
            protected Void doInBackground() throws Exception {
                System.out.println(Thread.currentThread().getId());
                for (int i = 0; i < steps; i++) {
                    int oldFitness = mTarget.getFitness();
                    Integer[] swap = mTarget.generateRandomSwap();
                    Integer[] reverseSwap = mTarget.swap(swap);
                    int newFitness = mTarget.getFitness();
                    if (!accept(newFitness, oldFitness))
                        mTarget.swap(reverseSwap);
                    else
                        publish(swap);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                super.process(chunks);
                for (Integer activityIndex : chunks)
                    callback.activityPlanned(activityIndex);
                callback.swapped(chunks);
            }

            @Override
            protected void done() {
                callback.done();
            }
        }.execute();


    }

    public boolean accept(int newFitness, int oldFitness) {
        if (mType == Type.HillClimber)
            return newFitness >= oldFitness;
        else {
            mTemperature = Math.max(1, mTemperature - 1);
            return Math.exp((newFitness - oldFitness)) > Math.random();
        }
    }

    public interface Callback {
        void done();
        void swapped(List<Integer> swap);
        void activityPlanned(int activityIndex);
    }

    public interface Target {
        Integer[] swap(Integer[] swap);
        Integer[] generateRandomSwap();
        int getFitness();
    }

    enum Type {
        HillClimber, SimulatedAnnealing;
    }
}
