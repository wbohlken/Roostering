package nl.uva.heuristiek;

import com.google.gson.*;
import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;


public class Application extends JFrame implements Schedule.ScheduleStateListener {


    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    HashSet<Integer[]> randomStates = new HashSet<>();
    private int mSmallestPenalty = 2000;
    private int mSmallestStudentPenalty = 100;
    private int mSmallestCoursePenalty = 2000;

    private final SchedulePanel mSchedulePanel;
    FileWriter mLogWriter;
    FileWriter mResultsLogWriter;

    public Application() {
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final File courses = new File("vakken.csv");
        final File students = new File("studenten_roostering.csv");

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

        mSchedulePanel = new SchedulePanel(null);
        add(mSchedulePanel);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        new Thread(new Runnable() {
            public void run() {
                Schedule bestSchedule = null;
                JsonArray goodSchedules = new JsonArray();
                long loops = 0;
                int dupilcates = 0;

                ArrayList<ArrayList<Integer>> values = new ArrayList<>();

                while (true) {
                    Schedule schedule = planScheduleConstructive(students, courses);
//                    Schedule schedule = planRandom(students, courses);
                    try {
                        mResultsLogWriter.write(schedule.getPenalty()+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (schedule.getPenalty() < 40) {
                        JsonObject scheduleJson = new JsonObject();
                        scheduleJson.add("state", gson.toJsonTree(schedule.getTimeslots()));
                        scheduleJson.addProperty("penalty", schedule.getPenalty());
                        goodSchedules.add(scheduleJson);
                    }
                    if (schedule.getPenalty() < mSmallestPenalty) {
                        mSmallestPenalty = schedule.getPenalty();
                        bestSchedule = schedule;
                    }

                    if (schedule.getStudentPenalty() < mSmallestStudentPenalty) {
                        mSmallestStudentPenalty = schedule.getStudentPenalty();
                    }
                    if (schedule.getCoursePenalty() < mSmallestCoursePenalty) {
                        mSmallestCoursePenalty = schedule.getCoursePenalty();
                    }
                    if ( loops > 10000) break;

                    log(String.format("Loops: %d, Total Penalty: %d, Student Penalty: %d, Course Penalty: %d, Smallest penalty: %d,Smallest Course penalty: %d, Smallest Student penalty: %d, Duplicates: %d",
                            loops++, schedule.getPenalty(),  schedule.getStudentPenalty(), schedule.getCoursePenalty(), mSmallestPenalty, mSmallestCoursePenalty, mSmallestStudentPenalty, dupilcates));

                    ArrayList<Integer> value = new ArrayList<>();
                    value.add(schedule.getPenalty());
                    value.add(schedule.getStudentPenalty());
                    value.add(schedule.getCoursePenalty());
                    values.add(value);
                }
                try {
                    Util.generateCsvFile("constructive_penalties.csv", values);
                    mLogWriter.write(gson.toJson(goodSchedules));
                    mLogWriter.close();

                    mResultsLogWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
            }
        }).start();
    }

    private Schedule planScheduleConstructive(File students, File courses) {
        Schedule schedule = DataProcessor.process(students, courses);
        schedule.setListener(Application.this);
        randomStates.add(schedule.getTimeslots());
        schedule.planCourses();
        return schedule;
    }

    private Schedule planRandom(File students, File courses) {
        Schedule schedule = DataProcessor.process(students, courses);
        schedule.setListener(this);
        schedule.planRandom();
        return schedule;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application application = new Application();
//                application.setVisible(true);
            }
        });
    }

    public void onStateChanged(Course.Activity[] schedule) {
//        mSchedulePanel.setActivities(schedule);
//        revalidate();
//        repaint();
    }

    public void onScheduleComplete(Course.Activity[] activities) {
//        mSchedulePanel.setActivities(activities);
//        revalidate();
//        repaint();
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
