package com.habs.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.HabitStats
import com.habs.domain.model.OverallStats
import com.habs.domain.usecase.GetHabitStatsUseCase
import com.habs.domain.usecase.GetOverallStatsUseCase
import com.habs.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class StatsPeriod { MONTHLY, YEARLY }

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.MONTHLY,
    val overallStats: OverallStats? = null,
    val habitStats: List<HabitStats> = emptyList(),
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

    init { loadStats() }

    fun setPeriod(period: StatsPeriod) {
        _uiState.update { it.copy(period = period) }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val fromDate = when (_uiState.value.period) {
                StatsPeriod.MONTHLY -> LocalDate.now().withDayOfMonth(1)
                StatsPeriod.YEARLY  -> LocalDate.now().withDayOfYear(1)
            }
            try {
                val overall = getOverallStats(fromDate)
                val habits = habitRepository.getAllHabits().first()
                val habitStatsList = habits.map { getHabitStats(it.id, fromDate) }
                _uiState.update {
                    it.copy(overallStats = overall, habitStats = habitStatsList, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}