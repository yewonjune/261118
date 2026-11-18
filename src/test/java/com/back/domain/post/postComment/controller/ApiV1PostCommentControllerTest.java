package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("댓글 다건조회: 1번 글의 댓글들, id 내림차순")
    void t1() throws Exception {
        // given
        int postId = 1;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/%d/comments".formatted(postId)))
                .andDo(print());

        // then
        Post post = postRepository.findById(postId).get();
        List<PostComment> comments = post.getComments().reversed();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        for (int i = 0; i < comments.size(); i++) {
            PostComment comment = comments.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(comment.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(comment.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(comment.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].postId".formatted(i)).value(postId))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(comment.getContent()));
        }
    }

    @Test
    @DisplayName("댓글 다건조회, 댓글이 없는 글이면 빈 배열")
    void t2() throws Exception {
        // given
        int postId = 3;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/%d/comments".formatted(postId)))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("댓글 다건조회, 존재하지 않는 글이면 404")
    void t3() throws Exception {
        // given
        int postId = Integer.MAX_VALUE;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/%d/comments".formatted(postId)))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }
}
