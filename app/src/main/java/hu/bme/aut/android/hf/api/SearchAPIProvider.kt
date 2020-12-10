package hu.bme.aut.android.hf.api

import android.util.Log
import hu.bme.aut.android.hf.data.SearchResult
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Flow

class SearchAPIProvider(val repo: SearchAPIRepository) {

    val searchForTitle: PublishSubject<String> = PublishSubject.create()
    val results: BehaviorSubject<List<SearchResult>>

    private val api = API()
    private val disposable: Disposable

    init {

        disposable = this.searchForTitle
            .distinctUntilChanged()
            .flatMap {
                this.api.getDataForTitle(it)
            }.subscribe( {
                this.repo.searchResult.onNext(it)
            }, {
                Log.println(Log.INFO, "Search", "Error in SearchAPIProvider: ${it.localizedMessage}")
            })

        this.results = this.repo.searchResult
    }

    fun dispose() {
        this.disposable.dispose()
    }
}