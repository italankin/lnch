package com.italankin.lintchecks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.italankin.lintchecks.detectors.JsonModelDetector

@Suppress("unused")
class LnchIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues: List<Issue> = listOf(
        JsonModelDetector.ISSUE
    )
}
