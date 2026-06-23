# ComposeNavChart

A small Compose Multiplatform library for rendering animated NAV charts on Android and iOS.

The chart is extracted from a production portfolio graph and made reusable with neutral data models, configurable line styles, date labels, tooltips, and scrub interaction.

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
    implementation("com.github.samarthraj11:compose-nav-chart:0.2.0")
}
```

### Maven Local

For local development:

```bash
./gradlew :compose-nav-chart:publishToMavenLocal
```

Then consume it from another local project:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    google()
}

commonMain.dependencies {
    implementation("io.github.samarthraj11:compose-nav-chart:0.2.0")
}
```

The configured Maven group is `io.github.samarthraj11`.

## Usage

```kotlin
@Composable
fun PortfolioChart() {
    ComposeNavChart(
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

Publishing to Maven Central requires Central Portal user-token credentials and a signing key in Gradle properties or environment variables.

Required Gradle properties:

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=...
signingInMemoryKeyPassword=...
```

These can also be supplied as environment variables with the `ORG_GRADLE_PROJECT_` prefix.

## License

Apache License 2.0
