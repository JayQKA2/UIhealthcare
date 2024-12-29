package vn.edu.usth.uihealthcare.Data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.Data.Entity.WeightEntity
import vn.edu.usth.uihealthcare.utils.HealthConnectManager

class WeightViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    private val _weights = MutableStateFlow<List<WeightEntity>>(emptyList())
    val weights: StateFlow<List<WeightEntity>> get() = _weights

    init {
        fetchWeights()
    }

    private fun fetchWeights() {
        healthConnectManager.getAllWeights().observeForever { weightList ->
            _weights.value = weightList
        }
    }

    fun insertWeight(weightEntity: WeightEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            healthConnectManager.insertWeight(weightEntity)
        }
    }
}