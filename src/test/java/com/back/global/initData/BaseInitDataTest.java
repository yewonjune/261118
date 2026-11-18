package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class BaseInitDataTest {
    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("샘플 데이터: 글 3개, 댓글 5개(1번 글 3개, 2번 글 2개, 3번 글 0개)")
    void t1() {
        List<Post> posts = postRepository.findAll();

        assertThat(posts).hasSize(3);
        assertThat(posts).extracting(Post::getTitle)
                .containsExactly("제목 1", "제목 2", "제목 3");
        assertThat(posts).extracting(post -> post.getComments().size())
                .containsExactly(3, 2, 0);
    }
}
