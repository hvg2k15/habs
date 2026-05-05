package com.habs.domain.model

import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    /** Day this task is due (yyyy-MM-dd). Shown on Today until done or overdue. */
    val dueDateKey: String,
    /** Optional time on the due day; null means no specific deadline time. */
    val dueTime: LocalTime? = null,
    val isCompleted: Boolean = false,
    /** When marked done, set to that calendar day key. */
    val completedOnKey: String? = null,
    val calendarSynced: Boolean = false,
    val calendarEventId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
