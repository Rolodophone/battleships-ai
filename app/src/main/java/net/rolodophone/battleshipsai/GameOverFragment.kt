package net.rolodophone.battleshipsai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import net.rolodophone.battleshipsai.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentGameOverBinding>(inflater, R.layout.fragment_game_over, container, false)

        binding.whoWonText.text = when(GameOverFragmentArgs.fromBundle(requireArguments()).aiHasWon) {
            true -> "You lose!"
            false -> "You win!"
        }

        binding.playAgainBtn.setOnClickListener { view: View ->
            ai = Ai()
            view.findNavController().navigate(GameOverFragmentDirections.actionGameOverFragmentToTitleFragment())
        }

        return binding.root
    }
}