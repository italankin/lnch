package com.italankin.lintchecks.detectors

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.intellij.lang.annotations.Language
import org.junit.Test

class ActivityResultDetectorTest {

    @Test
    fun basicScenario() {
        @Language("java")
        val testData = """
                import android.app.Activity;
                import android.content.Intent;

                class MyActivity extends Activity {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                        if (requestCode == Activity.RESULT_OK) {
                        }
                        if (requestCode == Activity.RESULT_CANCELED) {
                        }
                    }
                }
            """.trimIndent()
        val expectedText = """
                |src/MyActivity.java:7: Error: requestCode should not be compared with RESULT_OK [ActivityResultDetector]
                |        if (requestCode == Activity.RESULT_OK) {
                |            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |src/MyActivity.java:9: Error: requestCode should not be compared with RESULT_CANCELED [ActivityResultDetector]
                |        if (requestCode == Activity.RESULT_CANCELED) {
                |            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |2 errors, 0 warnings
            """.trimMargin()
        TestLintTask.lint()
                .files(java(testData))
                .issues(ActivityResultDetector.ISSUE)
                .run()
                .expect(expectedText)
    }

    @Test
    fun quickfix() {
        @Language("java")
        val testData = """
                import android.app.Activity;
                import android.content.Intent;

                class MyActivity extends Activity {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                        if (requestCode == Activity.RESULT_OK) {
                        }
                    }
                }
            """.trimIndent()
        val expectedFixDiffs = """
                |Fix for src/MyActivity.java line 7: Replace with resultCode:
                |@@ -7 +7
                |-         if (requestCode == Activity.RESULT_OK) {
                |+         if (resultCode == Activity.RESULT_OK) {
            """.trimMargin()
        TestLintTask.lint()
                .files(java(testData))
                .issues(ActivityResultDetector.ISSUE)
                .run()
                .expectErrorCount(1)
                .expectFixDiffs(expectedFixDiffs)
    }

    @Test
    fun `wrong comparison with changed parameter names`() {
        @Language("java")
        val testData = """
                import android.app.Activity;
                import android.content.Intent;

                class MyActivity extends Activity {
                    @Override
                    public void onActivityResult(int var1, int var2, Intent data) {
                        if (var1 == Activity.RESULT_OK) {
                        }
                        if (var1 == Activity.RESULT_CANCELED) {
                        }
                    }
                }
            """.trimIndent()
        val expectedText = """
                |src/MyActivity.java:7: Error: var1 should not be compared with RESULT_OK [ActivityResultDetector]
                |        if (var1 == Activity.RESULT_OK) {
                |            ~~~~~~~~~~~~~~~~~~~~~~~~~~
                |src/MyActivity.java:9: Error: var1 should not be compared with RESULT_CANCELED [ActivityResultDetector]
                |        if (var1 == Activity.RESULT_CANCELED) {
                |            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |2 errors, 0 warnings
            """.trimMargin()
        TestLintTask.lint()
                .files(java(testData))
                .issues(ActivityResultDetector.ISSUE)
                .run()
                .expect(expectedText)
    }

    @Test
    fun `clean code - no errors`() {
        @Language("java")
        val testData = """
                import android.app.Activity;
                import android.content.Intent;

                class MyActivity extends Activity {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                        }
                    }
                }
            """.trimIndent()
        TestLintTask.lint()
                .files(java(testData))
                .issues(ActivityResultDetector.ISSUE)
                .run()
                .expectClean()
    }

}