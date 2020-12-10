package hu.bme.aut.android.hf

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.room.Room
import com.jakewharton.rxrelay3.BehaviorRelay
import hu.bme.aut.android.hf.api.MovieSearchProvider
import hu.bme.aut.android.hf.data.MovieData
import hu.bme.aut.android.hf.data.MovieDetailsDatabase
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers.io
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.movie_details.*
import java.net.URL
import kotlin.concurrent.thread

class DetailsView : AppCompatActivity() {

    var imdbID: PublishSubject<String> = PublishSubject.create()
    val movieSearchProvider: MovieSearchProvider = MovieSearchProvider()
    var movieDetails: BehaviorSubject<MovieData> = BehaviorSubject.create()
    val disposeBag = CompositeDisposable()
    private lateinit var database: MovieDetailsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_details)

        val bundle = intent.extras
        if (bundle != null) {
            Log.d("Details view", "got id in bundle: ${bundle.getString("imdbID")}")
            this.imdbID.onNext(bundle.getString("imdbID", ""))
        }

        backButton.setOnClickListener {
            finish()
        }

        database = Room.databaseBuilder(applicationContext, MovieDetailsDatabase::class.java, "movies").build()
    }

    init {
        val searchDisposable = this.imdbID
            .filter {
                it != ""
            }.take(1)
            .subscribe({
                this.movieSearchProvider.getMoveDetails(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( {
                        if (it != null) {
                            Log.d("OK", "Movie details data came: $it")
                            this.movieDetails.onNext(it)
                        } else {
                            Log.d("Null MovieDetails", "Data that came was null")
                        }
                    }, {
                        Log.d("Error", "Error occured during movie details getting: $it")
                    })
            }, {
                Log.d("PublishSubject error", "Baj van")
            })

        val apiDisposable = this.movieDetails
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ movieData ->
                titleTextView.text = movieData!!.title
                if (movieData.genre != null) {
                    scrollViewInside.addView(addRow("Genre", movieData.genre))
                }
                if (movieData.actors != null) {
                    scrollViewInside.addView(addRow("Actors", movieData.actors))
                }
                if (movieData.imdbRating != null) {
                    scrollViewInside.addView(addRow("IMDB Score", movieData.imdbRating))
                }
                if (movieData.imdbVotes != null) {
                    scrollViewInside.addView(addRow("Number of votes", movieData.imdbVotes.toString()))
                }
            }, {
                Log.d("Error", "Error: ${it.localizedMessage}")
            })

        val pictureDisposable = this.movieDetails
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.poster != null) {
                    val url = URL(it.poster)
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    runOnUiThread {
                        this.imageView.setImageBitmap(bmp)
                    }
                }
            }, {
                Log.d("Image loading error", "Error occured during image loading: $it")
            })


        val persistenceDisposable = this.movieDetails
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                Log.d("Data", "data came: $it")
                database.movieDetailsDao().insert(it)
                Log.d("All data", "all data in database: ${database.movieDetailsDao().getAll()}")
            }, {
                Log.d("Persistence error", "no persistance saving, ${it.localizedMessage}")
            })


        disposeBag.addAll(searchDisposable, apiDisposable, persistenceDisposable, pictureDisposable)
    }

    private fun addRow(title: String, value: String) : View {
        val itemView: View = LayoutInflater.from(applicationContext).inflate(R.layout.result_row, null, false)
        val titleView = itemView.findViewById<TextView>(R.id.titleTextView)
        val valueView = itemView.findViewById<TextView>(R.id.yearTextView)
        titleView.text = title
        valueView.text = value
        return itemView
    }

    override fun onStop() {
        super.onStop()
        this.disposeBag.dispose()
        this.database.close()
    }

    private fun <T> Flowable<T>.applyScheduler(scheduler: Scheduler) =
        subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}