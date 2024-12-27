package com.lion.boardproject.viewmodel

import androidx.lifecycle.ViewModel
import com.lion.boardproject.fragment.BoardReadFragment
import com.lion.boardproject.fragment.BoardReadReplyBottomSheetFragment

class BoardReadReplyBottomSheetViewModel(val boardReadReplyBottomSheetFragment: BoardReadReplyBottomSheetFragment) : ViewModel() {

    // buttonBoardReadReplyBottomSheetModify - onClick
    fun buttonBoardReadReplyBottomSheetModifyOnClick() {
        boardReadReplyBottomSheetFragment.settingModifyButton()
    }
    // buttonBoardReadReplyBottomSheetDelete - onClick
    fun buttonBoardReadReplyBottomSheetDeleteOnClick() {
        boardReadReplyBottomSheetFragment.settingDeleteButton()
    }
}