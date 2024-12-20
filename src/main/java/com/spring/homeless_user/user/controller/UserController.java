package com.spring.homeless_user.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

import static org.hibernate.query.sqm.tree.SqmNode.log;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;

    //회원가입
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public CommonResDto userSignUp(
            @RequestPart("img") MultipartFile img,
            @RequestPart("data") String dataJson) throws IOException {
        log.info("singup");

        ObjectMapper objectMapper = new ObjectMapper();
        UserSaveReqDto dto = objectMapper.readValue(dataJson, UserSaveReqDto.class);
        return userService.userSignUp(dto, img);
    }

    // 이메일 인증번호 확인
    @GetMapping("/confirm")
    public CommonResDto confirm(@RequestParam String email, String token) {
    log.info("confirm");
        return userService.confirm(email, token);
    }

    // 로그인
    @PostMapping("/signin")
    public CommonResDto userSignIn(@RequestBody UserLoginReqDto dto) {
        log.info("signin");
        return userService.userSignIn(dto);
    }

//    @PostMapping("/refreshToken")
//    public CommonResDto reissueAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
//
//        return userService.refreshToken(refreshToken);
//    }
//
//    @PostMapping("/gitlogin")
//    public CommonResDto gitLogin(@Valid @RequestBody UserLoginReqDto dto) {
//
//        return userService.gitLogin(dto);
//    }

    // 이메일 중복검사
    @GetMapping("/duplicate/email")
    public CommonResDto duplicateEmail(@RequestParam String email) {
    log.info("duplicateEmail");
        return userService.duplicateEmail(email);
    }

    //닉네임 중복검사
    @GetMapping("/duplicate/nickname")
    public CommonResDto gitLogin(@RequestParam String nickname) {
    log.info("duplicateNickname");
    return userService.duplicateNickname(nickname);
    }

    //
    @PostMapping("/checkEmail")
    public CommonResDto checkEmail(@RequestParam EmailCheckDto dto) {
    log.info("checkEmail");
        return userService.checkEmail(dto);
    }
    
    // 비밀번호 변경
    @PostMapping("/pwchange")
    public CommonResDto pwChange(@RequestBody UserLoginReqDto dto) {
    log.info("pwChange");
       return userService.pwChange(dto);
    }

    // 닉네임 정보수정
    @PostMapping("/modify")
    public CommonResDto modify (@RequestBody String nickname){
    log.info("modify");
        return userService.modify(nickname);
    }

    // 회원탈퇴
    @DeleteMapping("/quit")
    public CommonResDto quit (@RequestParam String email) {
    log.info("quit");
        return userService.quit(email);
    }

    // 프로필 이미지 등록
    @PostMapping(value = "/profileimage", consumes = "multipart/form-data")
    public CommonResDto profileImageUpdate(  @RequestPart("img") MultipartFile img,
                                             @RequestPart("data") String dataJson) throws IOException {
    log.info("profileImageUpdate");
        ObjectMapper objectMapper = new ObjectMapper();
        UserLoginReqDto dto = objectMapper.readValue(dataJson, UserLoginReqDto.class); {
        return userService.profileImageUpdate(dto.getEmail(), img);}
    }

    // 친구 요청
    @PostMapping("/friends/addrequset")
    public CommonResDto addFriend(@RequestBody friendsDto dto) {
        log.info("addFriend");
        return userService.addFriends(dto);
    }

    //친구목록 조회
    @GetMapping("/friends/join")
    public CommonResDto userFriends(@RequestParam String nickname) {
    log.info("userFriends");
        return userService.UserFriends(nickname);
    }

    // 친구삭제
    @DeleteMapping("/friends/delete")
    public CommonResDto deleteFriend(@RequestBody friendsDto dto) {
        log.info("deleteFriend");
        return userService.deleteFreinds(dto);
    }
        
    //친구 요청응답    
    @PostMapping("/friends/addresponse")
    public CommonResDto addResFriend(@RequestBody friendsDto dto) {
        log.info("addFriend");
        return userService.addResFriends(dto);
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @GetMapping("/friends/addjoin")
        public CommonResDto addFriendJoin(@RequestParam String nickname) {
        log.info("addFriendJoin");
            return userService.addFriendsJoin(nickname);
    }
//
//    // 서버 추가요청
//    @PostMapping("/server/addrequset")
//        public CommonResDto addReqServer(@RequestParam String email){
//        log.info("addFriend");
//            return userService.addReqServer(email);
//    }
//
//    // 속한 서버 조회
//    @GetMapping("/server/join")
//        public CommonResDto userFriendJoin(@RequestParam String email) {
//        log.info("userFriends");
//            return userService.userServerJoin(email);
//    }
//
//    // 서버 탈퇴
//    @DeleteMapping("/server/deleteserver")
//        public CommonResDto deleteFriend(@RequestParam String email){
//        log.info("deleteFriend");
//            return userService.deleteServer(email);
//    }
//
//    //서버 요청 응답
//    @PostMapping("/server/addresponse")
//        public CommonResDto addResServer(@RequestParam String email) {
//        log.info("addFriend");
//                return userService.addResServer(email);
//    }
//
//    //서버요청 조회
//    @PostMapping("/server/addjoin")
//       public CommonResDto addServerJoin(@RequestParam String email) {
//        log.info("addFriendJoin");
//                return userService.addServerJoin(email);
//    }



}
