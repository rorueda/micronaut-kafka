# Micronaut Kafka

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.configuration/micronaut-kafka.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.configuration%22%20AND%20a:%22micronaut-kafka%22)
[![](https://github.com/micronaut-projects/micronaut-kafka/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-kafka/actions)

This project includes integration between [Micronaut](http://micronaut.io) and [Kafka](https://kafka.apache.org).

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-kafka/latest/guide) for more information.

## Snapshots and Releases

Snaphots are automatically published to JFrog OSS using [Github Actions](https://github.com/micronaut-projects/micronaut-data/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to JCenter and Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-data/actions).

A release is performed with the following steps:

* Change the version specified by `projectVersion` in `gradle.properties` to a semantic, unreleased version. Example `1.0.0`
* Commit the change. Example: `git commit -m "Release Version 1.0.0"`
* Tag the release where the tag starts with `v`. Example: `git tag v1.0.0`
* Push the commit and the tag: `git push && git push --tags`