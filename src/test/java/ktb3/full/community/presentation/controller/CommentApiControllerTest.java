package ktb3.full.community.presentation.controller;

import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.dto.request.CommentCreateRequest;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.dto.response.CommentResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithAuthMockUser
class CommentApiControllerTest extends ControllerTestSupport {

    @Nested
    class getAllComments {

        @Test
        void 게시글의_댓글_목록_페이지를_조회한다() throws Exception {
            // given
            long postId = 1L;
            PageRequest request = PageRequest.of(0, 10);
            PagedModel<CommentResponse> result = new PagedModel<>(new PageImpl<>(List.of()));

            given(commentService.getAllComments(postId, request)).willReturn(result);

            // when
            ResultActions resultActions = mockMvc.perform(get("/posts/{postId}/comments", postId)
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
    class createComment {

        @Test
        void 새_댓글을_작성한다() throws Exception {
            // given
            long userId = 1L;
            long postId = 1L;
            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content("content")
                    .build();

            CommentResponse result = CommentResponse.builder()
                    .commentId(1L)
                    .build();

            given(commentService.createComment(eq(userId), eq(postId), any(CommentCreateRequest.class))).willReturn(result);

            // when
            ResultActions resultActions = mockMvc.perform(post("/posts/{postId}/comments", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void 새_댓글_작성_시_내용에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            long postId = 1L;
            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content(" ")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(post("/posts/{postId}/comments", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

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
    class updateComment {

        @Test
        void 댓글의_내용을_수정한다() throws Exception {
            // given
            long commentId = 1L;

            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("updated content")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(patch("/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 댓글_수정_시_내용이_입력됐다면_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            long commentId = 1L;

            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content(" ")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(patch("/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

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
    class deleteComment {

        @Test
        void 댓글을_삭제한다() throws Exception {
            // given
            long commentId = 1L;

            // when
            ResultActions resultActions = mockMvc.perform(delete("/comments/{commentId}", commentId));

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