package com.umma.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umma.app.R
import com.umma.app.databinding.ItemGenericListBinding

data class ListItem(
    val id: Long? = null,
    val rawId: String? = null,
    val title: String,
    val subtitle: String,
    val extra: String,
    val badgeText: String? = null,
    val isPaid: Boolean? = null,
    val grade: String? = null,
    val actionText: String? = null,
    val onActionClick: (() -> Unit)? = null,
    val onDeleteClick: (() -> Unit)? = null
)

class GenericAdapter(
    private var items: List<ListItem>
) : RecyclerView.Adapter<GenericAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemGenericListBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateItems(newItems: List<ListItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGenericListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.tvTitle.text = item.title
        holder.binding.tvSubtitle.text = item.subtitle
        holder.binding.tvExtra.text = item.extra

        // Badge Handling
        if (item.badgeText != null) {
            holder.binding.tvBadge.visibility = View.VISIBLE
            holder.binding.tvBadge.text = item.badgeText
            holder.binding.tvBadge.setBackgroundResource(R.drawable.bg_chip_paid)
            holder.binding.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.umma_primary))
        } else if (item.isPaid != null) {
            holder.binding.tvBadge.visibility = View.VISIBLE
            if (item.isPaid == true) {
                holder.binding.tvBadge.text = "PAID"
                holder.binding.tvBadge.setBackgroundResource(R.drawable.bg_chip_paid)
                holder.binding.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.umma_success))
            } else {
                holder.binding.tvBadge.text = "UNPAID"
                holder.binding.tvBadge.setBackgroundResource(R.drawable.bg_chip_unpaid)
                holder.binding.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.umma_error))
            }
        } else if (item.grade != null) {
            holder.binding.tvBadge.visibility = View.VISIBLE
            holder.binding.tvBadge.text = "Grade: ${item.grade}"
            val color = when (item.grade) {
                "A" -> R.color.umma_success
                "B" -> R.color.umma_accent
                "C" -> R.color.umma_warning
                else -> R.color.umma_error
            }
            holder.binding.tvBadge.setTextColor(ContextCompat.getColor(context, color))
            holder.binding.tvBadge.setBackgroundResource(R.drawable.bg_chip_paid)
        } else {
            holder.binding.tvBadge.visibility = View.GONE
        }

        // Actions Handling
        val hasAction = item.onActionClick != null && item.actionText != null
        val hasDelete = item.onDeleteClick != null

        if (hasAction || hasDelete) {
            holder.binding.layoutActions.visibility = View.VISIBLE

            if (hasAction) {
                holder.binding.btnActionButton.visibility = View.VISIBLE
                holder.binding.btnActionButton.text = item.actionText
                holder.binding.btnActionButton.setOnClickListener { item.onActionClick?.invoke() }
            } else {
                holder.binding.btnActionButton.visibility = View.GONE
            }

            if (hasDelete) {
                holder.binding.btnDelete.visibility = View.VISIBLE
                holder.binding.btnDelete.setOnClickListener { item.onDeleteClick?.invoke() }
            } else {
                holder.binding.btnDelete.visibility = View.GONE
            }
        } else {
            holder.binding.layoutActions.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}