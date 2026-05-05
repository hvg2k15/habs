package com.habs.presentation.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitStats
import com.habs.domain.repository.HabitRepository
import com.habs.domain.usecase.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class HabitStatsRangePreset {
    ONE_MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    TWELVE_MONTHS,
    YEAR_TO_DATE
}

data class HabitDetailUiState(
    val habit: Habit? = null,
    val habitStats: HabitStats? = null,
    val preset: HabitStatsRangePreset = HabitStatsRangePreset.ONE_MONTH,
    /** Month shown when [preset] is [HabitStatsRangePreset.ONE_MONTH] (pager + stats). */
    val viewMonth: YearMonth = YearMonth.from(LocalDate.now()),
    val windowStart: LocalDate = LocalDate.now().withDayOfMonth(1),
    val windowEnd: LocalDate = LocalDate.now(),
    val rangeLabel: String = "",
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

@HiltViewModel
class HabitStatsDetailViewModel @Inject constructor(
    private val getHabitStats: GetHabitStatsUseCase,
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val habitId: Long = savedStateHandle.get<Long>("habitId")
        ?: error("Missing habitId in navigation arguments")

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun setPreset(preset: HabitStatsRangePreset) {
        if (_uiState.value.preset == preset) return
        _uiState.update {
            it.copy(
                preset = preset,
                viewMonth = if (preset == HabitStatsRangePreset.ONE_MONTH) {
                    YearMonth.from(LocalDate.now())
                } else {
                    it.viewMonth
                }
            )
        }
        load()
    }

    fun setViewMonth(ym: YearMonth) {
        if (_uiState.value.viewMonth == ym) return
        _uiState.update { it.copy(viewMonth = ym) }
        if (_uiState.value.preset == HabitStatsRangePreset.ONE_MONTH) {
            load()
        }
    }

    /**
     * Month strip pager spans this many calendar months ending in the current month so you can
     * swipe to earlier months even when the habit is new (pre-habit months show as not active).
     */
    fun monthPagerPageCount(): Int = MONTH_PAGER_PAGE_COUNT

    fun pagerFirstMonth(): YearMonth =
        YearMonth.from(LocalDate.now()).minusMonths((MONTH_PAGER_PAGE_COUNT - 1).toLong())

    fun yearMonthForPagerPage(page: Int): YearMonth =
        pagerFirstMonth().plusMonths(page.coerceIn(0, monthPagerPageCount() - 1).toLong())

    fun pagerPageForYearMonth(ym: YearMonth): Int {
        val first = pagerFirstMonth()
        val last = YearMonth.from(LocalDate.now())
        val clamped = when {
            ym.isBefore(first) -> first
            ym.isAfter(last) -> last
            else -> ym
        }
        return ChronoUnit.MONTHS.between(first, clamped).toInt()
    }

    companion object {
        private const val MONTH_PAGER_PAGE_COUNT = 24
    }

    private fun load() {
        viewModelScope.launch {
            val prior = _uiState.value
            // Full-screen spinner only on first load; otherwise the tree is torn down and
            // rememberPagerState resets, so month swipes snap back to the current month.
            val blockUi = prior.habit == null || prior.habit.id != habitId
            if (blockUi) {
                _uiState.update { it.copy(isLoading = true, notFound = false) }
            }
            val habit = habitRepository.getHabitById(habitId)
            if (habit == null) {
                _uiState.update { HabitDetailUiState(notFound = true, isLoading = false) }
                return@launch
            }
            val today = LocalDate.now()
            val preset = _uiState.value.preset
            val viewMonth = _uiState.value.viewMonth

            val (from, to, label) = when (preset) {
                HabitStatsRangePreset.ONE_MONTH -> {
                    val start = viewMonth.atDay(1)
                    val end = minOf(viewMonth.atEndOfMonth(), today)
                    val title = viewMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                    Triple(start, end, title)
                }
                HabitStatsRangePreset.THREE_MONTHS -> {
                    // Full calendar window; repository still counts scheduled days only from habit start.
                    val start = today.withDayOfMonth(1).minusMonths(2)
                    Triple(start, today, "Last 3 calendar months")
                }
                HabitStatsRangePreset.SIX_MONTHS -> {
                    val start = today.withDayOfMonth(1).minusMonths(5)
                    Triple(start, today, "Last 6 calendar months")
                }
                HabitStatsRangePreset.TWELVE_MONTHS -> {
                    val start = today.withDayOfMonth(1).minusMonths(11)
                    Triple(start, today, "Last 12 calendar months")
                }
                HabitStatsRangePreset.YEAR_TO_DATE -> {
                    val start = today.withDayOfYear(1)
                    Triple(start, today, "${today.year} (year to date)")
                }
            }

            val stats = getHabitStats(habitId, from, to)
            _uiState.update {
                it.copy(
                    habit = habit,
                    habitStats = stats,
                    preset = preset,
                    viewMonth = viewMonth,
                    windowStart = from,
                    windowEnd = to,
                    rangeLabel = label,
                    isLoading = false,
                    notFound = false
                )
            }
        }
    }
}
