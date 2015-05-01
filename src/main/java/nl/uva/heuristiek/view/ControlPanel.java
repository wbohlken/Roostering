package nl.uva.heuristiek.view;

import nl.uva.heuristiek.model.Penalty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by remco on 01/05/15.
 */
public class ControlPanel extends JPanel {

    private final JTextPane mPenaltyPane;
    public Penalty mPenalty;
    private ControlInterface mInterface;
    private final JButton mClimb100;
    private final JButton mClimb;
    private final JButton mComplete;
    private final JButton mNewConstructive;
    private final JButton mNewRandon;
    private final JButton mStep10;
    private final JButton mStep;

    public ControlPanel(ControlInterface controlInterface) {
        mInterface = controlInterface;
        mStep = new JButton("Step 1");
        mStep10 = new JButton("Step 10");
        mStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null) {
                    mInterface.step(1);
                }
            }
        });
        add(mStep);

        mStep10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null) {
                    mInterface.step(10);
                }
            }
        });
        add(mStep10);

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

            }
        });
        add(mComplete);

        mClimb = new JButton("Climb 1");
        mClimb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    mInterface.climb(1);
            }
        });
        add(mClimb);

        mClimb100 = new JButton("Climb 100");
        mClimb100.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mInterface != null)
                    mInterface.climb(100);
            }
        });
        add(mClimb100);

        mPenaltyPane = new JTextPane();
        add(mPenaltyPane);

    }

    public void setComplete(boolean complete) {
        mStep.setEnabled(!complete);
        mStep10.setEnabled(!complete);
        mClimb.setEnabled(complete);
        mClimb.setEnabled(complete);
        mComplete.setEnabled(!complete);
    }


    public void setPenalty(Penalty penalty) {
        mPenalty = penalty;
        if (mPenalty != null)
            mPenaltyPane.setText(mPenalty.toString());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (mPenalty != null)
            g2d.drawString(String.format("Penalty: %d", mPenalty.getTotal()), 0, 0);
    }

    public interface ControlInterface {
        boolean step(int size);
        void newRandom();
        void newContructive();
        void climb(int stepSize);
        void complete();
    }
}
