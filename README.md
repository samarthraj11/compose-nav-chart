# Net Asset Value Graph

A small Compose Multiplatform library for rendering animated Net Asset Value line graphs on Android and iOS.

The graph is extracted from a production portfolio chart and made reusable with neutral data models, configurable line styles, date labels, tooltips, and scrub interaction.

## Install

### JitPack

Add JitPack to your repositories:

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}
```

Then add the dependency:

```kotlin
commonMain.dependencies {
    implementation("com.github.samarthraj11:netassetvalue-graph:0.1.1")
}
```

### Maven Local

For local development:

```bash
./gradlew :netassetvalue-graph:publishToMavenLocal
```

Then consume it from another local project:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    google()
}

commonMain.dependencies {
    implementation("io.github.samarthraj11:netassetvalue-graph:0.1.1")
}
```

The configured Maven group is `io.github.samarthraj11`.

## Usage

```kotlin
@Composable
fun PortfolioChart() {
    NetAssetValueGraph(
        series = listOf(
            NavSeries(
                name = "Fund",
                points = listOf(
                    NavPoint(timestampMillis = 1_767_225_600_000L, value = 100.0),
                    NavPoint(timestampMillis = 1_769_904_000_000L, value = 108.4),
                    NavPoint(timestampMillis = 1_772_323_200_000L, value = 112.1),
                ),
            ),
            NavSeries(
                name = "Benchmark",
                points = listOf(
                    NavPoint(timestampMillis = 1_767_225_600_000L, value = 100.0),
                    NavPoint(timestampMillis = 1_769_904_000_000L, value = 105.2),
                    NavPoint(timestampMillis = 1_772_323_200_000L, value = 109.7),
                ),
            ),
        ),
        onScrubChange = { points ->
            // Update legend values while the user drags over the chart.
        },
    )
}
```

## Publishing

This project is configured with Gradle `maven-publish`.

Local repository:

```bash
./gradlew :netassetvalue-graph:publishAllPublicationsToLocalBuildRepository
```

Maven local:

```bash
./gradlew :netassetvalue-graph:publishToMavenLocal
```

For Maven Central, use the configured `io.github.samarthraj11` coordinates and add your Central Portal credentials plus signing key through your preferred release pipeline.

## License

Apache License 2.0
