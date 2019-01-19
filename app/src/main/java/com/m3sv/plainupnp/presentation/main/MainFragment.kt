package com.m3sv.plainupnp.presentation.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import com.m3sv.plainupnp.common.SpaceItemDecoration
import com.m3sv.plainupnp.common.utils.dp
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.databinding.MainFragmentBinding
import com.m3sv.plainupnp.disposeBy
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.main.data.toItems
import com.m3sv.plainupnp.upnp.BrowseToModel
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.RenderItem
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber


class MainFragment : BaseFragment() {

    private lateinit var viewModel: MainFragmentViewModel

    private lateinit var binding: MainFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private fun handleContentState(contentState: ContentState) {
        when (contentState) {
            is ContentState.Success -> {
                contentAdapter.setWithDiff(contentState.content.toItems())
                hideProgress()
            }

            is ContentState.Loading -> {
                showProgress()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        contentAdapter = GalleryContentAdapter(object : OnItemClickListener {
            override fun onDirectoryClick(itemUri: String?, parentId: String?) {
                itemUri?.let {
                    viewModel.browseTo(BrowseToModel(itemUri, parentId))
                } ?: Timber.e("Item URI is null")
            }

            override fun onItemClick(item: DIDLItem, position: Int) {
                viewModel.renderItem(RenderItem(item, position))
            }
        })

        binding.content.run {
            val orientation = resources.configuration.orientation
            val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                4
            } else {
                6
            }

            addItemDecoration(SpaceItemDecoration(2.dp))
            layoutManager = GridLayoutManager(
                requireActivity(),
                spanCount,
                GridLayoutManager.VERTICAL,
                false
            )

            adapter = contentAdapter
        }

        RxTextView.textChanges(binding.filter)
            .subscribeBy(onNext = contentAdapter::filter, onError = Timber::e)
            .disposeBy(disposables)

        with(binding) {
            expandSearch.setOnClickListener {
                filter.translationX = filter.width.toFloat()
                filter.animate().x(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            filter.visibility = View.VISIBLE
                        }
                    })

                closeSearch.visibility = View.VISIBLE
                expandSearch.visibility = View.GONE
            }

            closeSearch.setOnClickListener {
                filter.animate()
                    .translationX(filter.width.toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            filter.visibility = View.INVISIBLE
                        }
                    })

                closeSearch.visibility = View.GONE
                expandSearch.visibility = View.VISIBLE
            }
        }


        with(viewModel.contentData) {
            nonNullObserve(::handleContentState)

            if (value is ContentState.Success)
                contentAdapter.setWithDiff((value as ContentState.Success).content.toItems())
        }
    }

    private fun showProgress() {
        contentAdapter.clickable = false
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progress.visibility = View.GONE
        contentAdapter.clickable = true
    }

    companion object {
        fun newInstance(): MainFragment = MainFragment().apply {
            arguments = Bundle()
        }
    }
}