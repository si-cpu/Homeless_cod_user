package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.AddStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class friendsDto {
    private String ReqNickname;
    private String ResNickname;
    private AddStatus AddStatus;
}
