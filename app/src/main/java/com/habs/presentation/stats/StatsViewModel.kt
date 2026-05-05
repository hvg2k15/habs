package com.habs.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.HabitStats
import com.habs.domain.model.OverallStats
import com.habs.domain.usecase.GetHabitStatsUseCase
import com.habs.domain.usecase.GetOverallStatsUseCase
import com.habs.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class StatsPeriod { MONTHLY, YEARLY }

data class StatsUiState(
    val overallStats: OverallStats? = null,
    /** Sorted worst → best by completion rate for the current calendar month (since habit start is applied per habit in stats). */
    val habitStats: List<HabitStats> = emptyList(),
    /** Mean of each habit’s scheduled-day completion rate (only habits with ≥1 scheduled day this month). */
    val meanHabitCompletionRate: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getOverallStats: GetOverallStatsUseCase,
    private val getHabitStats: GetHabitStatsUseCase,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val fromDate = LocalDate.now().withDayOfMonth(1)
            try {
                val overall = getOverallStats(fromDate)
                val habits = habitRepository.getAllHabits().first()
                val habitStatsList = habits.map { getHabitStats(it.id, fromDate, LocalDate.now()) }
                    .sortedWith(
                        compareBy<HabitStats> { it.completionRate }
                            .thenByDescending { it.missedScheduledDays }
                    )
                val meanRate = habitStatsList
                    .filter { it.scheduledDaysInPeriod > 0 }
                    .map { it.completionRate }
                    .let { rates -> if (rates.isEmpty()) 0f else rates.average().toFloat() }
                _uiState.update {
                    it.copy(
                        overallStats = overall,
                        habitStats = habitStatsList,
                        meanHabitCompletionRate = meanRate,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
