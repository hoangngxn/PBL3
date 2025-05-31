package com.example.controller;

import com.example.model.Bookmark;
import com.example.service.BookmarkService;
import com.example.service.BookmarkService.BookmarkWithPostDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@RequestBody CreateBookmarkRequest request) {
        return ResponseEntity.ok(bookmarkService.createBookmark(request.getPostId()));
    }

    @GetMapping
    public ResponseEntity<List<BookmarkWithPostDetails>> getUserBookmarks() {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable String postId) {
        bookmarkService.deleteBookmark(postId);
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class CreateBookmarkRequest {
        private String postId;
    }
} 