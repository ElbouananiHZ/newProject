package Find.read.Read.service;

import Find.read.Read.models.Comment;
import Find.read.Read.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public void addReply(String commentId, String userId, String username, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Comment.Reply reply = new Comment.Reply();
        reply.setReplyId(UUID.randomUUID().toString());
        reply.setUserId(userId);
        reply.setUsername(username);
        reply.setContent(content);
        reply.setCreatedAt(LocalDateTime.now());

        comment.getReplies().add(reply);
        commentRepository.save(comment);
    }

    public void deleteReply(String commentId, String replyId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setReplies(
                comment.getReplies().stream()
                        .filter(reply -> !reply.getReplyId().equals(replyId) || reply.getUserId().equals(userId))
                        .toList()
        );

        commentRepository.save(comment);
    }
    // In CommentService.java
    public void addComment(String novelId, String userId, String username, String content) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setNovelId(novelId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    public void deleteComment(String commentId, String userId) {
        commentRepository.findById(commentId)
                .ifPresent(comment -> {
                    if (comment.getUserId().equals(userId)) {
                        commentRepository.delete(comment);
                    }
                });
    }
}