package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

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

}
