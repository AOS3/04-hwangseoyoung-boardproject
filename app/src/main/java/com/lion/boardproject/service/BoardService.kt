package com.lion.boardproject.service

import android.net.Uri
import android.util.Log
import com.lion.boardproject.model.BoardModel
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.repository.BoardRepository
import com.lion.boardproject.repository.BoardRepository.Companion
import com.lion.boardproject.repository.UserRepository
import com.lion.boardproject.util.BoardType
import com.lion.boardproject.vo.BoardVO
import com.lion.boardproject.vo.ReplyVO
import com.lion.boardproject.vo.UserVO

class BoardService {

    companion object{
        // 이미지 데이터를 서버로 업로드 하는 메서드
        suspend fun uploadImage(sourceFilePath:String, serverFilePath:String){
            BoardRepository.uploadImage(sourceFilePath, serverFilePath)
        }

        // 글 데이터를 저장하는 메서드
        // 새롭게 추가된 문서의 id를 반환한다.
        suspend fun addBoardData(boardModel: BoardModel) : String{
            // VO 객체를 생성한다.
            val boardVO = boardModel.toBoardVO()
            // 저장한다.
            val documentId = BoardRepository.addBoardData(boardVO)
            return documentId
        }

        // 글의 문서 id를 통해 글 데이터를 가져온다.
        suspend fun selectBoardDataOneById(documentId:String) : BoardModel{
            // 글 데이터를 가져온다.
            val boardVO = BoardRepository.selectBoardDataOneById(documentId)
            // BoardModel객체를 생성한다.
            val boardModel = boardVO.toBoardModel(documentId)

            // 사용자 정보를 가져온다.
            val userVO = UserRepository.selectUserDataByUserDocumentIdOne(boardModel.boardWriteId)
            val userModel = userVO.toUserModel(boardModel.boardWriteId)
            // 닉네임을 담아준다.
            boardModel.boardWriterNickName = userModel.userNickName

            return boardModel
        }

        // 이미지 데이터를 가져온다.
        suspend fun gettingImage(imageFileName:String) : Uri {
            val imageUri = BoardRepository.gettingImage(imageFileName)
            return imageUri
        }

        // 글 목록을 가져오는 메서드
        suspend fun gettingBoardList(boardType: BoardType) : MutableList<BoardModel>{
            // 글정보를 가져온다.
            val boardList = mutableListOf<BoardModel>()
            val resultList = BoardRepository.gettingBoardList(boardType)
            // 사용자 정보를 가져온다.
            val userList = UserRepository.selectUserDataAll()
            // 사용자 정보를 맵에 담는다.
            val userMap = mutableMapOf<String, String>()
            userList.forEach {
                val userDocumentId = it["user_document_id"] as String
                val userVO = it["user_vo"] as UserVO
                userMap[userDocumentId] = userVO.userNickName
            }

            resultList.forEach {
                val boardVO = it["boardVO"] as BoardVO
                val documentId = it["documentId"] as String
                val boardModel = boardVO.toBoardModel(documentId)
                boardModel.boardWriterNickName = userMap[boardModel.boardWriteId]!!
                boardList.add(boardModel)
            }

            return boardList
        }

        // 검색 결과를 반환하는 메서드
        suspend fun gettingBoardSearchList(boardType: BoardType, keyword:String) : MutableList<BoardModel>{
            // 해당 게시판에 해당하는 글 데이터를 가져온다.
            val boardList = gettingBoardList(boardType)
            // 검색 결과를 담을 리스트
            val resultList = mutableListOf<BoardModel>()
            // 게시글의 수 만큼 반복한다.
            boardList.forEach {
                // 제목이나 내용에 검색어가 포함되어 있다면
                if(it.boardTitle.contains(keyword) or it.boardText.contains(keyword)){
                    // 객체를 리스트에 담는다.
                    resultList.add(it)
                }
            }
            // 반환한다.
            return resultList
        }


        // 글 정보를 수정하는 메서드
        suspend fun updateBoardData(boardModel: BoardModel){
            // vo 객체에 담아준다.
            val boardVO = boardModel.toBoardVO()
            // 수정하는 메서드를 호출한다.
            BoardRepository.updateBoardData(boardVO, boardModel.boardDocumentId)
        }

        // 서버에서 이미지 파일을 삭제한다.
        suspend fun removeImageFile(imageFileName:String){
            BoardRepository.removeImageFile(imageFileName)
        }

        // 서버에서 글을 삭제한다.
        suspend fun deleteBoardData(boardDocumentId:String){
            BoardRepository.deleteBoardData(boardDocumentId)
        }

        // 댓글 데이터를 저장하는 메서드
        // 새롭게 추가된 문서의 id를 반환
        suspend fun addBoardReplyData(replyModel: ReplyModel) : String {
            // VO 객체를 생성한다.
            val replyVO = replyModel.toReplyVO()
            // 저장한다.
            val documentId = BoardRepository.addBoardReplyData(replyVO)
            return documentId
        }

        // 글에 댓글 Id를 업데이트하는 메서드
        suspend fun updateReplyToBoardData(boardModel: BoardModel){
            // vo 객체에 담아준다.
            val boardVO = boardModel.toBoardVO()
            // 수정하는 메서드를 호출한다.
            BoardRepository.updateReplyToBoardData(boardVO, boardModel.boardDocumentId)
        }

        // 글의 댓글 리스트를 가져오는 메서드
        suspend fun getRepliesByBoardId(documentId: String): MutableList<ReplyModel> {
            val replyList = BoardRepository.getRepliesByBoardId(documentId)
            // 사용자 정보를 가져온다.
            val userList = UserRepository.selectUserDataAll()
            // 사용자 정보를 맵에 담는다.
            val userMap = mutableMapOf<String, String>()
            userList.forEach {
                val userDocumentId = it["user_document_id"] as String
                val userVO = it["user_vo"] as UserVO
                userMap[userDocumentId] = userVO.userNickName
            }

            // 반환할 ReplyModel 리스트
            val replyModelList = mutableListOf<ReplyModel>()

            replyList.forEach {
                val replyVO = it["replyVO"] as ReplyVO
                val documentId = it["documentId"] as String
                val replyModel = replyVO.toReplyModel(documentId)
                replyModel.replyNickname = userMap[replyModel.replyNickname]!!
                replyModelList.add(replyModel)
            }

            return replyModelList
        }

        // 댓글 리스트에서 DocumentId를 가져온다.
        suspend fun getRepliesDocumentIdsByBoardId(documentId: String): List<String> {
            return BoardRepository.getRepliesDocumentIdsByBoardId(documentId)
        }

        // 서버에서 댓글을 삭제한다.
        suspend fun deleteReplyData(replyDocumentId:String){
            BoardRepository.deleteReplyData(replyDocumentId)
        }

        // 댓글을 수정하는 메서드
        suspend fun updateReplyData(replyModel: ReplyModel){
            // vo 객체에 담아준다.
            val replyVO = replyModel.toReplyVO()
            // 수정하는 메서드를 호출
            BoardRepository.updateReplyData(replyVO, replyModel.replyDocumentId)
        }
    }
}