package nl.uva.heuristiek;

import nl.uva.heuristiek.algorithm.HillClimber;
import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.ga.Config;
import nl.uva.heuristiek.ga.ScheduleGeneticAlgorithm;
import nl.uva.heuristiek.model.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by remco on 19/05/15.
 */
public class HeadlessApplication {

    private Context mContext;
    private HillClimber mHillClimber;
    private Schedule mBestSchedule;

    public HeadlessApplication(Schedule.Type method, int iterations, int studentHillClimbLoops, int createSchedules, int hillClimbLoops, HillClimber.Type hillClimberType) throws IOException {
        final InputStream coursesIS = ClassLoader.getSystemResourceAsStream("vakken.csv");
        final InputStream studentsIS = ClassLoader.getSystemResourceAsStream("studenten_roostering.csv");
        mContext = DataProcessor.process(new InputStreamReader(coursesIS), new InputStreamReader(studentsIS));


        final String date = new SimpleDateFormat("yy-MM-dd_hh_mm_ss").format(new Date());
        File fullHillClimberLogFile = new File("results", String.format("hc_full_%s_i_%d_shc_%d_s_%d_hc_%d_hct_%s_%d.csv",
                method, iterations, studentHillClimbLoops, createSchedules, hillClimbLoops, hillClimberType, hillClimberType.getInitialTemperature()));
        //noinspection ResultOfMethodCallIgnored
        fullHillClimberLogFile.createNewFile();
        File bestHillClimberLogFile = new File("results", String.format("hc_best_%s_i_%d_shc_%d_s_%d_hc_%d_hct_%s_%d.csv",
                method, iterations, studentHillClimbLoops, createSchedules, hillClimbLoops, hillClimberType, hillClimberType.getInitialTemperature()));
        //noinspection ResultOfMethodCallIgnored
        bestHillClimberLogFile.createNewFile();
        File scheduleCreationLogFile = new File("results", String.format("c_%s_i_%d_shc_%d_s_%d_hc_%d_hct_%s_%d.csv",
                method, iterations, studentHillClimbLoops, createSchedules, hillClimbLoops, hillClimberType, hillClimberType.getInitialTemperature()));
        //noinspection ResultOfMethodCallIgnored
        fullHillClimberLogFile.createNewFile();
        FileWriter schedulesCreationLogWriter = new FileWriter(scheduleCreationLogFile);
        FileWriter fullHillClimberLogWriter = new FileWriter(fullHillClimberLogFile);
        FileWriter bestHillClimberLogWriter = new FileWriter(fullHillClimberLogFile);

        Config gaConfig = null;

        HashMap<Integer, Integer> scores = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            if (gaConfig != null) {
                ScheduleGeneticAlgorithm algorithm = new ScheduleGeneticAlgorithm(mContext, gaConfig);
                algorithm.doLoops(1000);
            }
            for (Course course : mContext.getCourseMap().values()) {
                course.hillclimbStudents(studentHillClimbLoops);
            }
            final int size = mContext.getActivities().size();
            int bestFitness = 0;
            for (int scheduleIteration = 0; scheduleIteration < createSchedules; scheduleIteration++) {
                Schedule schedule;
                switch (method){
                    case Constructive:
                        schedule = new ConstructiveSchedule(mContext, 0, null, null);
                        break;
                    case Random:
                        schedule = new RandomSchedule(mContext, 0, null);
                        break;
                    default:
                        throw new RuntimeException("Schedule type may not be null");
                }
                schedule.doSteps(size);
                int fitness = schedule.getFitness();
                Integer count = scores.get(fitness);
                scores.put(fitness, (count == null ? 0 : count) + 1);
                if (mBestSchedule == null) {
                    mBestSchedule = schedule;
                    bestFitness = fitness;
                } else {
                    if (fitness > bestFitness) {
                        mBestSchedule = schedule;
                        bestFitness = fitness;
                    }
                }
                System.out.printf("%d schedules created\n", scheduleIteration + 1);
            }

            schedulesCreationLogWriter.write("fitness,occurences\n");
            for (Integer fitness : scores.keySet()) {
                schedulesCreationLogWriter.write(fitness + "," + scores.get(fitness) + "\n");
            }

            HillClimber hillClimber = new HillClimber(mBestSchedule, hillClimberType);
            final StringBuilder results = new StringBuilder(hillClimbLoops *5);
            final int iteration = i;
            hillClimber.climb(hillClimbLoops, new HillClimber.SyncCallback() {
                @Override
                public void publish(Integer[] swap, int currentStep, int currentBest, int globalBest, int bestCount, int temperature) {
                    if (results.length() > 0) results.append(',');
                    final int total = mBestSchedule.getFitness();
                    System.out.println(total+"\t"+globalBest+"\t"+currentBest+'\t'+bestCount+'\t'+currentStep+'\t'+iteration+'\t'+temperature);
                    results.append(total);

                }
            });
            System.out.println("Penalty: " + mBestSchedule.getPenalty().getTotal());
            results.append('\n');
            fullHillClimberLogWriter.write(results.toString());
            bestHillClimberLogWriter.write(mBestSchedule.getFitness() + "\n");
            resetContext();
        }
        fullHillClimberLogWriter.close();
        schedulesCreationLogWriter.close();
    }

    private void resetContext() {
        final InputStream coursesIS = ClassLoader.getSystemResourceAsStream("vakken.csv");
        final InputStream studentsIS = ClassLoader.getSystemResourceAsStream("studenten_roostering.csv");
        mContext = DataProcessor.process(new InputStreamReader(coursesIS), new InputStreamReader(studentsIS));
    }
}
