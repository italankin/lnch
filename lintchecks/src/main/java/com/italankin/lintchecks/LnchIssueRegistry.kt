package com.italankin.lintchecks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.italankin.lintchecks.detectors.DescriptorJsonDetector
import com.italankin.lintchecks.detectors.PrefsDetector

@Suppress("unused")
class LnchIssueRegistry : IssueRegistry() {

    override val vendor: Vendor = Vendor("lnch")

    override val api = CURRENT_API

    override val issues: List<Issue> = listOf(
            DescriptorJsonDetector.ISSUE,
            PrefsDetector.ISSUE
    )
}
