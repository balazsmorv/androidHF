package hu.bme.aut.android.hf

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
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

        database = Room.databaseBuilder(applicationContext, MovieDetailsDatabase::class.java, "movies").build()

        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.getString("imdbID") != null) {
                this.imdbID.onNext(bundle.getString("imdbID", ""))
            } else if (bundle.getSerializable("details") != null) {
                val movieData = bundle.getSerializable("details") as MovieData
                this.layoutSubviews(movieData)
                if (movieData.poster != null)
                    this.loadImage(movieData.poster)
                this.save(movieData)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

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
                layoutSubviews(movieData)
            }, {
                Log.d("Error", "Error: ${it.localizedMessage}")
            })

        val pictureDisposable = this.movieDetails
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.poster != null) {
                    loadImage(it.poster)
                }
            }, {
                Log.d("Image loading error", "Error occured during image loading: $it")
            })


        val persistenceDisposable = this.movieDetails
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                save(it)
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

    private fun layoutSubviews(movieData: MovieData) {
        titleTextView.text = movieData.title
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
    }

    override fun onStop() {
        super.onStop()
        this.disposeBag.dispose()
        this.database.close()
    }

    private fun loadImage(from: String) {
        thread {
            try {
                if (URLUtil.isValidUrl(from)) {
                    val url = URL(from)
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    runOnUiThread {
                        this.imageView.setImageBitmap(bmp)
                    }
                }
            } catch (e: Error) {
                    Log.d("Error image", "Error during image loading: $e")
                }
        }
    }

    private fun save(movieData: MovieData) {
        thread {
            try {
                val id = database.movieDetailsDao().getItemID(movieData.imdbID)
                if (id == null) {
                    database.movieDetailsDao().insert(movieData)
                } else {
                    database.movieDetailsDao().update(movieData)
                }
            } catch (e: Error) {
                Log.d("Database error", "${e.localizedMessage}")
            }
            Log.d("all data", "all data in database: ${database.movieDetailsDao().getAll()}")
        }
    }

    private fun <T> Flowable<T>.applyScheduler(scheduler: Scheduler) =
        subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}