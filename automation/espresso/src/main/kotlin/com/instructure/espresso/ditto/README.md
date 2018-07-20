# Ditto
A wrapper around [OkReplay](https://github.com/airbnb/okreplay) that adds VCR-like functionality to our Espresso tests.

### How it works
Individual tests annotated with `@Ditto` will have the VCR functionality enabled and can run in one of three modes: **Play**, **Record**, and **Live**. If not specified, tests will run in **Play** mode:

```kotlin
@Test
@Ditto
fun testOne() {
  // Runs in Play mode
}

@Test
@Ditto(mode = DittoMode.RECORD)
fun testTwo() {
  // Runs in Record mode
}
```
NOTE: **All committed tests should use Play mode** except in rare cases.

In **Play** mode, the responses to network requests are all read from a YAML tape file stored in the app's assets. Tape files are not shared between tests. These files are highly readable and can be manually edited if necessary. If the app attempts to make a network request in Play mode and there is no recorded response on the tape, the test will fail and a helpful message will be printed.

In **Record** mode, live network requests and responses are written to tape files located on the device at `/sdcard/okreplay/tapes/<application-id>`. If the application project is set up with the OkReplay gradle plugin, use the `pullOkReplayTapes` task to copy these files from the device to the project's assets directory. You may also use to `clearOkReplayTapes` task to delete the files on device. Note that recorded tapes will be discarded for failed tests.

In general you will only use this mode while adding or modifying tests and then revert back to Play mode before committing your changes.

In **Live** mode, VCR functionality is disabled and tests will run normally using live data.

---
### Global Mode
Sometimes you may want to change the mode for all `@Ditto` tests, for example to re-record all tests or confirm that the test suite still works with a live server. In these cases you can specify a 'global' mode when building the app. This can be specified either in code - by setting the mode of `dittoConfig` in `InstructureTest.kt` - or by specifying the `dittoMode` gradle property (one of 'play', 'record', or 'live') when building from the command line, `e.g.:
```
./gradlew :app:assembleQaDebugAndroidTest -PdittoMode=record
```
### Class Mode
Other times you may want to set the mode for all tests in a single class. You can do this by setting the `@DittoClassMode` annotation on the class itself and passing in the desired mode.

### Mode Priority
The global and class modes don't necessarily override the individual test mode or vice versa. Rather, the mode with the highest 'priority' is used:
 - If either the global mode, class mode, or individual test mode is **Live** then the test will run in **Live** mode.
 - Otherwise, if any mode is **Record** then the test will run in **Record** mode.
 - Otherwise, the test will run in **Play** mode.
 
 ---
## Recording generated test data
Nearly all of our UI tests rely on data seeding to populate the server with mock data prior to test execution. The generated data is unique to that specific test run, so it is necessary to record this data alongside the rest of the test's network requests. To solve this, simply wrap the call to seed data in a `mockableSeed` block.

For example, this code:
```kotlin
return InProcessServer.generalClient.seedParentData(request)

```
would become:
```kotlin
return mockableSeed { InProcessServer.generalClient.seedParentData(request) }
```

In **Record** mode the body of `mockableSeed` will be executed and its result recorded to tape. In **Play** mode the body is ignored and a previously-recorded result will be read from tape.

There is also the option to store arbitrary String data using `mockableString`. For this you'll need to provide a unique label to help disambiguate this data from other mocked String data on the tape:
```kotlin
val newCourseName = mockableString("new course name") { randomString() }
```
---
## Modifying responses
There may be cases where parts of recorded data become stale or invalid after a while. For example, some tests are sensitive to the device's current date and will fail if too much time has passed since the test was originally recorded (e.g. An assignment's state might change from 'unsubmitted' to 'missing' after a certain date).

Rather than re-record these tests every so often, we can instead use `DittoResponseMod` to modify all or part of the recorded responses for specific requests. As of right now there is only one implementation, `JsonObjectResponseMod`. To use it, create a new instance and register it at the beginning of the test via `addDittoMod`.

Here is an example taken from `AssignmentDetailsPageTest` in Teacher App which modifies the lock date of an assignment:
```kotlin
@Test
@Ditto
fun displaysNoFromDate() {
    val lockAt = 7.days.fromNow.iso8601
    addDittoMod(JsonObjectResponseMod(
        Regex("""(.*)/api/v1/courses/\d+/assignments/\d+\?(.*)"""),
        JsonObjectValueMod("lock_at", lockAt),
        JsonObjectValueMod("all_dates[0]:lock_at", lockAt)
    ))
    getToAssignmentDetailsPage(lockAt = lockAt)
    assignmentDetailsPage.assertToFilledAndFromEmpty()
}
```

Refer to the source code for `DittoResponseMod` and `JsonObjectResponseMod` for more information on response modification.
