package nl.uva.heuristiek.view;

import nl.uva.heuristiek.model.Penalty;
import nl.uva.heuristiek.model.Schedule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by remco on 01/05/15.
 */
public class ControlPanel extends JPanel {

    private final JTextPane mPenaltyPane;
    private final JTextField mStepSize;
    private ControlInterface mInterface;
    private final JButton mClimb;
    private final JButton mComplete;
    private final JButton mNewConstructive;
    private final JButton mNewRandon;
    private final JButton mStep;


    public ControlPanel(ControlInterface controlInterface) {
        mInterface = controlInterface;
        mStep = new JButton("Step");
        mStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null) {
                    mInterface.step(Integer.parseInt(mStepSize.getText()));
                }
            }
        });
        add(mStep);


        mNewRandon = new JButton("New Random");
        mNewRandon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    mInterface.newRandom();
            }
        });
        add(mNewRandon);

        mNewConstructive = new JButton("New Contructive");
        mNewConstructive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    mInterface.newContructive();
            }
        });
        add(mNewConstructive);

        mComplete = new JButton("Complete");
        mComplete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    mInterface.complete();
                setComplete(true);

            }
        });
        add(mComplete);

        mClimb = new JButton("Climb");
        mClimb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    new SwingWorker<Schedule, Schedule>() {

                        @Override
                        protected Schedule doInBackground() throws Exception {
                            return null;
                        }
                    }.run();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mInterface.climb(Integer.parseInt(mStepSize.getText()));
                        }
                    }).start();

            }
        });
        add(mClimb);

        mPenaltyPane = new JTextPane();
        add(mPenaltyPane);

        mStepSize = new JTextField("20");
        add(mStepSize);

    }

    public void setComplete(boolean complete) {
        mStep.setEnabled(!complete);
        mClimb.setEnabled(complete);
        mComplete.setEnabled(!complete);
    }


    public void setPenalty(Penalty penalty) {
        if (penalty != null)
            mPenaltyPane.setText(penalty.toString());
    }


    public interface ControlInterface {
        boolean step(int size);
        void newRandom();
        void newContructive();
        void climb(int stepSize);
        void complete();
    }
}
