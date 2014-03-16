package cz.mtheory.example.tool;

import java.util.Random;

public class Rnd {

    private static final Random random = new Random();

    private Rnd() {
        //utility class
    }
    
    public static void reseed(){
        random.setSeed(System.nanoTime());
    }

    public static int nextInt(int min, int max) {
        if (min == max) return min;
        if (min > max) throw new IllegalArgumentException("min is greater then max");
        return random.nextInt(max - min + 1) + min;
    }

    public static float nextFloat(int min, int max) {
        if (min == max) return min;
        int r = nextInt(min, max - 1);
        float f = random.nextFloat();
        return r + f;
    }

    public static double nextDouble(int min, int max) {
        if (min == max) return min;
        int r = nextInt(min, max - 1);
        double f = random.nextDouble();
        return r + f;
    }

}
