package nl.uva.heuristiek.data;

import com.opencsv.CSVReader;
import com.sun.istack.internal.NotNull;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;
import nl.uva.heuristiek.model.Student;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public class DataProcessor {

    private static Map<String, Course> mCourseMap;
    private static ArrayList<Student> mStudents;

    @NotNull
    public static Context process(File input, File vakken) {
        try {
            Context context = new Context();
            mCourseMap = new HashMap<>();
            mStudents = new ArrayList<>(660);

            CSVReader reader = new CSVReader(new FileReader(vakken));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                mCourseMap.put(nextLine[1], new Course(context, nextLine));
            }
            reader.close();

            reader = new CSVReader(new FileReader(input));

            while ((nextLine = reader.readNext()) != null) {
                Student student = new Student(context, nextLine);
                mStudents.add(student);
                for (int i = 3; i < nextLine.length; i++) {
                    Course course = mCourseMap.get(nextLine[i]);
                    if (course != null) {
                        course.addStudent(student);
                    }
                }
            }
            reader.close();

            context.init(mCourseMap, mStudents);
            return context;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
