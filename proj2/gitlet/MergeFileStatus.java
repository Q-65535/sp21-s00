package gitlet;

public enum MergeFileStatus {

    UNCHANGED("unchanged"),
    MODIFIED("modified"),
    DELETED("deleted"),
    ADDED("added");

    private String name;
    MergeFileStatus(String name) {
        this.name = name;
    }
}
