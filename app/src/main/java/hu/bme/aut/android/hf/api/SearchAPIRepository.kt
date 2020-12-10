package hu.bme.aut.android.hf.api

import android.util.Log
import hu.bme.aut.android.hf.data.SearchResult
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class SearchAPIRepository {

    val searchResult: BehaviorSubject<List<SearchResult>>
    private val disposable: Disposable

    init {
        val emptyList = emptyList<SearchResult>() // TODO: change this
        this.searchResult = BehaviorSubject.createDefault(emptyList)
        this.disposable = searchResult.subscribe {
            // TODO: save the search result to database
            Log.println(Log.INFO, "SearchAPIRepository", "got data in SerchAPIRepository: $it")
        }
    }

    public fun dispose() {
        this.disposable.dispose()
    }

}