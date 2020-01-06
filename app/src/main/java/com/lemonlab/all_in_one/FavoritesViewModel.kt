package com.lemonlab.all_in_one

import android.app.Application
import androidx.lifecycle.*
import com.lemonlab.all_in_one.model.Favorite
import com.lemonlab.all_in_one.model.FavoriteDao
import com.lemonlab.all_in_one.model.FavoritesRoomDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FavoritesRepository(private val favoriteDao: FavoriteDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    private val search = MutableLiveData<String>()

    val allFavorites: LiveData<List<Favorite>> = favoriteDao.getAllFavorites()
    var favoritesCodes: LiveData<List<Int>> = favoriteDao.getFavoritesCodes()
    var searchFavorites: LiveData<List<Favorite>> = Transformations.switchMap(search) { it ->
        favoriteDao.searchFor(it)

    }


    suspend fun insert(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun delete(favorite: Favorite) {
        favoriteDao.deleteFavorite(favorite)
    }

    fun update() {
        favoritesCodes = favoriteDao.getFavoritesCodes()
    }

    fun searchFor(value: String) {
        search.value = value
    }
}


// Class extends AndroidViewModel and requires application as a parameter.
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: FavoritesRepository
    // LiveData gives us updated words when they change.
    val allFavorites: LiveData<List<Favorite>>
    var searchFavorites: LiveData<List<Favorite>>
    val favoritesCodes: LiveData<List<Int>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct WordRepository.
        val favoriteDao = FavoritesRoomDatabase.getDatabase(application).favoriteDao()
        repository = FavoritesRepository(favoriteDao)
        allFavorites = repository.allFavorites
        favoritesCodes = repository.favoritesCodes
        searchFavorites = repository.searchFavorites
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insert(favorite: Favorite) = viewModelScope.launch {
        updateCodes()
        repository.insert(favorite)
    }


    fun remove(favorite: Favorite) = viewModelScope.launch {
        updateCodes()
        repository.delete(favorite)
    }

    fun search(search: String) = viewModelScope.launch {
        repository.searchFor(search)
        delay(1_200)
        searchFavorites = repository.searchFavorites
    }

    fun updateCodes() =
        viewModelScope.launch {
            repository.update()
        }

}

