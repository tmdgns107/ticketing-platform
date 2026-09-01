package com.ticketing.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.global.error.ErrorCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  void okWrapsPayloadAndLeavesErrorNull() {
    ApiResponse<String> response = ApiResponse.ok("hello");

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isEqualTo("hello");
    assertThat(response.error()).isNull();
  }

  @Test
  void failCarriesErrorCodeNameAndMessage() {
    ApiResponse<Void> response =
        ApiResponse.fail(ErrorCode.PERFORMANCE_NOT_FOUND, "공연 정보를 찾을 수 없습니다.");

    assertThat(response.success()).isFalse();
    assertThat(response.data()).isNull();
    assertThat(response.error().code()).isEqualTo("PERFORMANCE_NOT_FOUND");
    assertThat(response.error().message()).isEqualTo("공연 정보를 찾을 수 없습니다.");
  }
}
