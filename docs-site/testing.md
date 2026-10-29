# Testing

Three layers, enforced for every change (see `CONTRIBUTING.md` in the repository):

1. **Unit tests** for every piece of logic, written once in `commonTest` and run on every
   platform (JVM, Android host, iOS simulator, JS, Wasm).
2. **Compose UI tests** for every interactive or stateful composable (`runComposeUiTest`,
   semantics-driven assertions and clicks).
3. **[Roborazzi](https://github.com/takahirom/roborazzi) screenshot tests** for shared
   design-system components. Goldens are committed under
   `shared/src/androidHostTest/roborazzi/` and verified in CI on every push.

## Commands

```sh
./gradlew :shared:jvmTest                        # desktop JVM
./gradlew :shared:testAndroidHostTest            # Android host (incl. screenshot tests)
./gradlew :shared:wasmJsTest :shared:jsTest      # web
./gradlew :shared:iosSimulatorArm64Test          # iOS
./gradlew :shared:verifyRoborazziAndroidHostTest # screenshot verify only
./gradlew :shared:recordRoborazziAndroidHostTest # re-record goldens
```

Run a single test class with Gradle's filter:

```sh
./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.ui.screens.auth.AccountScreenContentTest"
```

## Conventions

- ViewModels are tested with fakes (`FakeAuthRepository`, in-memory stores), never mocks of
  framework types.
- Screenshot tests that render async content (e.g. markdown) wait for a semantic marker before
  capturing, so goldens are deterministic.
- CI (`.github/workflows/ci.yml`) runs the JVM suite, the Android host suite with Roborazzi
  verification, and an Android assemble on every push and pull request.
