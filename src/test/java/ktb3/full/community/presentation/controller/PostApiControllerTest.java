package ktb3.full.community.presentation.controller;

import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.dto.response.PostDetailResponse;
import ktb3.full.community.dto.response.PostResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithAuthMockUser
class PostApiControllerTest extends ControllerTestSupport {

    @Nested
    class getAllPosts {

        @Test
        void 게시글_목록_페이지를_조회한다() throws Exception {
            // given
            PageRequest request = PageRequest.of(0, 10);
            PagedModel<PostResponse> result = new PagedModel<>(new PageImpl<>(List.of()));

            given(postService.getAllPosts(request)).willReturn(result);

            // when
            ResultActions resultActions = mockMvc.perform(get("/posts")
                    .param("page", "0")
                    .param("size", "10"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page").isMap());
        }
    }

    @Nested
    class getPostDetail {

        @Test
        void 게시글_상세정보를_조회한다() throws Exception {
            // given
            long userId = 1L;
            long postId = 1L;
            PostDetailResponse result = PostDetailResponse.builder().build();

            given(postService.getPost(userId, postId)).willReturn(result);

            // when
            ResultActions resultActions = mockMvc.perform(get("/posts/{postId}", postId));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }
    }

    @Nested
    class createPost {

        @Test
        void 새_게시글을_작성한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                    .file(MultipartFileFixture.createImage())
                    .param("title", "post title")
                    .param("content", "post content"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 새_게시글_작성_시_제목에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                    .file(MultipartFileFixture.createImage())
                    .param("title", " ")
                    .param("content", "post content"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("제목은 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 새_게시글_작성_시_내용에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/posts")
                    .file(MultipartFileFixture.createImage())
                    .param("title", "post title")
                    .param("content", " "));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("내용은 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    class updatePost {

        @Test
        void 게시글의_제목만_수정한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, "/posts/{postId}", postId)
                    .param("title", "updated title"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 게시글의_내용만_수정한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, "/posts/{postId}", postId)
                    .param("content", "updated content"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 게시글의_이미지만_수정한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, "/posts/{postId}", postId)
                    .file(MultipartFileFixture.createImage()));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 게시글_수정_시_제목이_입력됐다면_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, "/posts/{postId}", postId)
                    .param("title", " "));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("제목은 공백일 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 게시글_수정_시_내용이_입력됐다면_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, "/posts/{postId}", postId)
                    .param("content", " "));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("내용은 공백일 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    class deletePost {

        @Test
        void 게시글을_삭제한다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(delete("/posts/{postId}", postId));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    class likePost {

        @Test
        void 게시글에_좋아요를_누른다() throws Exception {
            // given
            long postId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(post("/posts/{postId}/like", postId));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}