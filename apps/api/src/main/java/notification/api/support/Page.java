package notification.api.support;

import java.util.List;
import java.util.function.Function;

public record Page<T>(Boolean isLastPage, Long totalPage, List<T> data) {

    public static <T> Page<T> of(org.springframework.data.domain.Page<T> page) {
        return new Page<>(
                page.isLast(),
                (long) page.getTotalPages(),
                page.getContent()
        );
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(
                isLastPage,
                totalPage,
                data.stream()
                        .map(mapper)
                        .toList()
        );
    }
}
