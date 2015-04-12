package nl.uva.heuristiek;

import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

public class Application extends JFrame implements Schedule.ScheduleStateListener {
    Course.Activity[][][] mScheduleState;

    public Application() {

        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        File courses = new File("vakken.csv");
        File students = new File("studenten_roostering.csv");
        Schedule schedule = new Schedule(this);
        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
            SchedulePanel schedulePanel = new SchedulePanel(schedule);
            add(schedulePanel);
        }
        Map<String, Course> courseMap = DataProcessor.process(students, courses);
        if (courseMap != null) {
            LinkedList<Course.Activity> activities = new LinkedList<Course.Activity>();
            for (Course course : courseMap.values()) {
                activities.addAll(course.getActivities());
            }
            Collections.sort(activities, new Comparator<Course.Activity>() {
                public int compare(Course.Activity o1, Course.Activity o2) {
                    return Integer.compare(o1.getStudents().size(), o2.getStudents().size());
                }
            });

            schedule.planCourses(activities);

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

    public void onStateCreated(Course.Activity[][][] state) {
        mScheduleState = state;
        onStateChanged();
    }

    public void onStateChanged() {
        revalidate();
        repaint();
    }
}
