package nl.uva.heuristiek;

import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.model.Student;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

import org.jfugue.player.Player;

public class Application extends JFrame implements Schedule.ScheduleStateListener {
    Course.Activity[] mScheduleState;
    Player mPlayer;

    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    private int mSmallestPenalty = 2000;

    public Application() {
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mPlayer = new Player();
        File courses = new File("vakken.csv");
        File students = new File("studenten_roostering.csv");
        Schedule bestSchedule;
        long loops = 0;
        while (true) {
            DataProcessor.process(students, courses);
            Map<String, Course> courseMap = DataProcessor.getCourseMap();
            Collection<Course> courseCollection = courseMap.values();
            Set<Student> studentSet = DataProcessor.getStudents();
            Schedule schedule = new Schedule(this);
            LinkedList<Course.Activity> activities = new LinkedList<Course.Activity>();
            for (Course course : courseCollection) {
                activities.addAll(course.getActivities());
            }
            Collections.sort(activities, new Comparator<Course.Activity>() {
                public int compare(Course.Activity o1, Course.Activity o2) {
                    return Integer.compare(o1.getStudents().size(), o2.getStudents().size());
                }
            });

            schedule.planCourses(courseCollection, activities, DataProcessor.getStudents());
            if (schedule.getPenalty() < mSmallestPenalty) {
                mSmallestPenalty = schedule.getPenalty();
                bestSchedule = schedule;
                if (mSmallestPenalty <= 60) break;
            }
            System.out.printf("Loops: %d, Smallest penalty: %d\n ", loops++, mSmallestPenalty);
        }
        System.out.printf("Penalty: %d \n", bestSchedule.getPenalty());

        int[] occupation = bestSchedule.getRoomOccupation();
        int[] averageSeatOccupation = bestSchedule.getSeatOccupationPerRoom();

        System.out.println("Room occupations");
        for (int i = 0; i < occupation.length; i++) {
            System.out.println(i + ": " + occupation[i] + " %");
        }

        System.out.println("Average seat occupation per room");
        for (int i = 0; i < averageSeatOccupation.length; i++) {
            System.out.println(i + ": " + averageSeatOccupation[i] + " %");
        }

        for (Course.Activity activity : bestSchedule.getPenaltyActivities()) {
            System.out.printf("Penalty for Activity in course %s", activity.getCourse().getCourseId());
        }
        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
            SchedulePanel schedulePanel = new SchedulePanel(bestSchedule);
            add(schedulePanel);
        }


    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application application = new Application();
                application.setVisible(true);
            }
        });
    }

    public void onStateCreated(Course.Activity[] state) {
        mScheduleState = state;
        onStateChanged();
    }

    public void onStateChanged() {
        revalidate();
        repaint();
    }

    public void onScheduleComplete(int penalty, int totalActivities, int plannedActivities) {
//        if (penalty < mSmallestPenalty)
//            mSmallestPenalty = penalty;
//        System.out.printf("Smallest penalty: %d, Total Activities: %d, Activities planned: %d\n", penalty, totalActivities, plannedActivities);
    }
}
