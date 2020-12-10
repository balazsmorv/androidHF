package hu.bme.aut.android.hf.dto

import hu.bme.aut.android.hf.data.SearchResult


data class SearchResultDTO(val Search: List<SearchResult>, val totalResults: Int, val Response: String)


