package com.affirm.takehome.mainViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.affirm.takehome.data.Restaurant

//viewmodel  to hold the data needed
class MainViewModel : ViewModel() {
    var restaurantList = MutableLiveData<MutableList<Restaurant>>()

    init{
        this.restaurantList.value = mutableListOf()
    }

    fun addRestaurants(list: List<Restaurant>){
        restaurantList.value?.addAll(list)
    }


}