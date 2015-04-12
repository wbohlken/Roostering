package nl.uva.heuristiek.data;

import com.opencsv.CSVReader;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by remco on 08/04/15.
 */
public class DataProcessor {
    public static Map<String, Course> process(File input, File vakken) {
        try {
            CSVReader reader = new CSVReader(new FileReader(vakken));
            String[] nextLine;
            HashMap<String, Course> courseMap = new HashMap<String, Course>();
            int courseId = 0;
            while ((nextLine = reader.readNext()) != null) {
                courseMap.put(nextLine[0], new Course(courseId++, nextLine));
            }
            reader.close();

            reader = new CSVReader(new FileReader(input));

            while ((nextLine = reader.readNext()) != null) {
                for (int i = 3; i < nextLine.length; i++) {
                    Course course = courseMap.get(nextLine[i]);
                    if (course != null) {
                        course.addStudent(new Student(nextLine));
                    }
                }
            }
            reader.close();

            return courseMap;
        } catch (IOException e) {
            return null;
        }
    }
}
