package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.AddStatus;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class friendsDto {
    private String ReqEmail;
    private String ResEmail;
    private AddStatus AddStatus;

    public friendsDto(String mail, String mail1) {
    }
}
