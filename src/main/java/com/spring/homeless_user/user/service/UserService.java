package com.spring.homeless_user.user.service;


import com.amazonaws.services.s3.AmazonS3;
import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.FriendsRepository;
import com.spring.homeless_user.user.repository.ServerRepository;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final  FriendsRepository friendsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final  JavaMailSender mailSender;
//    private final AmazonS3 amazonS3;
    @Qualifier("login")
    private final RedisTemplate<String, String> redisLoginTemplate;
    @Qualifier("friends")
    private final RedisTemplate<String, String> friendsRedis;
    @Qualifier("server")
    private final RedisTemplate<String, String> serverRedis;
    @Qualifier("signup")
    private final RedisTemplate<String, String> redisSignupTemplate;

    public UserService(UserRepository userRepository,
                       ServerRepository serverRepository,
                       FriendsRepository friendsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender,
                       @Qualifier("login") RedisTemplate<String, String> redisLoginTemplate,
                       @Qualifier("friends") RedisTemplate<String, String> friendsRedis,
                       @Qualifier("server") RedisTemplate<String, String> serverRedis,
                       @Qualifier("signup") RedisTemplate<String, String> redisSignupTemplate) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.friendsRepository = friendsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.redisLoginTemplate = redisLoginTemplate;
        this.friendsRedis = friendsRedis;
        this.serverRedis = serverRedis;
        this.redisSignupTemplate = redisSignupTemplate;
    }


//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;


//    public String uploadFile(MultipartFile file) throws IOException {
//        if (!file.isEmpty()) {
//            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
//            amazonS3.putObject(bucketName, fileName, file.getInputStream(), null);
//            return amazonS3.getUrl(bucketName, fileName).toString(); // 업로드된 파일의 URL 반환
//        } else {
//            return null;
//        }
//    }

    //회원가입 로직
    public CommonResDto userSignUp(@Valid UserSaveReqDto dto, MultipartFile img) throws IOException {
        try{
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setCreateAt(LocalDateTime.now());
//        user.setProfileImage(uploadFile(img));


            userRepository.save(user);

            return new CommonResDto(HttpStatus.OK, "회원가입을 환영합니다.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    private void sendVerificationEmail(String email, String token) {
        // 이메일 제목과 본문 구성
        String subject = "이메일 인증을 해주세요";

        // 이메일 본문: 인증번호 포함
        String text = "<p>이메일 인증을 완료하려면 아래 인증번호를 입력창에 입력해주세요:</p>" +
                "<h3>" + token + "</h3>";

        try {
            // MimeMessage 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 이메일 정보 설정
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true); // HTML 형식으로 전송

            // 이메일 전송
            mailSender.send(message);

        } catch (Exception e) {
            // 예외 처리
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    //회원가입 인증로직
    public CommonResDto confirm(String email, String token) {
       try{
            redisSignupTemplate.opsForValue().get(email);
            String cachedToken = (String) redisSignupTemplate.opsForValue().get(token);
            if (cachedToken != null && cachedToken.equals(email)) {
                redisSignupTemplate.delete(email); // 토큰 검증 후 삭제
                return new CommonResDto(HttpStatus.OK, "token okay", "null");
            }
            return new CommonResDto(HttpStatus.OK, "token not okay", "null");
        }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
       }
    }

    
    // 로그인로직
    public CommonResDto userSignIn(UserLoginReqDto dto) {
        Optional<User> user = userRepository.findByEmail(dto.getEmail());
        log.info(user.get().toString());
        try{
            if (user.get() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid email.", null);
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.get().getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid password.", null);
            }
            //mysql에 refresh토큰 저장
            String refreshToken = jwtTokenProvider.refreshToken(dto.getEmail());
            user.get().setRefreshToken(refreshToken);

            userRepository.save(user.get());

            //redis에 accesstoken 저장
            String accessToken = jwtTokenProvider.accessToken(user.get().getEmail());
            redisLoginTemplate.opsForValue().set(dto.getEmail(), accessToken);


            return new CommonResDto(HttpStatus.OK, "SignUp successfully.", accessToken);
        } catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }


    public CommonResDto duplicateEmail(String email) {
        try{
            if (userRepository.findByEmail(email).isEmpty()) {
                return new CommonResDto(HttpStatus.OK, "이메일을 사용해도 좋아요.", null);
            } else {
                return new CommonResDto(HttpStatus.OK, "이메일 사용 불가", null);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);

        }
    }

    public CommonResDto duplicateNickname(String nickname) {
        try{
            if (userRepository.findByNickname(nickname) == null) {
                return new CommonResDto(HttpStatus.OK, "닉네임을 사용해도좋습니다.", null);
            } else {
                return new CommonResDto(HttpStatus.OK, "닉네임 사용 불가", null);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto pwChange(UserLoginReqDto dto) {
       try{
        Optional<User> user =userRepository.findByEmail(dto.getEmail());
        user.get().setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user.get());
        return new CommonResDto(HttpStatus.OK, "비밀번호 변경 성공", null);
       }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
       }
    }


    public CommonResDto quit(String email) {
        try{
            userRepository.deleteByEmail(email);
            return new CommonResDto(HttpStatus.OK, "삭제완료", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto profileImageUpdate(String email,MultipartFile img) throws IOException {
        try{
            Optional<User> user = userRepository.findByEmail(email);
//        user.setProfileImage(uploadFile(img));
            return new CommonResDto(HttpStatus.OK, "이미지 변경 완료", null);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto checkEmail(EmailCheckDto dto) {
        try{
            if (redisSignupTemplate.opsForValue().get(dto.getEmail()) != null && dto.getToken().equals(redisSignupTemplate.opsForValue().get(dto.getEmail()))){
                redisSignupTemplate.delete(dto.getEmail());
                return new CommonResDto(HttpStatus.OK, "인증번호가 일치합니다.",null);
            }
            return new CommonResDto(HttpStatus.OK, "인증번호가 일치하지 안습니다.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto modify(String nickname) {
        try{
            Optional<User> byNickname = userRepository.findByNickname(nickname);
            byNickname.get().setNickname(nickname);
            userRepository.save(byNickname.get());
            return new CommonResDto(HttpStatus.OK, "닉네임 변경 성공 ", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto addFriends(friendsDto dto) {
        try {
            // Redis에 친구 요청 확인
            String reqKey = dto.getReqNickname(); // 요청 키
            String resKey = dto.getResNickname(); // 응답 키

            // 이미 요청이 진행 중인지 확인
            if (friendsRedis.opsForSet().isMember(reqKey, dto.getResNickname())) {
                return new CommonResDto(HttpStatus.OK, "이미 친구요청이 진행중입니다", null);
            }

            // Redis에 요청 저장 (요청자와 응답자 양쪽에 저장)
            friendsRedis.opsForSet().add(reqKey, dto.getResNickname());
            friendsRedis.opsForSet().add(resKey, dto.getReqNickname());

            return new CommonResDto(HttpStatus.OK, "친구요청 완료", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }


    public CommonResDto UserFriends(String nickname) {
        try{
            Optional<User> byNickname = userRepository.findByNickname(nickname);
            Long userId = byNickname.get().getId();
            List<Friends> friends = friendsRepository.findAllById(userId);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, "친구 목록이 비어 있습니다.", Collections.emptyList());
            }
            // 닉네임 목록으로 변환
            List<String> friendNicknames = friends.stream()
                    .map(friend -> friend.getFriend().getNickname()) // 친구의 닉네임만 추출
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK, "친구목록 조회 성공", friendNicknames);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto deleteFreinds(friendsDto dto) {
        try {
            Optional<User> byNickname = userRepository.findByNickname(dto.getReqNickname());
            Long userId = byNickname.get().getId();
            List<Friends> friends = friendsRepository.findAllById(userId);
            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, "친구 목록이 비어 있습니다.", Collections.emptyList());
            }
            friends.removeIf(friend -> friend.equals(dto.getResNickname()));

            return new CommonResDto(HttpStatus.OK, "친국삭제 완료", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생" + e.getMessage(), null);
        }
    }

    public CommonResDto addResFriends(friendsDto dto) {
        try {
            String reqKey = dto.getReqNickname(); // 요청 리스트 키
            String resKey =  dto.getResNickname(); // 응답 리스트 키
            String friendKey1 =  dto.getReqNickname(); // 요청자의 친구 리스트 키
            String friendKey2 =  dto.getResNickname(); // 응답자의 친구 리스트 키

            // 요청자와 응답자 정보 조회
            Optional<User> reqUser = userRepository.findByNickname(dto.getReqNickname());
            Optional<User> resUser = userRepository.findByNickname(dto.getResNickname());


            // Mysql로 이관
            // 양방향 친구 관계 저장
            Friends friend1 = new Friends(resKey,reqKey);
            Friends friend2 = new Friends(reqKey,resKey);

            friendsRepository.save(friend1);
            friendsRepository.save(friend2);

            // 요청 리스트에서 제거
            friendsRedis.opsForSet().remove(reqKey, dto.getResNickname());
            friendsRedis.opsForSet().remove(resKey, dto.getReqNickname());

            return new CommonResDto(HttpStatus.OK, "친구가 되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto addFriendsJoin(String nickname) {
        String reqKey = nickname; // 요청 리스트 키
        String friendKey1 = nickname; // 요청자의 친구 리스트 키

        List<String> members = (List<String>) friendsRedis.opsForSet().members(reqKey);
        return new CommonResDto(HttpStatus.OK,"친구 요청 조회를 완료했습니다.", members );
    }
}


