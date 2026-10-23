# HW4 Jobs walkthrough and recording guide

This project implements the HW4 delayed-loading example and handcuff launcher icon. The saved version uses `repeatOnLifecycle`. The manual Job versions are commented out in `CrimeListFragment.kt` so you can demonstrate each experiment without running competing loaders. You still need to record and submit your own 5-10 minute video link.

## Files to show

- `app/src/main/java/com/example/criminalintent/CrimeListViewModel.kt`: asynchronous loading, five-second delay, initial size log, completed size log.
- `app/src/main/java/com/example/criminalintent/CrimeListFragment.kt`: view binding, lifecycle-aware loading, manual lifecycle alternatives, and cancellation logs.
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`: adaptive launcher foreground, cyan background, and monochrome layer.
- `app/src/main/res/mipmap-*/`: legacy launcher images and adaptive foreground images for five screen densities.

## Launcher icon demonstration

The installed icon uses the supplied black handcuffs on cyan (`#00FFFF`), matching the homework example. The image remains centered within the adaptive icon safe area. API 24/25 have legacy launcher images; API 26+ use the adaptive XML resources. The manifest already references `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`, so no manifest change was needed.

To demonstrate Android Studio's asset workflow yourself: right-click `app/src/main/res`, select **New > Image Asset**, choose **Launcher Icons (Adaptive and Legacy)**, and use the supplied `ic_handcuffs.png` as the foreground image. Center it with padding so the handcuffs fit inside the safe zone. Set the background to cyan `#00FFFF`; inspect the round and square previews. Use `ic_launcher` as the name. If you finish the wizard it regenerates the launcher resources, so review the previews before replacing the existing assets. Install and show the app in the launcher. The homework's video should include you demonstrating this process.

## 1. Show the final lifecycle-aware version

In `onViewCreated`, `viewLifecycleOwner.lifecycleScope.launch` starts a coroutine tied to this fragment's **view**. Inside it, `repeatOnLifecycle(Lifecycle.State.STARTED)` runs the loading block when the view is STARTED or RESUMED, cancels that block when it drops below STARTED, and launches a fresh block when it reaches STARTED again. Set it up once per view in `onViewCreated`.

The code directly awaits `crimeListViewModel.loadCrimes()` and assigns `CrimeListAdapter(crimes)` inside the lifecycle coroutine, matching video 12.2. The extra `Displayed 100 crimes` log is only HW4 instrumentation. There is no helper function or cancellation catch: normal coroutine cancellation propagates automatically. If you leave after loading has already finished, there is no unfinished load left to cancel.

This is a one-shot suspending loader inside the repeating block, not a Flow collector. Each new STARTED interval calls the loader again and waits five seconds. The ViewModel's separate `crimes` list is retained across configuration changes, but this exercise deliberately calls `loadCrimes()` again for the displayed list, as in the supplied PDF.

## 2. Switch to onStart/onStop Jobs

In `CrimeListFragment.kt`:

1. Comment out the entire `onViewCreated` method labeled **FINAL VERSION**.
2. Uncomment `import kotlinx.coroutines.Job`.
3. Uncomment `private var job: Job? = null`.
4. Remove only the `/*` and `*/` surrounding **HW4 EXPERIMENT 1**.
5. For the initial size-log experiment, also remove the block comment around **HW4 OBSERVATION** (`onCreate`). Video 12.2 removed this method, so it is only restored temporarily for HW4.
6. Leave experiment 2 commented out. Build and run.

`launch` creates and starts a coroutine immediately by default and returns its `Job`. The nullable `job` variable stores that handle. `onStop` calls `cancel()` and clears the reference. A canceled or completed Job is not restarted: the next `onStart` calls `launch` to create a new one.

## 3. Explain the size logs and delay

Use this Logcat filter to show the fragment first:

```text
package:com.example.criminalintent tag:CrimeListFragment
```

Then include both tags (parentheses make the OR explicit):

```text
package:com.example.criminalintent (tag:CrimeListFragment | tag:CrimeListViewModel)
```

With the HW4 OBSERVATION method enabled and a fresh app process, `CrimeListViewModel` starts with an empty mutable list. Its init coroutine enters `loadCrimes()` and suspends at `delay(5000)`. The constructor keeps going, so its end-of-init log and the fragment's `onCreate` log both report **0**. About five seconds later the initial load completes and the ViewModel logs **100**. The fragment's own load also returns 100 and installs an adapter.

The initial ViewModel load and the fragment's display load are **two separate coroutine calls**, matching video 12.2 and the PDF. They produce different lists; this is a teaching example, not a shared repository cache. The ViewModel completion log and UI display log describe different calls. The unused `CrimeRepository.get()` line in the PDF is not needed for this standalone example; the database begins in video 12.3.

Clear Logcat before a new run, then stop and rerun the app. To guarantee a fresh ViewModel for the zero-count demonstration, terminate the app before rerunning. Simply returning from the launcher may preserve the Activity and ViewModel, in which case `onCreate` may not run at all. A configuration change can create a new fragment while retaining its ViewModel, so a new fragment can then log 100.

`delay` simulates a slow data source and provides time to leave the screen before loading finishes. It suspends the coroutine without freezing the UI thread. It is also cancellable, so `job.cancel()` during the delay prevents the later adapter update. It does not create a background thread by itself; real blocking I/O would need appropriate dispatching.

## 4. Temporarily remove onStop cancellation

With experiment 1 active, comment out only its `onStop` override. Start the app and press **Home within five seconds**. Stay on the launcher longer than five seconds while watching Logcat.

The fragment's load can complete and log `Displayed 100 crimes` while the app is stopped because its view still exists. Its scope is tied to **view destruction**, not merely to `onStop`. Returning triggers another `onStart` and a new Job; the old one is not canceled just because the variable gets overwritten. Rapid background/foreground transitions can leave overlapping loads.

If the view is actually destroyed, `viewLifecycleOwner.lifecycleScope` cancels the job even without your `onStop`. Removing `onStop` therefore does not guarantee a crash, nor does it guarantee that every load keeps running. Show the lifecycle that actually occurs on your device. Restore the `onStop` method after this experiment.

## 5. Restore onStop, then leave and reopen

With experiment 1 fully restored, start a load and press **Back within five seconds**. Reopen the app using its new handcuff icon. Show:

```text
Job launching in onStart
Job getting canceled in onStop
Job launching in onStart
Displayed 100 crimes
```

Wait five seconds after the last launch for the final message. There is no custom canceled-load message in this lecture-aligned version. To observe cancellation, leave before five seconds and verify that no `Displayed 100 crimes` message occurs for the pending UI load. If you press Back after the job completes, cancellation does not undo the displayed rows. Some newer Android versions background a root launcher Activity on Back rather than destroying it. Home reliably demonstrates stopping without destruction. Explain the observed callbacks rather than assuming Back always destroys the Activity.

The ViewModel initialization load belongs to `viewModelScope`, so it may finish while the UI job is canceled. Its scope is canceled when the ViewModel is cleared, not at every `onStop`.

## 6. Try the sister lifecycle pair

Comment out experiment 1 and keep `onViewCreated` commented. Uncomment experiment 2's `onResume` and `onPause` methods. Keep the Job import and property active. Build and run; leave during the delay, then return. Show the `onResume` launch, `onPause` cancellation, and a fresh load on return.

These are **Fragment** callbacks because the list lives in a Fragment. The same lifecycle pairing idea applies to an Activity. The resume/pause pair requires the screen to be RESUMED for work to continue; start/stop permits work while STARTED. A screen can remain STARTED when it is no longer RESUMED, so the two policies are not identical.

## 7. Restore repeatOnLifecycle and explain the result

Comment both manual experiment blocks again, comment the `job` property and `Job` import, comment the temporary **HW4 OBSERVATION** `onCreate` override, and uncomment the final `onViewCreated`. This retains the video 12.2 cleanup. Run once more and show 100 crimes after the delay.

A Job represents the lifetime and cancellation state of a coroutine. It lets you cancel, inspect state, or wait for completion; a Job is not a dedicated thread. Cancellation is cooperative, and `delay` cooperates. Canceling a parent also cancels its child jobs.

`repeatOnLifecycle` still uses coroutines and Jobs internally. It replaces your manual job field and callback bookkeeping with automatic cancellation and relaunch at the chosen lifecycle state. The outer view lifecycle scope is canceled when the view is destroyed, which protects view binding. The benefit is fewer opportunities to omit cancellation or start overlapping lifecycle-controlled UI work.

## Suggested 8-minute video order

| Time | Demonstration |
| --- | --- |
| 0:00-1:00 | Show the icon asset process and installed launcher icon. |
| 1:00-2:00 | Explain the final repeatOnLifecycle method, then enable experiment 1. |
| 2:00-3:15 | Show fresh-process size logs, the five-second delay, and 100 loaded rows. |
| 3:15-4:30 | Remove onStop, press Home during the delay, and show loading continuing. |
| 4:30-5:45 | Restore onStop; show Back, cancellation, icon relaunch, and the new load. |
| 5:45-7:00 | Switch to onResume/onPause and demonstrate cancellation and reload. |
| 7:00-8:00 | Explain Jobs, restore repeatOnLifecycle, and show the final working list. |

Record your own observations and use these explanations to describe what you see. Submit your own video link through the course's submission process.

## References

- [Android repeatOnLifecycle reference](https://developer.android.com/reference/androidx/lifecycle/RepeatOnLifecycleKt)
- [Android LifecycleCoroutineScope reference](https://developer.android.com/reference/androidx/lifecycle/LifecycleCoroutineScope)
- [Kotlin Job reference](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/)
- [Kotlin coroutine cancellation](https://kotlinlang.org/docs/coroutines-cancellation.html)

- [Android Image Asset Studio](https://developer.android.com/studio/write/create-app-icons)


## Verification after rebuilding

All five source checkpoints compiled successfully: the original 10.7 baseline, video 12.1, video 12.2, the reapplied in-class activity, and the reapplied HW4 version.

| Runtime check | Observed result |
| --- | --- |
| Video 12.1 | Loading finished after the delay, but no crime rows appeared, matching the lecture's intentional ordering bug. |
| Video 12.2 | The list appeared after loading. Rotation created a fresh loading interval, then the rows returned in landscape. Rotation settings were restored afterward. |
| In-class activity | Title/date layout displayed correctly; handcuffs appeared on the visible solved rows and were absent from unsolved rows. |
| Final repeatOnLifecycle | Leaving during the delay prevented UI completion; returning loaded and displayed 100 crimes. |
| Manual onStart/onStop | Initial fragment and ViewModel logs showed 0; onStop canceled UI work, and returning created a fresh load. |
| Manual Job without onStop | The UI load completed while the app was in the background and its view still existed. |
| Manual onResume/onPause | onPause canceled UI work, and returning started a fresh load. |
| Final launcher | The cyan handcuff icon opened the restored final app, which displayed its list. |

After the experiments, the final source was restored exactly and rebuilt. Only repeatOnLifecycle is active; manual Jobs and the temporary onCreate observation override are commented out. The in-class files still match commit aa3049c. Git HEAD remains aa3049c, and the staging index is unchanged. No commit or push was made.

Final Android lint result: **0 errors, 14 warnings**. The warnings are advisory dependency/version, unused-resource, bitmap-density, and version-catalog notices. The updated sources and runtime logs are saved locally; the source checkpoints exclude generated build outputs.