package com.games.balancegameback.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoResponse {

    private String email;
    private String profileImage;
}
