# Contributing

## Local Checks

```bash
./gradlew :compose-nav-chart:build
```

## Publishing

This section is for maintainers.

Maven local:

```bash
./gradlew :compose-nav-chart:publishToMavenLocal
```

Maven Central manual release:

```bash
./gradlew :compose-nav-chart:publishToMavenCentral
```

Maven Central automatic release:

```bash
./gradlew :compose-nav-chart:publishAndReleaseToMavenCentral
```

Publishing to Maven Central requires Central Portal user-token credentials and a signing key.

Required Gradle properties:

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=...
signingInMemoryKeyPassword=...
```

These can also be supplied as environment variables with the `ORG_GRADLE_PROJECT_` prefix.
