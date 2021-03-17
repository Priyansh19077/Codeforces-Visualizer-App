package com.example.codeforcesviewer.UserData.SubmissionData

data class Submission(
        val author: Author,
        val contestId: Int,
        val creationTimeSeconds: Long,
        val id: Int,
        val memoryConsumedBytes: Long,
        val passedTestCount: Int,
        val problem: Problem,
        val programmingLanguage: String,
        val relativeTimeSeconds: Int,
        val testset: String,
        val timeConsumedMillis: Long,
        val verdict: String
)