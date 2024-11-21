package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommitMessage {
    private String commitId;
    private String repo;
    private String username;
    private LocalDateTime commitDate;
    private String language;
}
