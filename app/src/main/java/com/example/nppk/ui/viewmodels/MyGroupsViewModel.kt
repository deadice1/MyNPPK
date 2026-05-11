package com.example.nppk.ui.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedule.shared.group.domain.entity.Group
import com.example.schedule.shared.group.domain.repository.GroupRepository
import com.example.schedule.shared.group.domain.usecase.GetGroupSubjectsUseCase
import com.example.schedule.shared.schedule.domain.entity.Subgroup
import com.example.schedule.shared.schedule.domain.entity.TeacherSubscription
import com.example.schedule.shared.schedule.domain.repository.TeacherPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyGroupsViewModel(
    private val groupRepository: GroupRepository,
    private val preferencesRepository: TeacherPreferencesRepository,
    private val getGroupSubjectsUseCase: GetGroupSubjectsUseCase
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups = _groups.asStateFlow()

    private val _selectedGroups = MutableStateFlow<List<String>>(emptyList())
    val selectedGroups = _selectedGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _tempSelectedGroups = MutableStateFlow<Set<String>>(emptySet())
    val tempSelectedGroups = _tempSelectedGroups.asStateFlow()

    private val _manualGroups = MutableStateFlow<Set<String>>(emptySet())

    private val _groupSubjects = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val groupSubjects = _groupSubjects.asStateFlow()

    private val _drafts = MutableStateFlow<List<TeacherSubscription>>(emptyList())
    val drafts = _drafts.asStateFlow()

    init {
        loadGroups()
        loadSelectedGroups()
        refreshDrafts()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _groups.value = groupRepository.getAll()
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSelectedGroups() {
        viewModelScope.launch {
            val subscriptions = preferencesRepository.getSubscriptions()
            val groupsFromSubs = subscriptions.map { it.groupName }.toSet()
            _selectedGroups.value = (groupsFromSubs + _manualGroups.value).toList().sorted()
        }
    }

    fun toggleSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _tempSelectedGroups.value = emptySet()
        }
    }

    fun toggleGroupForDeletion(groupName: String) {
        val current = _tempSelectedGroups.value.toMutableSet()
        if (current.contains(groupName)) {
            current.remove(groupName)
        } else {
            current.add(groupName)
        }
        _tempSelectedGroups.value = current
        if (current.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun deleteSelectedGroups() {
        viewModelScope.launch {
            val currentSubs = preferencesRepository.getSubscriptions().toMutableList()
            currentSubs.removeAll { it.groupName in _tempSelectedGroups.value }
            preferencesRepository.saveSubscriptions(currentSubs)
            
            _manualGroups.value = _manualGroups.value - _tempSelectedGroups.value
            loadSelectedGroups()
            toggleSelectionMode(false)
            refreshDrafts()
        }
    }

    fun addGroups(groupNames: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _manualGroups.value = _manualGroups.value + groupNames
            loadSelectedGroups()
            _isLoading.value = false
        }
    }

    fun refreshDrafts() {
        viewModelScope.launch {
            _drafts.value = preferencesRepository.getSubscriptions()
            loadSelectedGroups() // Пересчитываем список групп
        }
    }

    fun toggleSubjectSelection(groupName: String, subjectName: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentDrafts = preferencesRepository.getSubscriptions().toMutableList()
            if (isSelected) {
                if (currentDrafts.none { it.groupName == groupName && it.subjectName == subjectName }) {
                    currentDrafts.add(TeacherSubscription(groupName, subjectName, Subgroup.ALL))
                }
            } else {
                currentDrafts.removeAll { it.groupName == groupName && it.subjectName == subjectName }
            }
            preferencesRepository.saveSubscriptions(currentDrafts)
            refreshDrafts()
        }
    }

    fun updateSubgroup(groupName: String, subjectName: String, subgroup: Subgroup) {
        viewModelScope.launch {
            val currentDrafts = preferencesRepository.getSubscriptions().map {
                if (it.groupName == groupName && it.subjectName == subjectName) {
                    it.copy(subgroup = subgroup)
                } else it
            }
            preferencesRepository.saveSubscriptions(currentDrafts)
            refreshDrafts()
        }
    }

    fun loadSubjectsIfNeed(groupName: String) {
        if (!_groupSubjects.value.containsKey(groupName)) {
            viewModelScope.launch {
                val subjects = getGroupSubjectsUseCase(groupName)
                _groupSubjects.value = _groupSubjects.value + (groupName to subjects)
            }
        }
    }
}
