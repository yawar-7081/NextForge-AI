package com.yawar.next_forge_ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsedTokenResponse {
    private String userId;
    private Long usedToken;
}
