package nl.uva.heuristiek;

import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Student;

import java.security.SecureRandom;
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

    public static int bestFitRoom(final int students) {
       for (int i = 0; i < Constants.ROOM_COUNT; i++) {
           if (students < Constants.ROOM_CAPACATIES[i]) return i;
       }
       throw new RuntimeException("No suitable room found");
   }

    public static void shuffleArray(Integer[] ar)
    {
        SecureRandom rnd = new SecureRandom();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
