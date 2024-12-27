package com.lion.boardproject.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.boardproject.BoardActivity
import com.lion.boardproject.R
import com.lion.boardproject.databinding.DialogBoardReadReplyBinding
import com.lion.boardproject.databinding.FragmentBoardReadReplyBottomSheetBinding
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.BoardService
import com.lion.boardproject.viewmodel.BoardReadReplyBottomSheetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class BoardReadReplyBottomSheetFragment(
    val boardReadFragment: BoardReadFragment,
    val selectedPosition: Int,
    val selectedReplyDocumentId: String
) : BottomSheetDialogFragment() {

    lateinit var fragmentBoardReadReplyBottomSheetBinding:FragmentBoardReadReplyBottomSheetBinding
    lateinit var boardActivity: BoardActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBoardReadReplyBottomSheetBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_board_read_reply_bottom_sheet, container, false)
        fragmentBoardReadReplyBottomSheetBinding.boardReadReplyBottomSheetViewModel = BoardReadReplyBottomSheetViewModel(this@BoardReadReplyBottomSheetFragment)
        fragmentBoardReadReplyBottomSheetBinding.lifecycleOwner = this@BoardReadReplyBottomSheetFragment

        boardActivity = activity as BoardActivity


        return fragmentBoardReadReplyBottomSheetBinding.root
    }

    // 수정 버튼 처리 메서드
    fun settingModifyButton() {
        fragmentBoardReadReplyBottomSheetBinding.apply {
            val builder = MaterialAlertDialogBuilder(boardActivity)
            builder.setTitle("댓글 수정")

            val dialogBoardReadReplyBinding = DialogBoardReadReplyBinding.inflate(layoutInflater)
            builder.setView(dialogBoardReadReplyBinding.root)

            // 기존 댓글 텍스트를 가져와 설정
            val initialText = boardReadFragment.replyRecyclerViewList[selectedPosition].replyText
            dialogBoardReadReplyBinding.textFieldBoardReadReplyModify.editText?.setText(initialText)

            // 입력 요소에 포커스를 준다.
            boardActivity.showSoftInput(dialogBoardReadReplyBinding.textFieldBoardReadReplyModify.editText!!)

            builder.setNegativeButton("취소", null)
            builder.setPositiveButton("수정", null)

            val dialog = builder.create()

            // PositiveButton 활성화 여부
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false

                // 내용이 비어있지 않으면 활성화
                dialogBoardReadReplyBinding.textFieldBoardReadReplyModify.editText?.addTextChangedListener { text ->
                    positiveButton.isEnabled = !text.isNullOrBlank()
                }

                positiveButton.setOnClickListener {
                    val modifiedText = dialogBoardReadReplyBinding.textFieldBoardReadReplyModify.editText?.text.toString()

                    // 댓글 수정
                    boardReadFragment.replyRecyclerViewList[selectedPosition].replyText = modifiedText

                    proReplyModify()

                    boardReadFragment.refreshReadReplyRecyclerView(boardReadFragment.boardDocumentId)

                    // Dialog 닫기
                    dialog.dismiss()

                }
            }
            dialog.show()

            dismiss()
        }
    }

    // 삭제 버튼 처리 메서드
    fun settingDeleteButton() {
        fragmentBoardReadReplyBottomSheetBinding.apply {
            val builder = MaterialAlertDialogBuilder(boardActivity)
            builder.setTitle("댓글 삭제")
            builder.setMessage("""
                    댓글을 삭제할까요?       
                """.trimIndent())

            builder.setNegativeButton("취소", null)
            builder.setPositiveButton("삭제"){ dialogInterface: DialogInterface, i: Int ->
                proReplyDelete()

                boardReadFragment.refreshReadReplyRecyclerView(boardReadFragment.boardDocumentId)
                boardReadFragment.replyRecyclerViewList.removeAt(selectedPosition)
                boardReadFragment.fragmentBoardReadBinding.recyclerViewBoardReplyList.adapter?.notifyItemRemoved(selectedPosition)
            }
            builder.show()

            // BottomSheet를 내려준다.
            dismiss()
        }
    }

    fun proReplyDelete() {
        CoroutineScope(Dispatchers.Main).launch {
            // 글 정보를 삭제한다.
            val work1 = async(Dispatchers.IO){
                BoardService.deleteReplyData(selectedReplyDocumentId)
            }
            work1.join()
        }
    }

    fun proReplyModify() {
        CoroutineScope(Dispatchers.Main).launch {
            // 댓글 데이터를 가져온다
            val replyModel = boardReadFragment.replyRecyclerViewList[selectedPosition]
            // 글 정보를 삭제한다.
            val work1 = async(Dispatchers.IO){
                BoardService.updateReplyData(replyModel)
            }
            work1.join()
        }
    }

}