package com.example.sookplace.ui.home.roulette

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.sookplace.R
import com.example.sookplace.data.remote.response.RouletteSpinResponse
import com.example.sookplace.databinding.FragmentRouletteResultBinding
import kotlin.jvm.java


class RouletteResultFragment : DialogFragment() {

    private var _binding: FragmentRouletteResultBinding? = null
    private val binding get() = _binding!!

    var onRetry: (() -> Unit)? = null
    var onReset: (() -> Unit)? = null
    var resultData: RouletteSpinResponse? = null // 백엔드 응답 객체
    var optionType: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRouletteResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그 크기 및 배경 설정
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            attributes = params
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()

        binding.btnReset.setOnClickListener {
            dismiss()
            onReset?.invoke()
        }
        binding.btnRetry.setOnClickListener {
            dismiss()
            onRetry?.invoke()
        }
    }

    private fun setupUI() {
        val data = resultData ?: return

        when (optionType) {
            1, 3 -> { // 식당 결과 (MY_PLACE, TOP_20)
                binding.layoutRestaurantCard.visibility = View.VISIBLE
                binding.layoutCategoryResult.visibility = View.GONE

                data.restaurant?.let { restaurant ->
                    binding.tvRestaurantName.text = restaurant.name
                    binding.tvRestaurantCategory.text = restaurant.category
                    binding.ivRestaurantImage.load(restaurant.thumbnailUrl) {
                        crossfade(true)
                        placeholder(R.drawable.background_radius_gray) // 로딩 중 이미지
                        error(R.drawable.background_radius_gray) // 에러 시 이미지
                    }
                    binding.tvRestaurantRating.text = restaurant.rating.toString()

                    binding.layoutRestaurantCard.setOnClickListener { //식당카드 선택 시 상세페이지로 이동
                        val intent = android.content.Intent(
                            requireContext(),
                            com.example.sookplace.ui.search.restaurantDetail.RestaurantDetailActivity::class.java
                        ).apply {
                            putExtra("RESTAURANT_ID", restaurant.id) // 식당 ID 전달
                        }
                        dismiss()
                        startActivity(intent)
                    }
                }
            }
            2 -> { // 카테고리 결과
                binding.layoutRestaurantCard.visibility = View.GONE
                binding.layoutCategoryResult.visibility = View.VISIBLE
                val categoryName = data.category?.name ?: "추천 메뉴"
                binding.tvCategoryName.text = categoryName

                binding.btnGoExplore.setOnClickListener { //탐색하러가기 버튼 클릭 시 탐색화면으로 이동
                    val bundle = Bundle().apply {
                        putString("category", categoryName)
                    }

                    dismiss()

                    findNavController().navigate(
                        R.id.action_homeFragment_to_searchFragment,
                        bundle
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}