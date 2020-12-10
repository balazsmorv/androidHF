package hu.bme.aut.android.hf.api

import android.util.Log
import com.rx2androidnetworking.Rx2AndroidNetworking
import hu.bme.aut.android.hf.data.MovieData
import hu.bme.aut.android.hf.data.Ratings
import hu.bme.aut.android.hf.data.RatingsConverter
import hu.bme.aut.android.hf.data.SearchResult
import hu.bme.aut.android.hf.dto.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient

class API {

    val client = OkHttpClient()
    val apiKey = "936907b99fmshb8b8ce9f592618bp14616ajsnc7cb6f502808"
    val host = "movie-database-imdb-alternative.p.rapidapi.com"

    fun getDataForTitle(title: String) : Observable<List<SearchResult>> {
        Log.d("API", "getDataForTitle(title: $title) called")
        val encodedTitle: String = java.net.URLEncoder.encode(title, "utf-8")
        return Rx2AndroidNetworking.get("https://movie-database-imdb-alternative.p.rapidapi.com/?s=$encodedTitle&page=1&r=json")
            .addHeaders("x-rapidapi-key", apiKey)
            .addHeaders("x-rapidapi-host", host)
            .build()
            .getObjectObservable(SearchResultDTO::class.java)
            .map {
                it.Search
            }
            .onErrorReturn {
                emptyList()
            }
            .subscribeOn(Schedulers.io())
    }

    fun getDataForID(id: String) : Observable<MovieData?> {
        Log.d("API", "getDataForID(id: $id) called")
        val encodedID = java.net.URLEncoder.encode(id, "utf-8")
        return Rx2AndroidNetworking.get("https://movie-database-imdb-alternative.p.rapidapi.com/?i=$encodedID&r=json")
            .addHeaders("x-rapidapi-key", apiKey)
            .addHeaders("x-rapidapi-host", host)
            .build()
            .getObjectObservable(MovieSearchDTO::class.java)
            .map { dto: MovieSearchDTO ->
                MovieData(dto.Title,
                    dto.Year,
                    dto.Rated,
                    dto.Released?.toIntOrNull(),
                    dto.Runtime,
                    dto.Genre,
                    dto.Director,
                    dto.Writer,
                    dto.Actors,
                    dto.Plot,
                    dto.Language,
                    dto.Country,
                    dto.Awards,
                    dto.Poster,
                    RatingsConverter().toRatingList(dto),
                    dto.Metascore,
                    dto.imdbRating,
                    dto.imdbVotes?.toIntOrNull(),
                    dto.imdbID,
                    dto.Type)
            }
            .doOnError {
                Log.d("Movie details error", "Error using the details api: $it")
            }
            .onErrorReturn { null }
            .subscribeOn(Schedulers.io())
    }






}