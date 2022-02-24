package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";

    public static void main(String[] args) {
        double[] CONCERTS = new double[keyboard.length()];
        for (int i = 0; i < CONCERTS.length; i++) {
            CONCERTS[i] = 440 * Math.pow(2, (i - 24) / 12);
        }
        Integer[] map = new Integer[255];
        for (int i = 0; i < keyboard.length(); i++) {
            char index = keyboard.charAt(i);
            map[index] = i;
        }

        GuitarString[] strings = new GuitarString[CONCERTS.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new GuitarString(CONCERTS[i]);
        }

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (map[key] == null) {
                    continue;
                }
                int index = map[key];
                strings[index].pluck();
            }
            /* compute the superposition of samples */
            double sample = 0;
            for (GuitarString string : strings) {
                sample += string.sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (GuitarString string : strings) {
                string.tic();
            }
        }


    }
}
