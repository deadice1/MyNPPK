package com.example.nppk.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedule.shared.group.domain.entity.Group
import com.example.schedule.shared.group.domain.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupSelectionViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _groups.value = groupRepository.getAll()
            } catch (e: Exception) {
                // В реальном приложении здесь была бы обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }
}
