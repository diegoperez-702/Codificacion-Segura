package com.example.infrastructure.analysis;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaticCodeAnalysisReport implements Sensor {

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Static Code Analysis Report");
    }

    @Override
    public void execute(SensorContext context) {
        FileSystem fileSystem = context.fileSystem();
        Iterable<InputFile> files = fileSystem.inputFiles(fileSystem.predicates().all());
        for (InputFile file : files) {
            if (file.language().equals("java")) {
                NewIssue issue = context.newIssue()
                       .forRule(RuleKey.of("example", "RuleKey"));
                NewIssueLocation primaryLocation = issue.newLocation()
                       .on(file)
                       .message("Potential security vulnerability detected.");
                issue.at(primaryLocation)
                        .save();
            }
        }
    }
}
