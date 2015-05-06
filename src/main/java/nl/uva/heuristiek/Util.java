package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Util {
    public static int tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Collection<Course> deepClone(Collection<Course> courseCollection) {
        Collection<Course> cloned = new HashSet<Course>(courseCollection.size());
        for (Course course : courseCollection)
            cloned.add(new Course(course));
        return cloned;
    }

    public static Set<Student> deepClone(Set<Student> studentCollection) {
        if (studentCollection == null) return null;
        Set<Student> cloned = new HashSet<Student>(studentCollection.size());
        for (Student student : studentCollection)
            cloned.add(new Student(student));
        return cloned;
    }

    public static void generateCsvFile(String sFileName, ArrayList<ArrayList<Integer>> values)
    {
        try
        {
            FileWriter writer = new FileWriter(sFileName);

            writer.append("Total");
            writer.append(',');
            writer.append("Student");
            writer.append(',');
            writer.append("Course");
            writer.append('\n');

            for (ArrayList<Integer> row : values) {
                for (int column : row) {
                    writer.append(String.valueOf(column));
                    writer.append(',');
                }
                writer.append('\n');
            }

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
