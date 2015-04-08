package nl.uva.heuristiek;

import nl.uva.heuristiek.data.DataProcessor;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Application {
    public static void main(String[] args) {
        try {
            File courses = new File("vakken.csv");
            File students = new File("studenten_roostering.csv");
            Map<String, Course> courseMap = DataProcessor.process(students, courses);
            Schedule schedule = new Schedule(courseMap.values());
            schedule.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
