package com.fairshare.controller;

import com.fairshare.dto.GroupDto;
import com.fairshare.dto.MemberDto;
import com.fairshare.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@Valid @RequestBody GroupDto dto) {
        GroupDto created = groupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> updateGroup(@PathVariable Long groupId, @Valid @RequestBody GroupDto dto) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, dto));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    // Members Sub-endpoints
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<MemberDto>> getGroupMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<MemberDto> addMember(@PathVariable Long groupId, @Valid @RequestBody MemberDto dto) {
        MemberDto created = groupService.addMemberToGroup(groupId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
