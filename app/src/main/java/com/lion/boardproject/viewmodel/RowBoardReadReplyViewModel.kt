package com.lion.boardproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lion.boardproject.fragment.BoardReadFragment

class RowBoardReadReplyViewModel(val boardReadFragment: BoardReadFragment) : ViewModel() {
    // textViewRowBoardReadCommentNickName - text
    val textViewRowBoardReadReplyNickNameText = MutableLiveData("")
    // textViewRowBoardReadCommentText - text
    val textViewRowBoardReadReplyTextText = MutableLiveData("")
    // textViewRowBoardReadCommentUpdateDate - text
    val textViewRowBoardReadReplyUpdateDateText = MutableLiveData("")

}