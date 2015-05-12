package nl.uva.heuristiek.ga;

import com.sun.istack.internal.NotNull;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.BaseModel;
import nl.uva.heuristiek.model.Penalty;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by remco on 08/05/15.
 */
public abstract class BaseAlgorithm<T extends Chromosome> extends BaseModel {

    private Config mConfig;
    private List<T> mChromosomes = new ArrayList<>();

    public BaseAlgorithm(@NotNull Context context, @NotNull Config config) {
        super(context);
        mConfig = config;
        initPopulation();
        evaluate();
    }

    public Config getConfig() {
        return mConfig;
    }

    public abstract T newChromsome();

    public abstract T crossbreed(T parent1, T parent2);

    public abstract void mutate(T chromosome);

    protected abstract T selectParent();

    protected void initPopulation() {
        while (mChromosomes.size() < mConfig.getMinPopulation()) {
            mChromosomes.add(newChromsome());
        }
    }

    public void loop() {
        eliminate();
        reproduce();
        evaluate();
    }

    public void doLoops(final int loops, final Callback callback) {
        new SwingWorker<Void, Chromosome>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int loop = 0; loop < loops; loop++) {
                    loop();
                    Collections.sort(mChromosomes);
                    publish(mChromosomes.get(0));
                }
                return null;
            }

            @Override
            protected void process(List<Chromosome> chunks) {
                super.process(chunks);
                callback.iterationComplete(chunks.get(0));
            }

            @Override
            protected void done() {
                callback.done(BaseAlgorithm.this);
            }
        }.execute();
    }

    public void evaluate() {
        for (T chromosome : mChromosomes)
            chromosome.evaluate();
    }

    public T getBest() {
        Collections.sort(mChromosomes);
        return mChromosomes.get(0);
    }

    public abstract void eliminate();

    public void reproduce() {
        while (mChromosomes.size() < mConfig.getMaxPopulation()) {
            T crossbreed = crossbreed(selectParent(), selectParent());
            if (crossbreed == null) continue;
            mutate(crossbreed);
            mChromosomes.add(crossbreed);
        }
    }

    public List<T> getChromosomes() {
        return mChromosomes;
    }

    public interface Callback {
        void done(BaseAlgorithm algorithm);
        void iterationComplete(Chromosome best);
    }
}
