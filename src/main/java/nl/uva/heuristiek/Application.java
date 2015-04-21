package nl.uva.heuristiek;

import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;

import org.jfugue.player.Player;

public class Application extends JFrame implements Schedule.ScheduleStateListener {
    Player mPlayer;

    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    HashSet<Integer[]> randomStates = new HashSet<>();
    private int mSmallestPenalty = 2000;
    private final SchedulePanel mSchedulePanel;

    public Application() {
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mPlayer = new Player();
        final File courses = new File("vakken.csv");
        final File students = new File("studenten_roostering.csv");

        mSchedulePanel = new SchedulePanel(null);
        add(mSchedulePanel);

        new Thread(new Runnable() {
            public void run() {
                Schedule bestSchedule;
                long loops = 0;
                int dupilcates = 0;
                while (true) {
                    Schedule schedule = DataProcessor.process(students, courses);
                    schedule.setListener(Application.this);
                    if (randomStates.contains(schedule.getTimeslots())) {
                        log("Already seen");
                        dupilcates++;
                        continue;
                    }
                    randomStates.add(schedule.getTimeslots());
                    schedule.planCourses();
                    if (schedule.getPenalty() < mSmallestPenalty) {
                        mSmallestPenalty = schedule.getPenalty();
                        bestSchedule = schedule;
                        if (mSmallestPenalty <= 40) break;
                    }
                    log(String.format("Loops: %d, Smallest penalty: %d, Duplicates: %d", loops++, mSmallestPenalty, dupilcates));
                }

                log(String.format("Penalty: %d", bestSchedule.getPenalty()));

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

                for (Course.Activity activity : bestSchedule.getPenaltyActivities()) {
                    log(String.format("Penalty for Activity in course %s", activity.getCourse().getCourseId()));
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application application = new Application();
                application.setVisible(true);
            }
        });
    }

    public void onStateChanged(Course.Activity[] schedule) {
//        mSchedulePanel.setActivities(schedule);
//        revalidate();
//        repaint();
    }

    public void onScheduleComplete(Course.Activity[] activities, int penalty, int totalActivities, int plannedActivities) {
        mSchedulePanel.setActivities(activities);
        revalidate();
        repaint();
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
