package fr.mastergime.arqioui.debit.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.BuildConfig
import fr.mastergime.arqioui.debit.databinding.DebitFragmentLayoutBinding
import fr.mastergime.arqioui.debit.services.DebitService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class DebitFragment : Fragment(){

    private lateinit var _debitBinding: DebitFragmentLayoutBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = "Albums"

        _debitBinding = DebitFragmentLayoutBinding.inflate(inflater)
        return _debitBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _debitBinding.buttonStart.setOnClickListener {
            val scheduleTaskExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
            scheduleTaskExecutor.scheduleAtFixedRate(
                { requireActivity().startService(Intent(context, DebitService::class.java)) },
                0,
                2,
                TimeUnit.SECONDS
            )
        }
    }



}