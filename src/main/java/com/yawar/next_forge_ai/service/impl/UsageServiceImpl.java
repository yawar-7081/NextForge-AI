package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.TotalTokenResponse;
import com.yawar.next_forge_ai.dto.UsedTokenResponse;
import com.yawar.next_forge_ai.entity.UsageLog;
import com.yawar.next_forge_ai.entity.User;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.repository.UsageLogRepository;
import com.yawar.next_forge_ai.repository.UserRepository;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.UsageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public TotalTokenResponse getTotalToken() {

//        String userId = jwtService.getLoggedInUserId();
//
//        UsageLog usageLog = usageLogRepository.findByUserId(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("Usage Log",userId));
//
//        return new TotalTokenResponse(usageLog.getUser().getId(),usageLog.getTotalUsedTokens());

        return null;
    }

    @Override
    public UsedTokenResponse getUsedToken() {
        return null;
    }

    @Override
    public void recordToken(Long token,String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));

        UsageLog usageLog = usageLogRepository.findByUserId(userId)
                .orElse(null);

        if(usageLog == null){
            usageLog = UsageLog.builder()
                    .user(user)
                    .totalUsedTokens(token)
                    .build();
        }
        usageLog.setTotalUsedTokens(
                usageLog.getTotalUsedTokens() + token
        );

        usageLogRepository.save(usageLog);
    }
}
