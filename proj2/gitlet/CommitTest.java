package gitlet;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommitTest {
    @Test
    public void testDate() {
        Date date = new Date(0);
        System.out.println(date.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println(sdf.format(date));
    }

    @Test
    public void testCommitInfo() {
        Commit commit = Commit.initialCommit();
        System.out.println(commit.commitInfoStr());
    }

    @Test
    public void testGlobalLog() {
        StringBuilder sb = new StringBuilder();
        System.out.println(sb.length());
    }
}
