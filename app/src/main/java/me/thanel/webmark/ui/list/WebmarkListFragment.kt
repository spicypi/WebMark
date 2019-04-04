package me.thanel.webmark.ui.list

import android.content.ClipDescription
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_webmark_list.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.thanel.recyclerviewutils.adapter.lazyAdapterWrapper
import me.thanel.webmark.R
import me.thanel.webmark.data.Webmark
import me.thanel.webmark.data.ext.isRead
import me.thanel.webmark.ext.viewModel
import me.thanel.webmark.saveaction.SaveWebmarkService
import me.thanel.webmark.ui.base.BaseFragment
import me.thanel.webmark.ui.touchhelper.ItemTouchCallback
import org.kodein.di.generic.instance

class WebmarkListFragment : BaseFragment(R.layout.fragment_webmark_list) {

    private val viewModel: WebmarkListViewModel by viewModel()
    private val clipboard: ClipboardManager by instance()

    private val adapterWrapper by lazyAdapterWrapper {
        register(WebmarkViewBinder(), WebmarkItemCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webmarkRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        webmarkRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        webmarkRecyclerView.adapter = adapterWrapper.adapter

        val itemTouchCallback = ItemTouchCallback.create { position, direction ->
            val item = adapterWrapper.adapter.items.getOrNull(position)
            when (item) {
                is Webmark -> onSwiped(direction, item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(webmarkRecyclerView)

        filterInput.doAfterTextChanged {
            viewModel.filterText = it?.toString() ?: ""
        }

        archiveCheckBox.onCheckedChanged = { isChecked ->
            viewModel.showArchive = isChecked
        }

        view.findViewById<MotionLayout>(R.id.toolbar).setTransitionListener(object: TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (currentId == R.id.start) {
                    searchIcon.setImageResource(R.drawable.ic_search)
                    filterInput.clearFocus()
                    filterInput.text.clear()
                } else {
                    searchIcon.setImageResource(R.drawable.ic_close)
                    filterInput.requestFocus()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.unreadWebmarks.observe(this, Observer {
            noWebmarksView.isVisible = it.isEmpty()
            GlobalScope.launch(Dispatchers.IO) {
                adapterWrapper.updateItems(it)
            }
        })

        if (savedInstanceState == null) {
            suggestSaveCopiedUrl()
        }
    }

    private fun suggestSaveCopiedUrl() {
        val hasTextMimeType =
            clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ?: false
        if (!clipboard.hasPrimaryClip() || !hasTextMimeType) return

        val item = clipboard.primaryClip?.getItemAt(0) ?: return
        val text = item.text.toString()
        if (!Patterns.WEB_URL.matcher(text).matches()) return

        val message = getString(R.string.question_save_copied_url, text)
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_save) {
                SaveWebmarkService.start(requireContext(), Uri.parse(text))
            }
            .show()
    }

    private fun onSwiped(direction: Int, item: Webmark) {
        if (item.isRead) {
            if (direction == ItemTouchHelper.RIGHT) {
                delete(item.id)
            } else {
                viewModel.markWebmarkAsUnread(item.id)
            }
            return
        }

        if (direction == ItemTouchHelper.RIGHT) {
            markAsRead(item.id)
        }
    }

    private fun markAsRead(id: Long) {
        viewModel.markWebmarkAsRead(id)

        Snackbar.make(coordinator, R.string.info_marked_read, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo) { viewModel.markWebmarkAsUnread(id) }
            .show()
    }

    private fun delete(id: Long) {
        viewModel.deleteWebmark(id)
        Snackbar.make(coordinator, R.string.info_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo) { viewModel.undoDeleteWebmark(id) }
            .show()
    }

}
