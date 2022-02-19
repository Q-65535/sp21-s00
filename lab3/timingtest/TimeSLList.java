package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        int endVolume = 128000;
        for (int curVolume = 1000; curVolume <= endVolume; curVolume *= 2) {
            SLList<Integer> testSLList = new SLList<>();
            // add N items
            for (int i = 0; i < curVolume; i++) {
                testSLList.addLast(3);
            }

            // perform getLast operation 10000 times and time it
            Stopwatch sw = new Stopwatch();
            int ops = 0;
            for (int i = 0; i < 10000; i++) {
                testSLList.getLast();
                ops++;
            }
            double timeInSeconds = sw.elapsedTime();

            Ns.addLast(curVolume);
            times.addLast(timeInSeconds);
            opCounts.addLast(ops);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
