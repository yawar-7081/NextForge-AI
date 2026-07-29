package com.yawar.next_forge_ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TotalTokenResponse {
    private String userId;
    private Long totalToken;
}
