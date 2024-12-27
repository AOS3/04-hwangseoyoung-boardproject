package com.lion.boardproject.viewmodel

import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.lion.boardproject.fragment.BoardReadFragment

class BoardReadViewModel(val boardReadFragment: BoardReadFragment) : ViewModel() {
    // textFieldBoardReadTitle - text
    val textFieldBoardReadTitleText = MutableLiveData(" ")
    // textFieldBoardReadType - text
    val textFieldBoardReadTypeText = MutableLiveData(" ")
    // textFieldBoardReadText - text
    val textFieldBoardReadTextText = MutableLiveData(" ")
    // textFieldBoardReadNickName - text
    val textFieldBoardReadNickName = MutableLiveData(" ")

    // textFieldBoardReadReplyText - text
    val textFieldBoardReadReplyTextText = MutableLiveData("")

    // textViewBoardCommentCount - text
    val textViewBoardCommentCountText = MutableLiveData("")

    // buttonBoardReadReplyAdd - onClick
    fun buttonBoardReadReplyAddOnClick() {
        boardReadFragment.proBoardReplySubmit()
    }

    companion object {
        // toolbarBoardRead - onNavigationClickBoardRead
        @JvmStatic
        @BindingAdapter("onNavigationClickBoardRead")
        fun onNavigationClickBoardRead(materialToolbar: MaterialToolbar, boardReadFragment: BoardReadFragment){
            materialToolbar.setNavigationOnClickListener {
                boardReadFragment.movePrevFragment()
            }
        }
    }
}