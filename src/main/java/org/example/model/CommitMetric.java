package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommitMetric {
    private String committer;
    private String language;
    private String commitId;
}
