package com.udacity.asteroidradar.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.ui.adapters.AsteroidsAdapter
import com.udacity.asteroidradar.ui.viewmodels.MainViewModel
import com.udacity.asteroidradar.ui.viewmodels.UIState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val adapter = AsteroidsAdapter(AsteroidsAdapter.OnClickListener { asteroid ->
        findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
    })
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = adapter

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch {
                viewModel.asteroids.collect {
                    adapter.submitList(it)
                    Log.d("MainFragment", "$it")
                }
            }

            launch {
                viewModel.pictureOfDay.collect {
                    it?.let {
                        Picasso.get()
                            .load(it.url)
                            .into(binding.activityMainImageOfTheDay)
                    }
                }
            }

            launch {
                viewModel.uiState.collect{
                    when(it) {
                        is UIState.Loading -> showLoading()
                        is UIState.Success, UIState.Idle -> hideLoading()
                    }
                }
            }
        }
    }

    private fun hideLoading() {
        binding.statusLoadingWheel.isVisible = false
    }

    private fun showLoading() {
        binding.statusLoadingWheel.isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
