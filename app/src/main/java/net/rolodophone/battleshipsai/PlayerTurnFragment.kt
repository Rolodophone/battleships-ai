package net.rolodophone.battleshipsai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import net.rolodophone.battleshipsai.databinding.FragmentPlayerTurnBinding

class PlayerTurnFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =  DataBindingUtil.inflate<FragmentPlayerTurnBinding>(inflater, R.layout.fragment_player_turn, container, false)

        ArrayAdapter.createFromResource(requireContext(), R.array.letters, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.letterSpinner.adapter = it
        }

        ArrayAdapter.createFromResource(requireContext(), R.array.numbers, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.numberSpinner.adapter = it
        }

        binding.fireButton.setOnClickListener {
            val result = ai.receiveShot(binding.letterSpinner.selectedItemPosition, binding.numberSpinner.selectedItemPosition)
            binding.resultText.text = when(result) {
                Ai.FireResult.HIT -> "HIT"
                Ai.FireResult.MISS -> "MISS"
                Ai.FireResult.SINK -> "SUNK"
            }
            binding.resultText.visibility = View.VISIBLE
            binding.okButton.visibility = View.VISIBLE
            binding.fireButton.isEnabled = false
        }

        binding.okButton.setOnClickListener { view: View ->
            if (ai.gameOver) {
                view.findNavController().navigate(PlayerTurnFragmentDirections.actionPlayerTurnFragmentToGameOverFragment(false))
            }
            else {
                view.findNavController().navigate(R.id.action_playerTurnFragment_to_aiTurnFragment)
            }
        }

        return binding.root
    }
}