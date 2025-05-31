package com.example.service;

import com.example.model.Bookmark;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.BookmarkRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public Bookmark createBookmark(String postId) {
        User currentUser = userService.getCurrentUser();

        // Check if post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if bookmark already exists
        if (bookmarkRepository.existsByUserIdAndPostId(currentUser.getId(), postId)) {
            throw new RuntimeException("Post is already bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(currentUser.getId());
        bookmark.setPostId(postId);
        bookmark.setCreatedAt(LocalDateTime.now());

        return bookmarkRepository.save(bookmark);
    }

    public List<BookmarkWithPostDetails> getUserBookmarks() {
        User currentUser = userService.getCurrentUser();
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(currentUser.getId());

        return bookmarks.stream()
                .map(bookmark -> {
                    Post post = postRepository.findById(bookmark.getPostId())
                            .orElse(null);
                    if (post == null) {
                        return null;
                    }
                    return new BookmarkWithPostDetails(bookmark, post);
                })
                .filter(bookmark -> bookmark != null)
                .collect(Collectors.toList());
    }

    public void deleteBookmark(String postId) {
        User currentUser = userService.getCurrentUser();
        bookmarkRepository.deleteByUserIdAndPostId(currentUser.getId(), postId);
    }

    @lombok.Data
    public static class BookmarkWithPostDetails {
        private String id;
        private String userId;
        private String postId;
        private String createdAt;
        private Post post;  // Full post details

        public BookmarkWithPostDetails(Bookmark bookmark, Post post) {
            this.id = bookmark.getId();
            this.userId = bookmark.getUserId();
            this.postId = bookmark.getPostId();
            this.createdAt = bookmark.getCreatedAt().toString();
            this.post = post;
        }
    }
} 