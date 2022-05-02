package com.harishtk.app.wallpick.ui.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.harishtk.app.wallpick.AutoCompleteBoxTag

@Stable
interface AutoCompleteEntity {
    fun filter(query: String): Boolean
}

@Stable
interface ValueAutoCompleteEntity<T> : AutoCompleteEntity {
    val value: T
}

private typealias ItemsSelected<T> = (T) -> Unit

@Stable
interface AutoCompleteScope<T : AutoCompleteEntity> : AutoCompleteDesignScope {
    var isSearching: Boolean
    fun filter(query: String)
    fun onItemSelected(block: ItemsSelected<T> = {})
}

@Stable
interface AutoCompleteDesignScope {
    var boxWidthPercentage: Float
    var shouldWrapContentHeight: Boolean
    var boxMaxHeight: Dp
    var boxBorderStroke: BorderStroke
    var boxShape: androidx.compose.ui.graphics.Shape
}

class AutoCompleteState<T : AutoCompleteEntity>(private val startItems: List<T>) : AutoCompleteScope<T> {
    private var onItemsSelected: ItemsSelected<T>? = null

    fun selectItem(item: T) {
        onItemsSelected?.invoke(item)
    }

    var filteredItems by mutableStateOf(startItems)
    override var isSearching by mutableStateOf(false)
    override var boxWidthPercentage by mutableStateOf(0.9f)
    override var shouldWrapContentHeight by mutableStateOf(false)
    override var boxBorderStroke by mutableStateOf(BorderStroke(width = 2.dp, Color.Black))
    override var boxMaxHeight by mutableStateOf(TextFieldDefaults.MinHeight * 3)
    override var boxShape: Shape by mutableStateOf(RoundedCornerShape(8.dp))

    override fun filter(query: String) {
        if (isSearching) {
            filteredItems = startItems.filter { entity ->
                entity.filter(query)
            }
        }
    }

    override fun onItemSelected(block: ItemsSelected<T>) {
        onItemsSelected = block
    }

}

/* To turn any type into necessary type */
typealias CustomFilter<T> = (T, String) -> Boolean

fun <T> List<T>.asAutoCompleteEntities(filter: CustomFilter<T>): List<ValueAutoCompleteEntity<T>> {
    return map {
        object : ValueAutoCompleteEntity<T> {
            override val value: T = it

            override fun filter(query: String): Boolean {
                return filter(value, query)
            }
        }
    }
}

fun Modifier.autoComplete(
    autoCompleteState: AutoCompleteDesignScope
) : Modifier = composed {
    val baseModifier = if (autoCompleteState.shouldWrapContentHeight)
        wrapContentHeight()
    else
        heightIn(0.dp, autoCompleteState.boxMaxHeight)

    baseModifier
        .testTag(AutoCompleteBoxTag)
        .fillMaxWidth(autoCompleteState.boxWidthPercentage)
        .border(
            border = autoCompleteState.boxBorderStroke,
            shape = autoCompleteState.boxShape
        )
}