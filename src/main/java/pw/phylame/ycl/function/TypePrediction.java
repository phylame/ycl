package pw.phylame.ycl.function;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TypePrediction implements Prediction<Object> {
    @NonNull
    private final Class<?> type;

    @Override
    public boolean test(Object obj) {
        return obj == null ? false : type.isInstance(obj);
    }

}
