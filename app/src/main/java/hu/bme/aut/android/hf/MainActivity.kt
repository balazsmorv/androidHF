package hu.bme.aut.android.hf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import hu.bme.aut.android.hf.api.SearchAPIProvider
import hu.bme.aut.android.hf.api.SearchAPIRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import hu.bme.aut.android.hf.api.MovieSearchProvider
import hu.bme.aut.android.hf.data.MovieDetailsDatabase
import hu.bme.aut.android.hf.data.SearchResult
import hu.bme.aut.android.hf.recyclerViewThings.SearchResultAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), SearchResultAdapter.SearchResultClickListener {

    // API properties
    private val searchProvider: SearchAPIProvider = SearchAPIProvider(SearchAPIRepository())
    private val movieSearchProvider = MovieSearchProvider()
    private val disposeables = CompositeDisposable()
    private lateinit var database: MovieDetailsDatabase

    // Recycler view properties
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchButton.setOnClickListener {
            this.adapter.removeItems()
            if (!toggleButton.isChecked) {
                Log.d("Search for title", "search for ${searchField.text.toString()}")
                searchProvider.searchForTitle.onNext(searchField.text.toString())
            } else {
                Log.d("Search for id", "search for ${searchField.text.toString()}")
                movieSearchProvider.getMoveDetails(searchField.text.toString())
                    .subscribe({
                        if (it != null) {
                            thread {
                                Log.d("Movie", "new data: $it")
                                val intent = Intent(this, DetailsView::class.java)
                                val bundle = Bundle()
                                bundle.putSerializable("details", it)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }
                        }
                    }, {
                        Log.d("Movie error", it.localizedMessage)
                    })

            }
        }

        historyButton.setOnClickListener {
            thread {
                Log.d("History view", "going to history view")
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
        }

        val beforeFirstSearchDisposable = this.searchProvider.results
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe({
                this.adapter.addItem(SearchResult("Search for a title or id to get results here.", null, null, null, null))
            }, {

            })

        val searchDisposable = this.searchProvider.results
            .observeOn(AndroidSchedulers.mainThread())
            .skip(1) // The first value is always an empty array, when the app starts
            .subscribe( {
                if (it.size > 0) {
                    it.map {
                        this.adapter.addItem(it)
                    }
                } else {
                    this.adapter.addItem(SearchResult("No results found", null, null, null, null))
                }
            }, {
                Log.println(Log.INFO, "MainActivity", "error occured in MainActivity: ${it.localizedMessage}")
            })


        disposeables.addAll(searchDisposable, beforeFirstSearchDisposable)
        initRecyclerView()
        database = Room.databaseBuilder(applicationContext, MovieDetailsDatabase::class.java, "movies").build()
    }

    override fun onDestroy() {
        this.disposeables.dispose()
        this.searchProvider.dispose()
        super.onDestroy()
        database.close()
    }

    override fun onItemChanged(item: SearchResult) {
        thread {
            Log.d("MainActivity", "update was successful")
        }
    }

    override fun onTap(item: SearchResult) {
        thread {
            Log.d("INFO", "taptap")
            val intent = Intent(this, DetailsView::class.java)
            val bundle = Bundle()
            bundle.putString("imdbID", item.imdbID)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        recyclerView = SearchRecyclerView
        adapter = SearchResultAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /*private fun loadItemsInBackground() {
        thread {
            val items = database.shoppingItemDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }*/
}