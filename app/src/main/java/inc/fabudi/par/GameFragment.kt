package inc.fabudi.par

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import inc.fabudi.par.components.Cell
import inc.fabudi.par.components.CellState
import inc.fabudi.par.databinding.FragmentGameBinding
import java.util.Locale

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val cells = mutableListOf<Cell>()
    private val cols = 3
    private val rows = 4
    private var money = 0
    private var previousCellIndex = -1
    private var seconds = 0
    private var running = false

    private lateinit var assetManager: AssetManager
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val cellParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
    ).also {
        it.weight = 1.0f
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        editor = sharedPref.edit()
        assetManager = requireContext().assets
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        money = sharedPref.getInt(getString(R.string.money), 0)
        updateMoney()
        binding.boost1.setOnClickListener {
            if (money >= 10) {
                it.isClickable = false
                (it as LinearLayout).removeAllViewsInLayout()
                reveal(3)
                spendMoney(10)
            }
        }
        binding.boost2.setOnClickListener {
            if (money >= 30) {
                it.isClickable = false
                (it as LinearLayout).removeAllViewsInLayout()
                reveal(cells.size / 2)
                spendMoney(30)
            }
        }
        binding.boost3.setOnClickListener {
            if (money >= 50) {
                it.isClickable = false
                (it as LinearLayout).removeAllViewsInLayout()
                reveal(cells.size)
                spendMoney(50)
            }
        }
        generateCells()
        fillGrid()
    }

    private fun reveal(amount: Int) {
        var revealed = 0
        for (cellShuffled in cells.shuffled()) {
            val cell = cells.find { it.cellIndex == cellShuffled.cellIndex } ?: cells[0]
            if (revealed == amount) break
            if (cell.state == CellState.HIDDEN) {
                revealed++
                val index = cell.cellIndex
                val button = pairedImageButton(index)
                button.isClickable = false
                rotate(hide = false, cell = cell, imageButton = button).doOnEnd {
                    rotate(
                        hide = true, cell = cell, imageButton = button, withDelay = true
                    ).doOnEnd {
                        button.isClickable = true
                    }
                }
            }
        }
    }

    private fun rotate(hide: Boolean, cell: Cell, imageButton: ImageButton, withDelay: Boolean = false): ObjectAnimator {
        cell.state = if (hide) CellState.HIDDEN else CellState.SELECTED
        val flip = ObjectAnimator.ofFloat(
            imageButton, "rotationY", if (hide) 180f else 0f, if (hide) 0f else 180f
        )
        flip.setDuration(300)
        flip.addUpdateListener { animator ->
            if (animator.animatedFraction > .5f) {
                imageButton.isSelected = !hide
                imageButton.imageAlpha = if (hide) 0 else 255
            }
        }
        if (withDelay) flip.startDelay = 3000
        flip.start()
        return flip
    }

    private fun updateMoney() {
        binding.coinsLabel.text = money.toString()
    }

    private fun spendMoney(amount: Int) {
        editor.putInt(
            getString(R.string.money), sharedPref.getInt(getString(R.string.money), 0) - amount
        )
        editor.apply()
        money = sharedPref.getInt(getString(R.string.money), 0)
        updateMoney()
    }

    private fun generateCells() {
        var c = -1
        for (k in 0..1) {
            for (i in 0..<cols * rows / 2) {
                cells.add(
                    Cell(
                        cellIndex = ++c, cellType = i, icon = Drawable.createFromStream(
                            assetManager.open("icons/skin1/${i + 1}.png"), null
                        )
                    )
                )
            }
        }
        cells.shuffle()
    }

    private fun fillGrid() {
        binding.gridLayout.alignmentMode = GridLayout.ALIGN_BOUNDS
        binding.gridLayout.columnCount = cols
        binding.gridLayout.rowCount = rows
        for (cell in cells) {
            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                width = 0
                height = 0
                setMargins((4 * resources.displayMetrics.density).toInt())
            }
            binding.gridLayout.addView(createCell(cell), params)
        }
    }

    private fun runTimer() {
        running = true
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60
                val time = String.format(
                    Locale.getDefault(), "%02d:%02d", minutes, secs
                )
                binding.stopwatchLabel.text = time
                if (running) {
                    seconds++
                    handler.postDelayed(this, 1000)
                }
                if (!running && seconds > 0) {
                    val bundle = Bundle().apply { putInt("time", seconds) }
                    findNavController().navigate(R.id.action_GameFragment_to_ResultFragment, bundle)
                }
            }
        })
    }

    private fun createCell(cell: Cell): ImageButton {
        val ib = ImageButton(context)
        ib.setImageDrawable(cell.icon)
        ib.background = ResourcesCompat.getDrawable(resources, R.drawable.cell_background, null)
        ib.imageAlpha = 0
        ib.isSelected = false
        ib.tag = cell.cellIndex
        ib.scaleType = ImageView.ScaleType.CENTER_INSIDE
        ib.adjustViewBounds = true
        ib.layoutParams = cellParams.apply {
            weight = 1f
            width = height
        }
        ib.setOnClickListener {
            if (!running) runTimer()
            val temp = previousCellIndex
            if (temp == cell.cellIndex || cell.state == CellState.CORRECT) return@setOnClickListener
            if (temp == -1) {
                previousCellIndex = cell.cellIndex
                it.isClickable = false
                rotate(hide = false, cell = cell, imageButton = it as ImageButton).doOnEnd { _ ->
                    it.isClickable = true
                }
                return@setOnClickListener
            }
            previousCellIndex = -1
            val prevButton = pairedImageButton(temp)
            val prevCell = cells.find { it.cellIndex == temp } ?: cells[0]
            val correct = prevCell.cellType == cell.cellType
            it.isClickable = false
            prevButton.isClickable = false
            rotate(hide = false, cell = cell, imageButton = it as ImageButton).doOnEnd { _ ->
                if (!correct) {
                    rotate(hide = true, cell = cell, imageButton = it)
                    rotate(
                        hide = true, cell = prevCell, imageButton = prevButton
                    ).doOnEnd { _ ->
                        it.isClickable = true
                        prevButton.isClickable = true
                    }
                }
            }
            if (correct) {
                cell.state = CellState.CORRECT
                prevCell.state = CellState.CORRECT
                prevButton.isClickable = false
                it.isClickable = false
                if (cells.find { it.state != CellState.CORRECT } == null) running = false
            }
        }
//        val ll = LinearLayout(context)
//        ll.layoutParams = cellParams.apply { gravity = Gravity.CENTER }
//        ll.addView(ib)
        return ib
    }

    private fun pairedImageButton(tag: Any): ImageButton {
        return binding.gridLayout.findViewWithTag(tag)
    }
}