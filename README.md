[Uplo# 댓글 기능 구현 (평가)

# 💬 프로젝트 설명

- 게시판 프로젝트 - 게시글 내 댓글 기능 구현
- 프로젝트 진행 : Android 3기 4팀 황서영
---

# 🛖 레이아웃 구성

## 참고 (당근마켓 앱 🥕 - 동네생활 게시글)

SNS 및 소셜 네트워크 환경에서는 화면 전환(댓글 페이지 이동) 또는 Bottom Sheet를 많이 사용하지만,

현재 배포되고 있는 커뮤니티,  게시판이 목적인 앱에서는 게시글 하단에 리스트를 보여주는 것으로 많이 구현되어있는 것으로 보여

게시글 내용 하단에 댓글 목록이 표시되도록 구현했습니다.

- 게시글 내 댓글 목록 표시
- 댓글 목록 상단 댓글 개수 표시
- 화면 하단에 댓글 입력 필드 표시
![스크린샷 2024-12-27 오후 7 15 06](https://github.com/user-attachments/assets/0b5c63ad-d99d-4454-954b-b583da66ffa8)




## BoardReadFragment View 구성

![image 3](https://github.com/user-attachments/assets/f7a9078b-90f2-4757-a3c3-4fd3d10a4e22)


# 🗒️ 댓글 기능 구현 계획

- 댓글을 입력할 수 있어야한다.
- 게시글에 해당하는 댓글이 표시되어야 한다.
- 댓글 수와 목록이 업데이트 되어 표시되어야한다.
- 댓글 수정 및 삭제가 가능하여야 한다.
- 댓글 계정과 현재 계정이 다를 경우 수정 및 삭제가 불가하여야 한다.

# 기능 구현 내용 정리

## 구조

게시글을 불러올 때 BoardData의 boardReply를 통해 댓글 데이터도 함께 가져오는 구조로

BoardRepository와 BoardService 내에 추가로 댓글 기능을 구현했습니다.

```markdown
- BoardRepository
    - 댓글 데이터 저장
    - 게시글에 댓글 DocumentId 저장
    - 댓글 데이터 가져오기
    - 댓글 수정
    - 댓글 삭제
- BoardService
    - 댓글 데이터 저장
    - 게시글에 댓글 DocumentId 저장
    - 댓글 데이터 가져오기
    - 댓글 수정
    - 댓글 삭제
```

## 화면

- **ScrollView → NestedScrollView로 전환**
    
    : 같은 ViewGroup 내에 수직 방향의 RecyclerView와 또 다른 수직 방향의 레이아웃을 중첩시킬 때에 사용하는 레이아웃
    
    `문제`
    
    NestedScrollView 내의 RecyclerView는 메모리가 재활용되지 않아 전체 아이템의 개수에 비례하여 메모리가 누적됨
    
    아이템 개수가 많을 경우 앱의 메모리 성능이 크게 저하되기 때문에
    
    NestedScrollView 안에 RecyclerView를 배치해주면 부자연적인 스크롤이 발생하는 경우가 있음
    
    `해결`
    
    → 중첩 스크롤 속성(nestedScrollingEnabled)을 비활성화해준다.
    
![image 4](https://github.com/user-attachments/assets/19fd93d0-77ed-4f1c-98c9-a74d6cc745ae)



- **BottomSheet 추가**
    - 댓글을 관리하기 위한 Bottom Sheet 추가
    - Fragment를 추가하여 수정, 삭제를 눌렀을때 다이얼로그가 표시되며 댓글 데이터가 업데이트 되도록 함
![image 5](https://github.com/user-attachments/assets/aeb5c4d9-5781-49c1-a163-e1f32d0d59b7)


