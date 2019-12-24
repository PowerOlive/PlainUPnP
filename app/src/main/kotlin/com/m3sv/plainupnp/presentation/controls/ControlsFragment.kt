package com.m3sv.plainupnp.presentation.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.BottomSheetCallback
import com.m3sv.plainupnp.common.OnSlideAction
import com.m3sv.plainupnp.common.OnStateChangedAction
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.common.utils.onSeekBarChangeListener
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE


sealed class ControlsAction {
    object NextClick : ControlsAction()
    object PreviousClick : ControlsAction()
    object PlayClick : ControlsAction()
    data class ProgressChange(val progress: Int) : ControlsAction()
    data class SelectRenderer(val position: Int) : ControlsAction()
    data class SelectContentDirectory(val position: Int) : ControlsAction()
}


interface ControlsActionCallback {
    fun onAction(action: ControlsAction)
}

class ControlsFragment : Fragment() {

    interface ShowDismissListener {
        fun onShow()
        fun onDismiss()
    }

    var actionCallback: ControlsActionCallback? = null

    private lateinit var binding: ControlsFragmentBinding

    private val bottomSheetCallback: BottomSheetCallback = BottomSheetCallback()

    private val showDismissListeners: MutableList<ShowDismissListener> = mutableListOf()

    private val behavior: BottomSheetBehavior<ConstraintLayout> by lazy(NONE) {
        BottomSheetBehavior.from(binding.backgroundContainer)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = close()
    }

    private lateinit var rendererAdapter: SimpleArrayAdapter<SpinnerItem>

    private lateinit var contentDirectoriesAdapter: SimpleArrayAdapter<SpinnerItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ControlsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rendererAdapter = SimpleArrayAdapter.init(binding.root.context)
        contentDirectoriesAdapter = SimpleArrayAdapter.init(binding.root.context)

        if (savedInstanceState != null) {
            rendererAdapter.onRestoreInstanceState(savedInstanceState)
            contentDirectoriesAdapter.onRestoreInstanceState(savedInstanceState)
        }

        behavior.addBottomSheetCallback(bottomSheetCallback)

        addOnStateChangedAction(object : OnStateChangedAction {
            override fun onStateChanged(sheet: View, newState: Int) {
                onBackPressedCallback.isEnabled = newState != BottomSheetBehavior.STATE_HIDDEN
            }
        })

        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        with(binding) {
            progress.isEnabled = false

            next.setOnClickListener {
                actionCallback?.onAction(ControlsAction.NextClick)
            }

            previous.setOnClickListener {
                actionCallback?.onAction(ControlsAction.PreviousClick)
            }

            play.setOnClickListener {
                actionCallback?.onAction(ControlsAction.PlayClick)
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener { progress ->
                actionCallback?.onAction(ControlsAction.ProgressChange(progress))
            })

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Renderer click: $position")
                        actionCallback?.onAction(ControlsAction.SelectRenderer(position - 1))
                    }
            }

            with(mainContentDevicePicker) {
                adapter = contentDirectoriesAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Content directory click: $position")
                        actionCallback?.onAction(ControlsAction.SelectContentDirectory(position - 1))
                    }
            }
        }
    }

    fun toggle(): Boolean = when (behavior.state) {
        BottomSheetBehavior.STATE_HIDDEN -> {
            open()
            true
        }

        BottomSheetBehavior.STATE_HALF_EXPANDED,
        BottomSheetBehavior.STATE_EXPANDED,
        BottomSheetBehavior.STATE_COLLAPSED -> {
            close()
            false
        }
        else -> false
    }

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        showDismissListeners.forEach { listener -> listener.onShow() }
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        showDismissListeners.forEach { listener -> listener.onDismiss() }
    }

    fun addOnStateChangedAction(action: OnStateChangedAction) {
        bottomSheetCallback.addOnStateChangedAction(action)
    }

    fun addOnSlideAction(action: OnSlideAction) {
        bottomSheetCallback.addOnSlideAction(action)
    }

    fun setProgress(progress: Int, isEnabled: Boolean) {

    }

    fun setRenderers(items: List<SpinnerItem>) {
        rendererAdapter.setNewItems(items.addEmptyItem())
    }

    fun setContentDirectories(items: List<SpinnerItem>) {
        contentDirectoriesAdapter.setNewItems(items.addEmptyItem())
    }

    fun setControlsActionCallback(actionCallback: ControlsActionCallback) {
        this.actionCallback = actionCallback
    }

    fun addShowDismissListener(listener: ShowDismissListener) {
        showDismissListeners.add(listener)
    }

    private fun List<SpinnerItem>.addEmptyItem() =
        this.toMutableList().apply { add(0, SpinnerItem("Empty item")) }.toList()

    override fun onSaveInstanceState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
        contentDirectoriesAdapter.onSaveInstanceState(outState)
    }
}