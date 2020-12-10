package hu.bme.aut.android.hf.api

import android.util.Log
import hu.bme.aut.android.hf.data.MovieData
import io.reactivex.Observable
import io.reactivex.Single

class MovieSearchProvider {

    private val api = API()

    fun getMoveDetails(id: String) : Observable<MovieData?> {
        return api.getDataForID(id)
    }
}