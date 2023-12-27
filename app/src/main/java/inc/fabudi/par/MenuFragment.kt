package inc.fabudi.par

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import inc.fabudi.par.databinding.FragmentMenuBinding
import kotlin.system.exitProcess

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playButton.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_GameFragment)
        }
        binding.exitButton.setOnClickListener {
            exitProcess(0)
        }
        binding.shopButton.setOnClickListener {
            Toast.makeText(context, "Here you can buy skins, etc.", Toast.LENGTH_SHORT).show()
        }
        binding.privacyPolicyLabel.setOnClickListener {
            Toast.makeText(
                context,
                "Some action to view privacy policy, I DUNNO",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}