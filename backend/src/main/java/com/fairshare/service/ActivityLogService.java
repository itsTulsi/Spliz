package com.fairshare.service;

import com.fairshare.dto.ActivityLogDto;
import com.fairshare.entity.ActivityLogEntity;
import com.fairshare.entity.GroupEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void logActivity(GroupEntity group, String entityType, Long entityId,
                            ActivityEventType eventType, String summary) {
        ActivityLogEntity entity = new ActivityLogEntity(group, entityType, entityId, eventType, summary);
        activityLogRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getGroupActivity(Long groupId) {
        return activityLogRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(a -> new ActivityLogDto(
                    a.getId(),
                    a.getGroup().getId(),
                    a.getEntityType(),
                    a.getEntityId(),
                    a.getEventType(),
                    a.getSummary(),
                    a.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
