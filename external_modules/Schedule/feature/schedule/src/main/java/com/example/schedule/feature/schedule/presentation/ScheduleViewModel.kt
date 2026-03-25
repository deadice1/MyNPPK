package com.example.schedule.feature.schedule.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedule.shared.date.domain.usecase.GetDatesAroundTodayUseCase
import com.example.schedule.shared.date.domain.usecase.GetTodayUseCase
import com.example.schedule.shared.group.domain.entity.Group
import com.example.schedule.shared.group.domain.usecase.GetSelectedGroupListUseCase
import com.example.schedule.shared.schedule.domain.usecase.GetScheduleByDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ScheduleViewModel(
    private val getTodayUseCase: GetTodayUseCase,
    private val getSelectedGroupListUseCase: GetSelectedGroupListUseCase,
    private val getScheduleByDateUseCase: GetScheduleByDateUseCase,
    private val getDatesAroundTodayUseCase: GetDatesAroundTodayUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Initial)
    val state: StateFlow<State> = _state

    init {
        // Загружаем данные один раз при создании ViewModel.
        loadInitialDataInternal()
    }

    fun loadInitialData() {
        // Оставляем public-метод на будущее: сейчас он просто гарантирует наличие данных
        val currentState = _state.value
        if (currentState is State.Content) {
            Log.d(
                "ScheduleViewModel",
                "loadInitialData(): ensure schedule for index=${currentState.selectedScheduleIndex}"
            )
            loadSchedule(currentState.selectedScheduleIndex)
        }
    }

    fun updateSelectedScheduleIndex(newIndex: Int) {
        val contentState = _state.value as? State.Content ?: return
        _state.value = contentState.copy(selectedScheduleIndex = newIndex)
        loadSchedule(newIndex)
    }

    fun getPreviousDay() {
        val contentState = _state.value as? State.Content ?: return
        updateSelectedScheduleIndex(contentState.selectedScheduleIndex - 1)
    }

    fun getNextDay() {
        val contentState = _state.value as? State.Content ?: return
        updateSelectedScheduleIndex(contentState.selectedScheduleIndex + 1)
    }

    fun selectNewGroup(group: Group) {
        val contentState = _state.value as? State.Content ?: return

        if (contentState.selectedGroup.id == group.id) {
            cancelGroupSelecting()
            return
        }

        viewModelScope.launch {
            _state.value = contentState.copy(
                selectedGroup = group,
                selectedGroupState = SelectedGroupState.SELECTED,
                scheduleStateList = createInitialScheduleStates()
            )
            loadSchedule(contentState.selectedScheduleIndex)
        }
    }

    fun cancelGroupSelecting() {
        updateSelectedGroupState(SelectedGroupState.SELECTED)
    }

    fun startGroupSelecting() {
        updateSelectedGroupState(SelectedGroupState.SELECTING)
    }

    private fun updateSelectedGroupState(newState: SelectedGroupState) {
        val contentState = _state.value as? State.Content ?: return
        _state.value = contentState.copy(selectedGroupState = newState)
    }

    private fun createInitialScheduleStates(): List<ScheduleState> {
        return getDatesAroundTodayUseCase(500, 500)
            .map(ScheduleState::ReadyToLoad)
    }

    private fun loadInitialDataInternal() {
        Log.d("ScheduleViewModel", "loadInitialDataInternal(): start")
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                val today = getTodayUseCase()
                val selectedGroupList = getSelectedGroupListUseCase()
                val scheduleStateList = createInitialScheduleStates()

                val selectedIndex = scheduleStateList.indexOfFirst { it.date == today }
                    .takeIf { it >= 0 } ?: 0

                val selectedGroup = selectedGroupList.first()

                Log.d(
                    "ScheduleViewModel",
                    "loadInitialDataInternal(): loaded groups=${selectedGroupList.size}, " +
                        "selectedGroup=${selectedGroup.id}, selectedIndex=$selectedIndex"
                )

                _state.value = State.Content(
                    selectedGroup = selectedGroup,
                    scheduleStateList = scheduleStateList,
                    selectedScheduleIndex = selectedIndex,
                    selectedGroupList = selectedGroupList,
                    selectedGroupState = SelectedGroupState.SELECTED
                )

                // При первом показе сразу загружаем расписание для выбранного дня
                loadSchedule(selectedIndex)
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "loadInitialDataInternal() failed", e)

                // В случае ошибки хотя бы выходим из бесконечной загрузки
                val today = try {
                    getTodayUseCase()
                } catch (_: Exception) {
                    java.time.LocalDate.now()
                }

                val fallbackGroup = Group(id = -1, name = "Unknown")
                val fallbackScheduleState =
                    ScheduleState.Loaded(date = today, lessons = emptyList())

                _state.value = State.Content(
                    selectedGroup = fallbackGroup,
                    selectedGroupList = listOf(fallbackGroup),
                    scheduleStateList = listOf(fallbackScheduleState),
                    selectedScheduleIndex = 0,
                    selectedGroupState = SelectedGroupState.SELECTED
                )
            }
        }
    }

    private fun loadSchedule(index: Int) {
        val currentState = _state.value as? State.Content ?: return
        val scheduleState = currentState.scheduleStateList.getOrNull(index) ?: return

        Log.d(
            "ScheduleViewModel",
            "loadSchedule(index=$index, date=${scheduleState.date}, " +
                "stateType=${scheduleState::class.java.simpleName})"
        )

        if (scheduleState is ScheduleState.Loading || scheduleState is ScheduleState.Loaded) {
            return
        }

        _state.value = currentState.updateScheduleState(
            index = index,
            scheduleState = ScheduleState.Loading(scheduleState.date)
        )

        viewModelScope.launch {
            try {
                // Ограничиваем время ожидания, чтобы не зависать бесконечно
                val schedule = withTimeout(5_000L) {
                    getScheduleByDateUseCase(
                        currentState.selectedGroup.id,
                        scheduleState.date,
                    )
                }
                (_state.value as? State.Content)?.let {
                    _state.value = it.updateScheduleState(
                        index = index,
                        scheduleState = ScheduleState.Loaded(scheduleState.date, schedule.lessons)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "ScheduleViewModel",
                    "Failed to load schedule for index=$index, date=${scheduleState.date}",
                    e
                )

                // Даже при ошибке выходим из бесконечной загрузки и показываем пустой день
                (_state.value as? State.Content)?.let {
                    _state.value = it.updateScheduleState(
                        index = index,
                        scheduleState = ScheduleState.Loaded(scheduleState.date, emptyList())
                    )
                }
            }
        }
    }

    private fun State.Content.updateScheduleState(
        index: Int,
        scheduleState: ScheduleState
    ): State.Content =
        scheduleStateList.toMutableList()
            .apply { set(index, scheduleState) }
            .let { copy(scheduleStateList = it) }
}