package com.example.sookplace.ui.home.roulette

import android.R.attr.start
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.sookplace.R

class RouletteLoadingFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_roulette_loading, container, false)
        val ivWheel = view.findViewById<ImageView>(R.id.ivRouletteWheel)

        ObjectAnimator.ofFloat(ivWheel, "rotation", 0f, 360f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start() // 바로 시작
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        // 배경을 투명하게 하고 터치로 닫히지 않게 설정
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            attributes = params
        }
        isCancelable = false
    }


}