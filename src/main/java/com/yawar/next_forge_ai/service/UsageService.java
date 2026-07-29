package com.yawar.next_forge_ai.service;

import com.yawar.next_forge_ai.dto.TotalTokenResponse;
import com.yawar.next_forge_ai.dto.UsedTokenResponse;

public interface UsageService {
    TotalTokenResponse getTotalToken();

    UsedTokenResponse getUsedToken();

    void recordToken(Long token,String userId);
}
