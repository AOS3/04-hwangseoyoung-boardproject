package com.lion.boardproject.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.lion.boardproject.BoardActivity
import com.lion.boardproject.R
import com.lion.boardproject.databinding.FragmentBoardReadBinding
import com.lion.boardproject.databinding.RowBoardReadReplyBinding
import com.lion.boardproject.model.BoardModel
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.BoardService
import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.viewmodel.BoardReadViewModel
import com.lion.boardproject.viewmodel.RowBoardReadReplyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BoardReadFragment(val boardMainFragment: BoardMainFragment) : Fragment() {

    lateinit var fragmentBoardReadBinding: FragmentBoardReadBinding
    lateinit var boardActivity: BoardActivity

    // 현재 글의 문서 id를 담을 변수
    lateinit var boardDocumentId:String

    // 글 데이터를 담을 변수
    lateinit var boardModel:BoardModel

    // 댓글 RecyclerView를 구성하기 위해 사용할 리스트
    var replyRecyclerViewList = mutableListOf<ReplyModel>()

    // recyclerView 구성을 위한 임시 데이터
//    val tempData1 = Array(20) {
//        "닉네임 : $it"
//    }
//    val tempData2 = Array(20) {
//        "댓글 : $it"
//    }
//    val tempData3 = Array(20) {
//        "날짜 : $it"
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentBoardReadBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_board_read, container, false)
        fragmentBoardReadBinding.boardReadViewModel = BoardReadViewModel(this@BoardReadFragment)
        fragmentBoardReadBinding.lifecycleOwner = this@BoardReadFragment

        boardActivity = activity as BoardActivity

        // 이미지 뷰를 안보이는 상태로 설정한다.
        fragmentBoardReadBinding.imageViewBoardRead.isVisible = false

        // arguments의 값을 변수에 담아주는 메서드를 호출한다.
        gettingArguments()
        // 툴바를 구성하는 메서드를 호출한다.
        settingToolbar()
        // 댓글 리사이클러뷰 구성 메서드 호출
        settingReadReplyRecyclerView()
        // 글 데이터를 가져와 보여주는 메서드를 호출한다.
        settingBoardData()
        // 댓글 목록을 갱신하는 메서드 호출
        refreshReadReplyRecyclerView(boardDocumentId)

        return fragmentBoardReadBinding.root
    }

    // 이전 화면으로 돌아가는 메서드
    fun movePrevFragment(){
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_WRITE_FRAGMENT)
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
    }

    // 툴바를 구성하는 메서드
    fun settingToolbar(){
        fragmentBoardReadBinding.apply {
            // 메뉴를 보이지 않게 설정한다.
            toolbarBoardRead.menu.children.forEach {
                it.isVisible = false
            }

            toolbarBoardRead.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuItemBoardReadModify -> {
                        // 글의 문서 번호를 전달한다.
                        val dataBundle = Bundle()
                        dataBundle.putString("boardDocumentId", boardDocumentId)
                        boardMainFragment.replaceFragment(BoardSubFragmentName.BOARD_MODIFY_FRAGMENT, true, true, dataBundle)
                    }
                    R.id.menuItemBoardReadDelete -> {
                        val builder = MaterialAlertDialogBuilder(boardActivity)
                        builder.setTitle("글 삭제")
                        builder.setMessage("삭제시 복구할 수 없습니다")
                        builder.setNegativeButton("취소", null)
                        builder.setPositiveButton("삭제"){ dialogInterface: DialogInterface, i: Int ->
                            proBoardDelete()
                        }
                        builder.show()
                    }
                }
                true
            }
        }
    }

    // arguments의 값을 변수에 담아준다.
    fun gettingArguments(){
        boardDocumentId = arguments?.getString("boardDocumentId")!!
    }

    // 글 데이터를 가져와 보여주는 메서드
    fun settingBoardData(){
        // 서버에서 데이터를 가져온다.
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                BoardService.selectBoardDataOneById(boardDocumentId)
            }
            boardModel = work1.await()

            fragmentBoardReadBinding.apply {
                boardReadViewModel?.textFieldBoardReadTitleText?.value = boardModel.boardTitle
                boardReadViewModel?.textFieldBoardReadTextText?.value = boardModel.boardText
                boardReadViewModel?.textFieldBoardReadTypeText?.value = boardModel.boardTypeValue.str
                boardReadViewModel?.textFieldBoardReadNickName?.value = boardModel.boardWriterNickName

                // 작성자와 로그인한 사람이 같으면 메뉴를 보기에 한다.
                if(boardModel.boardWriteId == boardActivity.loginUserDocumentId){
                    toolbarBoardRead.menu.children.forEach {
                        it.isVisible = true
                    }
                }
            }

            // 첨부 이미지가 있다면
            if(boardModel.boardFileName != "none"){
                val work1 = async(Dispatchers.IO) {
                    // 이미지에 접근할 수 있는 uri를 가져온다.
                    BoardService.gettingImage(boardModel.boardFileName)
                }

                val imageUri = work1.await()
                boardActivity.showServiceImage(imageUri, fragmentBoardReadBinding.imageViewBoardRead)
                fragmentBoardReadBinding.imageViewBoardRead.isVisible = true
            }
        }
    }

    // 글 삭제 처리 메서드
    fun proBoardDelete(){
        CoroutineScope(Dispatchers.Main).launch {
            // 만약 첨부 이미지가 있다면 삭제한다.
            if(boardModel.boardFileName != "none"){
                val work1 = async(Dispatchers.IO){
                    BoardService.removeImageFile(boardModel.boardFileName)
                }
                work1.join()
            }
            // 글 정보를 삭제한다.
            val work2 = async(Dispatchers.IO){
                BoardService.deleteBoardData(boardDocumentId)
            }
            work2.join()
            // 글 목록 화면으로 이동한다.
            boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
        }
    }

    // 댓글 RecyclerView 구성 메서드
    fun settingReadReplyRecyclerView() {
        fragmentBoardReadBinding.apply {
            recyclerViewBoardReplyList.adapter = ReadReplyRecyclerViewAdapter()
            recyclerViewBoardReplyList.layoutManager = LinearLayoutManager(boardActivity)
            val deco = MaterialDividerItemDecoration(boardActivity, MaterialDividerItemDecoration.VERTICAL)
            recyclerViewBoardReplyList.addItemDecoration(deco)
        }
    }

    // 댓글 RecyclerView 어댑터
    inner class ReadReplyRecyclerViewAdapter : RecyclerView.Adapter<ReadReplyRecyclerViewAdapter.ReplyViewHolder>(){
        // ViewHolder
        inner class ReplyViewHolder(var rowBoardReadReplyBinding: RowBoardReadReplyBinding): RecyclerView.ViewHolder(rowBoardReadReplyBinding.root)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
            val rowBoardReadReplyBinding = DataBindingUtil.inflate<RowBoardReadReplyBinding>(layoutInflater, R.layout.row_board_read_reply, parent, false)
            rowBoardReadReplyBinding.rowBoardReadReplyViewmodel = RowBoardReadReplyViewModel(this@BoardReadFragment)
            rowBoardReadReplyBinding.lifecycleOwner = this@BoardReadFragment

            val readReplyViewHolder = ReplyViewHolder(rowBoardReadReplyBinding)

            return readReplyViewHolder
        }

        override fun getItemCount(): Int {
            return replyRecyclerViewList.size
        }

        override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
            val replyModel = replyRecyclerViewList[position]

            if (replyModel.replyNickname == boardActivity.loginUserNickName) {
                holder.rowBoardReadReplyBinding.buttonRowBoardReadReplyManage.isVisible = true
            } else {
                holder.rowBoardReadReplyBinding.buttonRowBoardReadReplyManage.isVisible = false
            }

            Log.d("test100", "댓글 작성자: ${replyModel.replyNickname}, 현재 계정: ${boardActivity.loginUserNickName}")

            holder.rowBoardReadReplyBinding.rowBoardReadReplyViewmodel?.textViewRowBoardReadReplyNickNameText?.value = replyRecyclerViewList[position].replyNickname
            holder.rowBoardReadReplyBinding.rowBoardReadReplyViewmodel?.textViewRowBoardReadReplyTextText?.value = replyRecyclerViewList[position].replyText
            holder.rowBoardReadReplyBinding.rowBoardReadReplyViewmodel?.textViewRowBoardReadReplyUpdateDateText?.value = formatTime(replyRecyclerViewList[position].replyTimeStamp)

            // 버튼 클릭 리스너 설정
            holder.rowBoardReadReplyBinding.buttonRowBoardReadReplyManage.setOnClickListener {

                val replyManageBottomSheet = BoardReadReplyBottomSheetFragment(
                    this@BoardReadFragment,
                    position,
                    replyModel.replyDocumentId
                )
                replyManageBottomSheet.show(boardActivity.supportFragmentManager, "BottomSheet")
            }
        }

        // 밀리초를 포맷된 시간으로 변환
        private fun formatTime(timeInMillis: Long): String {
            val sdf =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timeInMillis))
        }
    }


    // 입력 요소 초기화 메서드 (댓글)
    fun resetInput(){
        fragmentBoardReadBinding.apply {
            boardReadViewModel?.textFieldBoardReadReplyTextText?.value = ""
        }
    }

    // 댓글 작성 완료 처리 메서드
    fun proBoardReplySubmit() {
        fragmentBoardReadBinding.apply {
            // 댓글 작성자 닉네임
            var replyNickname = boardActivity.loginUserDocumentId
            // 댓글 내용
            var replyText = boardReadViewModel?.textFieldBoardReadReplyTextText?.value!!
            // 댓글이 달린 글 구분 값
            var replyBoardId = boardDocumentId
            // 시간
            var replyTimeStamp = System.currentTimeMillis()
            // 상태
            var replyState = ReplyState.REPLY_STATE_NORMAL

            if (replyText.isEmpty()) {
                boardActivity.showMessageDialog("입력 오류", "댓글을 입력해주세요", "확인") {
                    boardActivity.showSoftInput(textFieldBoardReadReply)
                }
                return
            }

            // 업로드
            CoroutineScope(Dispatchers.Main).launch {
                // 서버에 저장할 댓글 데이터
                val replyModel = ReplyModel()
                replyModel.replyNickname = replyNickname
                replyModel.replyText = replyText
                replyModel.replyBoardId = replyBoardId
                replyModel.replyTimeStamp = replyTimeStamp
                replyModel.replyState = replyState

                // 댓글을 저장한다.
                val work1 = async(Dispatchers.IO) {
                    BoardService.addBoardReplyData(replyModel)
                }
                work1.await()

                val work2 = async(Dispatchers.IO) {
                    // 글에 댓글 ID 를 업데이트한다.
                    boardModel.boardReply.add(replyModel)
                    BoardService.updateReplyToBoardData(boardModel)
                }
                work2.await()

                // 댓글 입력 초기화
                resetInput()
                // 댓글 목록을 갱신한다.
                refreshReadReplyRecyclerView(boardModel.boardDocumentId)
            }
        }
    }

    // 댓글 목록(readReplyRecyclerView)을 갱신한다.
    fun refreshReadReplyRecyclerView(documentId:String){

        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                BoardService.getRepliesByBoardId(documentId)
            }
            replyRecyclerViewList = work1.await()

            fragmentBoardReadBinding.recyclerViewBoardReplyList.adapter?.notifyDataSetChanged()

            // 댓글 수 갱신
            val replyCount = replyRecyclerViewList.size
            fragmentBoardReadBinding.boardReadViewModel?.textViewBoardCommentCountText?.value = "댓글 $replyCount"

        }
    }


}
