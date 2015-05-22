package nl.uva.heuristiek.algorithm;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;

/**
 * Created by remco on 07/05/15.
 */
public class HillClimber {

    Target mTarget;
    Type mType;

    private int mTemperature;
    private int mBestCount;
    private int mBest, mGlobalBest;

    public HillClimber(Target target, Type type) {
        mTarget = target;
        mType = type;
        mTemperature = type.getInitialTemperature();
    }

    public void climb(final int steps, final Callback callback) {
        new SwingWorker<Void, Integer>() {

            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < steps; i++) {
                        int oldFitness = mTarget.getFitness();
                        Integer[] swap = mTarget.generateRandomSwap();
                        Integer[] reverseSwap = mTarget.swap(swap);
                        int newFitness = mTarget.getFitness();
                        if (!accept(newFitness, oldFitness))
                            mTarget.swap(reverseSwap);
                        else {
                            publish(swap);
                            if (newFitness > mBest) {
                                mBest = newFitness;
                                mBestCount = 0;
                            } else {
                                mBestCount++;
                            }
                        }
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                super.process(chunks);
                callback.swapped(chunks);
            }

            @Override
            protected void done() {
                callback.done();
            }
        }.execute();
    }

    public int climb(final int stepSize, SyncCallback callback) {
        int step;
        mBest = 0;
        mBestCount = 0;
        HashSet<Integer> unique = new HashSet<>();
        for (step = 0; step < stepSize; step++) {
            int oldFitness = mTarget.getFitness();
            Integer[] swap = mTarget.generateRandomSwap();
            Integer[] reverseSwap = mTarget.swap(swap);
            int newFitness = mTarget.getFitness();
            if (!accept(newFitness, oldFitness)) {
                mTarget.swap(reverseSwap);
            }
            else {
                if (unique.add(mTarget.hashCode())) {
                        callback.publish(swap, step, mBest, mGlobalBest, mBestCount, mTemperature);
                    if (newFitness > mBest) {
                        mBest = newFitness;
                        mBestCount = 0;
                    } else if (newFitness == mBest) {
                        mBestCount++;
                    }
                }
            }
            if (mBest > mGlobalBest) mGlobalBest = mBest;
        }
        return step;
    }

    public boolean accept(int newFitness, int oldFitness) {
        if (mType == Type.HillClimber) {
            return newFitness >= oldFitness;
        } else if (mType == Type.HillClimberPlus) {
            if (newFitness >= oldFitness) return true;
            if (mBestCount >= 2000) {
                final boolean accept = Math.exp(newFitness - oldFitness) > Math.random();
                if (accept) {
                    mBestCount = 0;
                    mBest = newFitness;
                }
                return accept;
            }
            return false;
        } else {
            mTemperature = Math.max(1, mTemperature - 1);
            return Math.exp((newFitness - oldFitness)/mTemperature) > Math.random();
        }
    }

    public interface Callback {
        void done();
        void swapped(List<Integer> swap);
    }

    public interface SyncCallback {
        void publish(Integer[] swap, int currentStep, int currentBest, int globalBest, int bestCount, int temperature);
    }

    public interface Target {
        Integer[] swap(Integer[] swap);
        Integer[] generateRandomSwap();
        int getFitness();
    }

    public enum Type {
        HillClimber,
        HillClimberPlus,
        SimulatedAnnealing;

        private int mInitialTemperature;

        public Type setInitialTemperature(int initialTemperature) {
            mInitialTemperature = initialTemperature;
            return this;
        }

        public int getInitialTemperature() {
            return mInitialTemperature;
        }
    }
}
