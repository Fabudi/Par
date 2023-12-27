package inc.fabudi.par


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import inc.fabudi.par.databinding.FragmentResultBinding
import java.util.Locale


class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private var adWatched = false
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        editor = sharedPref.edit()
        return binding.root
    }

    var seconds: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seconds = arguments?.getInt("time", 0) ?: 0
        setStopwatch()
        setReward(countReward())
        saveMoney()
        binding.againButton.setOnClickListener {
            findNavController().navigate(R.id.action_ResultFragment_to_GameFragment)
        }
        binding.menuButton.setOnClickListener {
            findNavController().navigate(R.id.action_ResultFragment_to_MenuFragment)
        }
        binding.doubleButton.setOnClickListener {
            Toast.makeText(
                context,
                if (!adWatched) "Kinda watching annoying ad" else "No-no-no, stop watching it, it's addictive, really",
                Toast.LENGTH_SHORT
            ).show()
            if (!adWatched) {
                doubleReward()
                adWatched = true
            }
        }
    }

    private fun saveMoney() {
        editor.putInt(
            getString(R.string.money),
            sharedPref.getInt(getString(R.string.money), 0) + countReward()
        )
        editor.apply()
    }

    private fun doubleReward() {
        setReward(countReward() * 2)
        saveMoney()
    }

    val coinsPerSecond = 5
    val splitTime = 20

    private fun countReward() = (100 - (seconds - splitTime) * coinsPerSecond).coerceIn(
        maximumValue = 100, minimumValue = 10
    )

    private fun setReward(rewardAmount: Int) {
        binding.coinsLabel.text = rewardAmount.toString()
    }

    private fun setStopwatch() {
        val minutes: Int = seconds % 3600 / 60
        val secs: Int = seconds % 60
        binding.stopwatchLabel.text = String.format(
            Locale.getDefault(), "%02d:%02d", minutes, secs
        )
    }

}