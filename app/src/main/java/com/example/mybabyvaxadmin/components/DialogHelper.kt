package com.example.mybabyvaxadmin.components

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.developer.kalert.KAlertDialog
import com.developer.kalert.R

import kotlin.apply
import kotlin.collections.forEach
import kotlin.let

class DialogHelper {

    companion object {

        private fun enlargeButtons(dialog: KAlertDialog) {
            val confirmButton =
                dialog.findViewById<Button>(R.id.custom_confirm_button)
            val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

            listOf(confirmButton, cancelButton).forEach { button ->
                button?.let {
                    val params = it.layoutParams


                    if (params is ViewGroup.LayoutParams) {
                        val newParams = (params as? LinearLayout.LayoutParams)
                        newParams?.apply {
                            height = dpToPx(dialog.context, 50)
                            width = 0
                            weight = 1f
                        }
                        it.layoutParams = newParams
                    }

                    it.textSize = 18f
                }
            }
        }

        private fun dpToPx(context: Context, dp: Int): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }

        fun showSuccess(
            context: Context,
            title: String,
            message: String,
            onConfirm: (() -> Unit)? = null
        ) {
            val dialog = KAlertDialog(context, KAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmClickListener { d ->
                    d.dismissWithAnimation()
                    onConfirm?.invoke()
                }
            dialog.show()
            enlargeButtons(dialog)
        }

        fun showError(
            context: Context,
            title: String,
            message: String,
            onConfirm: (() -> Unit)? = null
        ) {
            val dialog = KAlertDialog(context, KAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmClickListener { d ->
                    d.dismissWithAnimation()
                    onConfirm?.invoke()
                }
            dialog.show()
            enlargeButtons(dialog)
        }

        fun showWarning(
            context: Context,
            title: String,
            message: String,
            onConfirm: (() -> Unit)? = null,
            onCancel: (() -> Unit)? = null
        ) {
            val dialog = KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText("Yes")
                .setCancelText("No")
                .setConfirmClickListener { d ->
                    d.dismissWithAnimation()
                    onConfirm?.invoke()
                }
                .setCancelClickListener { d ->
                    d.dismissWithAnimation()
                    onCancel?.invoke()
                }
            dialog.show()
            enlargeButtons(dialog)
        }
    }
}
