package notification.api.support;

import java.util.List;

public record Page<T>(Boolean isLastPage, Long totalPage, List<T> data) {

    public static <R> Page<R> convertData(Page<?> page, List<R> data) {
        return new Page<>(page.isLastPage, page.totalPage, data);
    }
}
