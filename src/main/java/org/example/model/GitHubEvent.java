package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GitHubEvent {
    private String repo;
    private String commitId;
    private Commit commit;
    private Commit committer;
    private Owner owner;
    private int contributor;
    private String language;
    private String interval;  // e.g., "1h", "8h" (interval for last commits)

    @Getter
    @Setter
    public static class Commit {
        private String name;
        private String email;
        private LocalDateTime date;
    }

    @Getter
    @Setter
    public static class Owner {
        private String login;
        private String name;
    }
}
