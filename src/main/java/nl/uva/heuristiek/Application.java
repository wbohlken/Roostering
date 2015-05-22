package nl.uva.heuristiek;

import nl.uva.heuristiek.algorithm.HillClimber;
import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.ga.Config;
import nl.uva.heuristiek.ga.ScheduleGeneticAlgorithm;
import nl.uva.heuristiek.model.*;
import nl.uva.heuristiek.view.ControlPanel;
import nl.uva.heuristiek.view.SchedulePanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;


public class Application extends JFrame implements Schedule.ScheduleStateListener, ControlPanel.ControlInterface {

    String[] notes = new String[] {"A", "B", "C", "D", "E", "F", "G"};

    private Penalty mSmallestPenalty = null;
    private final SchedulePanel mSchedulePanel;
    private FileWriter mLogWriter;
    private FileWriter mResultsLogWriter;
//    private final File mCoursesFile;
//    private final File mStudentsFile;
    private HillClimber mHillClimber;

    private Context mContext;
    private Schedule mCurrentSchedule;

    public Application() {
        setTitle("Simple example");
        setSize(1280, 1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final InputStream coursesIS = ClassLoader.getSystemResourceAsStream("vakken.csv");
        final InputStream studentsIS = ClassLoader.getSystemResourceAsStream("studenten_roostering.csv");
        mContext = DataProcessor.process(new InputStreamReader(coursesIS), new InputStreamReader(studentsIS));

        final ScheduleGeneticAlgorithm geneticAlgorithm = new ScheduleGeneticAlgorithm(mContext, new Config().setMinPopulation(100).setMaxPopulation(200));
//        geneticAlgorithm.doLoops(100000, new BaseAlgorithm.Callback() {
//            @Override
//            public void done(BaseAlgorithm algorithm) {
//                Penalty bestPenalty = algorithm.getBest().getPenalty();
//                System.out.println(bestPenalty.toString());
//            }
//
//            @Override
//            public void iterationComplete(Chromosome best) {
//                System.out.println(best.getPenalty().getTotal());
//            }
//        });

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
        }

//        for (int i = 0; i < 10; i++) {
//            final Schedule schedule = new RandomSchedule(mContext, 0, null);
//            schedule.doSteps(mContext.getActivities().size());
//            final HillClimber hillClimber = new HillClimber(schedule, HillClimber.Type.HillClimber);
//            int loops = 100000;
//            final StringBuilder results = new StringBuilder(loops*5);
//            final int iteration = i;
//            hillClimber.climb(loops, new HillClimber.SyncCallback() {
//                @Override
//                public void publish(Integer[] swap, int currentStep, int currentBest, int globalBest, int bestCount, int temperature) {
//                    if (results.length() > 0) results.append(',');
//                    final int total = schedule.getFitness();
//                    System.out.println(total+"\t"+globalBest+"\t"+currentBest+'\t'+bestCount+'\t'+currentStep+'\t'+iteration);
//                    results.append(total);
//
//                }
//            });
//            System.out.println("Penalty: "+schedule.getPenalty().getTotal());
//            results.append('\n');
//            try {
//                mResultsLogWriter.write(results.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }



        mSchedulePanel = new SchedulePanel(this);
        add(mSchedulePanel);

        mCurrentSchedule = new ConstructiveSchedule(mContext, 0, null, this);
    }

    public static void main(String[] args) {
        if (!Arrays.asList(args).contains("-ui")) {
            try {
                Schedule.Type method = Schedule.Type.Random;
                int iterations = 1;
                int studentHillClimbLoops = 0;
                int createSchedules = 101;
                int hillClimbLoops = 0;
                HillClimber.Type hillClimberType = HillClimber.Type.HillClimber;
                for (int i = 0; i < args.length; i++) {
                    if ("--method".equals(args[i])) {
                        method = Schedule.Type.valueOf(args[i + 1]);
                    } else if ("--schedules".equals(args[i])) {
                        createSchedules = Integer.parseInt(args[i + 1]);
                    } else if ("--iterations".equals(args[i])) {
                        iterations = Integer.parseInt(args[i + 1]);
                    } else if ("--shc".equals(args[i])) {
                        studentHillClimbLoops = Integer.parseInt(args[i + 1]);
                    } else if ("--hc".equals(args[i])) {
                        hillClimbLoops = Integer.parseInt(args[i + 1]);
                    } else if ("--hct".equals(args[i])) {
                        hillClimberType = HillClimber.Type.valueOf(args[i + 1]);
                        if (hillClimberType == HillClimber.Type.SimulatedAnnealing) {
                            hillClimberType.setInitialTemperature(Integer.parseInt(args[i + 2]));
                        }
                    }
                }
                int defaultStudentHillClimb = 10000;
                int defaultHillClimb = 150000;

                final boolean constr = false;
                final boolean hillClimber = true;
                final boolean simAnn = true;


                if (constr) {
                    new HeadlessApplication(method, iterations, studentHillClimbLoops, createSchedules, hillClimbLoops, hillClimberType);
                    new HeadlessApplication(Schedule.Type.Random, 1, 0, 100000, 0, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Random, 1, defaultStudentHillClimb, 100000, 0, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Constructive, 1, 0, 100000, 0, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Constructive, 1, defaultStudentHillClimb, 100000, 0, HillClimber.Type.HillClimber);
                }

                final int hcIterations = 50;
                if (hillClimber) {
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.HillClimberPlus);
                }
                if (simAnn) {
                    new HeadlessApplication(Schedule.Type.Random, 1, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(1));
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(10000));
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(50000));
                }
                if (hillClimber) {
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.HillClimberPlus);
                }
                if (simAnn) {
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(1));
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(10000));
                    new HeadlessApplication(Schedule.Type.Random, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(50000));
                }
                if (hillClimber) {
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.HillClimberPlus);
                }
                if (simAnn) {
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(1));
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(10000));
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, 0, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(50000));
                }
                if (hillClimber) {
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.HillClimber);
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.HillClimberPlus);
                }
                if (simAnn) {
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(1));
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(10000));
                    new HeadlessApplication(Schedule.Type.Constructive, hcIterations, defaultStudentHillClimb, 1, defaultHillClimb, HillClimber.Type.SimulatedAnnealing.setInitialTemperature(50000));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Application application = new Application();
                    application.setVisible(true);
                }
            });
        }
    }

    private void resetContext() {
//        mContext = DataProcessor.process(new FileReader(mCoursesFile), new FileReader(mStudentsFile));
    }

    public void redraw(Schedule schedule, boolean scheduleComplete) {
        mSchedulePanel.setComplete(scheduleComplete);
        mSchedulePanel.setPenalty(schedule.getPenalty());
        redraw();
    }

    @Override
    public void activityAdded(int roomSlot, Course.Activity activity) {
        mSchedulePanel.addActivity(roomSlot, activity);
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
            mHillClimber = new HillClimber(mCurrentSchedule, HillClimber.Type.HillClimber);
        System.out.println(Thread.currentThread().getId());
        mHillClimber.climb(stepSize, new HillClimber.Callback() {
            @Override
            public void done() {
                mSchedulePanel.setPenalty(mCurrentSchedule.getPenalty());
                redraw();
            }

            @Override
            public void swapped(List<Integer> swap) {
                for (Integer activityIndex : swap) {
                    activityAdded(mCurrentSchedule.getRoomSlot(activityIndex), mContext.getActivities().get(activityIndex));
                }
                mSchedulePanel.setPenalty(mCurrentSchedule.getPenalty());
                redraw();
            }
        });
    }
}
