package inc.fabudi.par.components

import android.graphics.drawable.Drawable

data class Cell(
    val cellType: Int,
    val icon: Drawable?,
    var state: CellState = CellState.HIDDEN,
    val cellIndex: Int
)

enum class CellState {
    HIDDEN,
    SELECTED,
    CORRECT
}
