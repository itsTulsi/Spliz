package com.fairshare.controller;

import com.fairshare.dto.ActivityLogDto;
import com.fairshare.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogDto>> getGroupActivity(@PathVariable Long groupId) {
        return ResponseEntity.ok(activityLogService.getGroupActivity(groupId));
    }
}
