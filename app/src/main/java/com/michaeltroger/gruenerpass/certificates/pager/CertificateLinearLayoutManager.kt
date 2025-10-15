package com.michaeltroger.gruenerpass.certificates.pager

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

class CertificateLinearLayoutManager(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayoutManager(context, attributeSet, defStyleAttr, defStyleRes) {
    override fun canScrollHorizontally(): Boolean {
        return itemCount > 1
    }
}
