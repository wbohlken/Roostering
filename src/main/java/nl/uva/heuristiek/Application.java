package nl.uva.heuristiek;

import nl.uva.heuristiek.algorithm.HillClimber;
import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.*;
import nl.uva.heuristiek.view.ControlPanel;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jfugue.player.Player;

public class Application extends JFrame implements Schedule.ScheduleStateListener, ControlPanel.ControlInterface {
    private Thread mMainThread;
    Player mPlayer;

    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    private Penalty mSmallestPenalty = null;
    private final SchedulePanel mSchedulePanel;
    FileWriter mLogWriter;
    FileWriter mResultsLogWriter;
    private final File mCoursesFile;
    private final File mStudentsFile;
    private HillClimber mHillClimber;

    private Context mContext;
    private Schedule mCurrentSchedule;

    public Application() {
        mMainThread = Thread.currentThread();
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mPlayer = new Player();
        mCoursesFile = new File("vakken.csv");
        mStudentsFile = new File("studenten_roostering.csv");
        mContext = DataProcessor.process(mStudentsFile, mCoursesFile);


        File file = new File("highscores.json");
        File csvResults = new File("results.csv");

        try {
            if (file.exists()) file.delete();
            file.createNewFile();
                mLogWriter = new FileWriter(file);
            if (csvResults.exists()) csvResults.delete();
            csvResults.createNewFile();
            mResultsLogWriter = new FileWriter(csvResults);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSchedulePanel = new SchedulePanel(this);
        add(mSchedulePanel);

        mCurrentSchedule = new ConstructiveSchedule(mContext, 0, null, this);

//        Runnable target = new Runnable() {
//            public void run() {
//                Schedule bestSchedule = null;
//                long loops = 0;
//                while (loops++ < 1) {
//                    Schedule schedule = planSchedule(Schedule.FLAG_PLAN_METHOD_CONSTRUCTIVE, true);
//                    try {
//                        mResultsLogWriter.write(schedule.getPenalty(false) + "\n");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if (mSmallestPenalty == null || schedule.getPenalty(false).getTotal() < mSmallestPenalty.getTotal()) {
//                        mSmallestPenalty = schedule.getPenalty(false);
//                        bestSchedule = schedule;
//                    }
//                    log(String.format("Loops: %d, Penalty: %d, Smallest penalty: %d", loops++, schedule.getPenalty(false).getTotal(), mSmallestPenalty.getTotal()));
//                }
//                try {
//                    mLogWriter.close();
//
//                    mResultsLogWriter.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                log(String.format("Penalty: %s", bestSchedule.getPenalty(false).toString()));
//
//                int[] occupation = bestSchedule.getRoomOccupation();
//                int[] averageSeatOccupation = bestSchedule.getSeatOccupationPerRoom();
//
//                log("Room occupations");
//                for (int i = 0; i < occupation.length; i++) {
//                    log(i + ": " + occupation[i] + " %");
//                }
//
//                log("Average seat occupation per room");
//                for (int i = 0; i < averageSeatOccupation.length; i++) {
//                    log(i + ": " + averageSeatOccupation[i] + " %");
//                }
//            }
//        };
//        new Thread(target).start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application application = new Application();
                application.setVisible(true);
                System.out.println(Thread.currentThread().getId());
            }
        });
        System.out.println(Thread.currentThread().getId());
    }

    private void resetContext() {
        mContext = DataProcessor.process(mStudentsFile, mCoursesFile);
    }

    public void redraw(Schedule schedule, boolean scheduleComplete) {
        mSchedulePanel.setComplete(scheduleComplete);
        mSchedulePanel.setPenalty(schedule.getPenalty(true));
        redraw();
    }

    @Override
    public void activityAdded(int roomSlot, Course.Activity activity) {
        mSchedulePanel.addActivity(roomSlot, activity);
    }

    @Override
    public void removeActivity(int roomSlot) {
        mSchedulePanel.removeActivity(roomSlot);
    }

    public static void log(String message) {
//        System.out.println(message);
    }

    @Override
    public void step(int size) {
        mCurrentSchedule.doSteps(size, new Schedule.Callback() {
            @Override
            public void done(boolean scheduleComplete) {
                mSchedulePanel.setComplete(scheduleComplete);
            }
        });
    };

    @Override
    public void newRandom() {
        mCurrentSchedule = new RandomSchedule(mContext, 0, this);
        mSchedulePanel.reset();
        redraw();
    }

    @Override
    public void newContructive() {
        mCurrentSchedule = new ConstructiveSchedule(mContext, 0, null, this);
        mSchedulePanel.reset();
        redraw();
    }

    public void redraw() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                revalidate();
                repaint();
            }
        });
    }

    @Override
    public void complete() {
        mCurrentSchedule.doSteps(mContext.getActivities().size(), new Schedule.Callback() {
            @Override
            public void done(boolean scheduleComplete) {

            }
        });
    }

    @Override
    public void climb(int stepSize) {
        if (mHillClimber == null)
            mHillClimber = new HillClimber(mCurrentSchedule);
        System.out.println(Thread.currentThread().getId());
        mHillClimber.climb(stepSize, new HillClimber.Callback() {
            @Override
            public void done() {
                mSchedulePanel.setPenalty(mCurrentSchedule.getPenalty(true));
                redraw();
            }

            @Override
            public void swapped(List<Integer> swap) {
                for (Integer activityIndex : swap) {
                    activityAdded(mCurrentSchedule.getRoomSlot(activityIndex), mContext.getActivities().get(activityIndex));
                }
                mSchedulePanel.setPenalty(mCurrentSchedule.getPenalty(false));
                redraw();
            }

            @Override
            public void activityPlanned(int activityIndex) {

            }
        });
    }
}
