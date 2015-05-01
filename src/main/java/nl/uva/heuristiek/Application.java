package nl.uva.heuristiek;

import com.google.gson.*;
import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Penalty;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.view.ControlPanel;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfugue.player.Player;

public class Application extends JFrame implements Schedule.ScheduleStateListener, ControlPanel.ControlInterface {
    Player mPlayer;

    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    private Penalty mSmallestPenalty = null;
    private final SchedulePanel mSchedulePanel;
    FileWriter mLogWriter;
    FileWriter mResultsLogWriter;
    private final File mCoursesFile;
    private final File mStudentsFile;
    private Schedule mStepSchedule;

    public Application() {
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mPlayer = new Player();
        mCoursesFile = new File("vakken.csv");
        mStudentsFile = new File("studenten_roostering.csv");

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

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        new Thread(new Runnable() {
            public void run() {
                Schedule bestSchedule = null;
                long loops = 0;
                while (loops++ < 1) {
                    Schedule schedule = planSchedule(Schedule.FLAG_PLAN_METHOD_CONSTRUCTIVE, true);
//                    Schedule schedule = planRandom(students, courses);
                    try {
                        mResultsLogWriter.write(schedule.getPenalty(false)+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (mSmallestPenalty == null || schedule.getPenalty(false).getTotal() < mSmallestPenalty.getTotal()) {
                        mSmallestPenalty = schedule.getPenalty(false);
                        bestSchedule = schedule;
                    }
                    log(String.format("Loops: %d, Penalty: %d, Smallest penalty: %d", loops++, schedule.getPenalty(false).getTotal(), mSmallestPenalty.getTotal()));
                }
                try {
                    mLogWriter.close();

                    mResultsLogWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log(String.format("Penalty: %s", bestSchedule.getPenalty(false).toString()));

                int[] occupation = bestSchedule.getRoomOccupation();
                int[] averageSeatOccupation = bestSchedule.getSeatOccupationPerRoom();

                log("Room occupations");
                for (int i = 0; i < occupation.length; i++) {
                    System.out.println(i + ": " + occupation[i] + " %");
                }

                log("Average seat occupation per room");
                for (int i = 0; i < averageSeatOccupation.length; i++) {
                    log(i + ": " + averageSeatOccupation[i] + " %");
                }
            }
        }).start();
    }

    public Schedule planSchedule(int flags, boolean plan) {
        Context context = DataProcessor.process(mStudentsFile, mCoursesFile);
        Schedule schedule = new Schedule(context, flags);
        schedule.setListener(Application.this);
        if (plan)
            schedule.plan();
        return schedule;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application application = new Application();
                application.setVisible(true);
            }
        });
    }

    public void onStateChanged(Context context, Schedule schedule) {
        Course.Activity[] activities = new Course.Activity[Constants.ROOMSLOT_COUNT];
        final int count = schedule.getContext().getActivities().size();
        for (int activityIndex = 0; activityIndex < count; activityIndex++) {
            int roomSlot = schedule.getContext().getRoomSlot(activityIndex);
            if (roomSlot != -1)
                activities[roomSlot] = schedule.getContext().getActivities().get(activityIndex);
        }
        mSchedulePanel.setActivities(activities, schedule.getPenalty(true));
        mSchedulePanel.setComplete(false);
        revalidate();
        repaint();
    }

    public void onScheduleComplete(Schedule schedule) {
        Course.Activity[] activities = new Course.Activity[Constants.ROOMSLOT_COUNT];
        final int count = schedule.getContext().getActivities().size();
        for (int activityIndex = 0; activityIndex < count; activityIndex++) {
            int roomSlot = schedule.getRoomSlot(activityIndex);
            if (roomSlot != -1)
                activities[roomSlot] = schedule.getContext().getActivities().get(activityIndex);
        }
        mSchedulePanel.setActivities(activities, schedule.getPenalty(true));
        mSchedulePanel.setComplete(true);
        revalidate();
        repaint();
    }


    public static void log(String message) {
        System.out.println(message);
    }

    @Override
    public boolean step(int size) {
        if (mStepSchedule != null)
            return mStepSchedule.planStep(size);
        return false;
    }

    @Override
    public void newRandom() {
        mStepSchedule = planSchedule(Schedule.FLAG_PLAN_METHOD_RANDOM, false);
    }

    @Override
    public void newContructive() {
        mStepSchedule = planSchedule(Schedule.FLAG_PLAN_METHOD_CONSTRUCTIVE, false);
    }

    @Override
    public void complete() {
        while (mStepSchedule.planStep(1));
    }

    @Override
    public void climb(int stepSize) {
        mStepSchedule.climbHill(stepSize);
    }
}
