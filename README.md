﻿# FriendTalk
private -> public

작품이름 : Friend Talk

작품 시연 영상 : https://www.youtube.com/watch?v=7OROxq_otls&t=2s

프로젝트 참여자 : 김윤규(1명)

[작품 소개]
 
친구들과 SNS, 채팅, 간단한 미니게임을 즐길 수 있는 어플리케이션입니다.
 
[주요 기능]

1. 회원가입
 
- 아이디 중복확인을 할 수 있습니다.

- 닉네임 중복확인을 할 수 있습니다.

- 이미지를 업로드할 수 있습니다.
 
2. 아이디 찾기
 
- 자신의 이메일을 닉네임, 휴대폰 번호로 찾을 수 있습니다.
 
3. 비밀번호 찾기
 
- 자신의 비밀번호를 이메일, 닉네임, 휴대폰 번호로 찾을 수 있습니다. 

4. 친구목록
 
- 다른 유저의 아이디를 검색하여 친구추가 할 수 있습니다.
 
- 자신의 아이디와 이미 친구 추가된 유저는 검색할 수 없습니다.
 
- 친구를 삭제할 수 있습니다.
 
- 자신의 프로필 사진을 변경할 수 있습니다.
 
- 채팅방을 생성할 수 있습니다.
 
- 상대 유저에게 전화를 할 수 있습니다.
 
5. 채팅
 
- 1:1 채팅이 가능합니다.
 
- 그룹 채팅이 가능합니다.
 
- 각 채팅 마다 메시지 읽음 표시를 구현하여 현재 메시지를 몇 명이 읽었는지 확인 할 수 있습니다.
 
- 상대방이 메시지를 보내면 PUSH 알림이 울립니다. 현재 유저가 채팅방에 들어와 있을 경 우 알림이 울리지 않습니다.
 
- 이미지를 주고 받을 수 있고 갤러리에 저장할 수 있습니다.
 
6. 채팅 목록
 
- 상대방 유저의 프로필 사진, 마지막으로 보낸 메시지의 내용과 시간을 확인할 수 있습니다.
 
- 마지막 메시지가 사진일 경우 채팅 목록에서 “사진”으로 표시됩니다.
 
7. SNS
 
- 게시글을 등록할 수 있습니다. (이미지, 텍스트)
 
- 친구추가 한 유저들의 게시글을 볼 수 있습니다.
 
- 자신의 게시글을 수정, 삭제할 수 있습니다.
 
- 좋아요 기능을 사용할 수 있습니다.
 
- 게시물에 댓글을 달 수 있습니다.
 
- 좋아요 개수와 댓글 개수를 게시물마다 확인할 수 있습니다.
 
- 게시글 목록에서 스크롤을 끝까지 올려 새로고침 기능을 사용할 수 있습니다.
 
8. 고객센터
 
- 유저가 회사에 불편사항을 쉽게 전달할 수 있도록 전화, 메일, 홈페이지 이동 버튼을 만들었습니다.
 
- 광고 배너 내용이 6초마다 바뀝니다.
 
- 광고 배너에 X표시를 누르면 광고가 천천히 사라지도록 구현하였습니다.
 
9. 미니게임
 
- 게임 시작 버튼을 누르면 1부터 20까지의 숫자들이 랜덤으로 화면에 나타납니다.
 
- 30초 안에 1부터 20까지의 숫자들을 차례로 클릭했을 때 게임이 끝나게 됩니다.

[사용기술]
- Firebase(Auth, RealtimeBase, Storage)
- FCM
- Picasso
- Http(3.9.1)
- Gson(2.4)
