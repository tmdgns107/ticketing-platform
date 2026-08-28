package com.ticketing.domain.performance;

import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public List<PerformanceResponse> findAll() {
        return performanceRepository.findAll().stream()
                .map(PerformanceResponse::from)
                .toList();
    }

    public PerformanceResponse findById(Long id) {
        return performanceRepository.findById(id)
                .map(PerformanceResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND));
    }
}
