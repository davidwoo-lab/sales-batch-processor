package com.davidlab.salesbatch.common.response;

/**
 * API 공통 응답 래퍼.
 * 성공 여부, 메시지, 실제 데이터를 일관된 형태로 감싼다.
 *
 * @param success 처리 성공 여부
 * @param message 응답 메시지
 * @param data    응답 데이터 (없으면 null)
 * @param <T>     데이터 타입
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    /** 성공 응답 생성 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    /** 메시지를 포함한 성공 응답 생성 */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** 실패 응답 생성 */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
