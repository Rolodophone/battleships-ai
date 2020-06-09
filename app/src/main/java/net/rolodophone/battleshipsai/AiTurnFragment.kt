package net.rolodophone.battleshipsai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import net.rolodophone.battleshipsai.databinding.FragmentAiTurnBinding

class AiTurnFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentAiTurnBinding>(inflater, R.layout.fragment_ai_turn, container, false)

        binding.missButton.setOnClickListener { receivePlayerResult(Ai.FireResult.MISS) }
        binding.hitButton.setOnClickListener  { receivePlayerResult(Ai.FireResult.HIT ) }
        binding.sinkButton.setOnClickListener { receivePlayerResult(Ai.FireResult.SINK) }

        val aiFireLocation = ai.fire()
        binding.targetText.text = "${resources.getStringArray(R.array.letters)[aiFireLocation.x]}${aiFireLocation.y + 1}"

        return binding.root
    }

    private fun receivePlayerResult(result: Ai.FireResult) {
        ai.observeResult(result)

        if (ai.gameOver) {
            requireView().findNavController().navigate(AiTurnFragmentDirections.actionAiTurnFragmentToGameOverFragment(true))
        }
        else {
            requireView().findNavController().navigate(R.id.action_aiTurnFragment_to_playerTurnFragment)
        }
    }
}