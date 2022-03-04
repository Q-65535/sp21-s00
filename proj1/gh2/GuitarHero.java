package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {

    public static void main(String[] args) {
        final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        double[] concerts = new double[keyboard.length()];
        // construct the concerts according to the formula
        for (int i = 0; i < concerts.length; i++) {
            concerts[i] = 440 * Math.pow(2, (i - 24) / 12);
        }
        // map: character --> its index
        Integer[] map = new Integer[256];
        for (int i = 0; i < keyboard.length(); i++) {
            char index = keyboard.charAt(i);
            map[index] = i;
        }

        GuitarString[] strings = new GuitarString[concerts.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new GuitarString(concerts[i]);
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
