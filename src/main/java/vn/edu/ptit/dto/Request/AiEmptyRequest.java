package vn.edu.ptit.dto.Request;

/**
 * Input rỗng cho AI tool không cần tham số (VD: xem yêu cầu thuê hôm nay).
 * Spring AI yêu cầu Function<T, R> phải có input type, dù không cần tham số.
 */
public record AiEmptyRequest(){}
