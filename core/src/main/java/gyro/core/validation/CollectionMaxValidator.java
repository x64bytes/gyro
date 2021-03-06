package gyro.core.validation;

import java.util.Collection;

import gyro.core.resource.Diffable;
import gyro.util.Bug;

public class CollectionMaxValidator implements Validator<CollectionMax> {

    @Override
    public boolean isValid(Diffable diffable, CollectionMax annotation, Object value) {
        if (value instanceof Collection) {
            return ((Collection<?>) value).size() <= annotation.value();
        }

        throw new Bug("Invalid usage of '@CollectionMax' validation annotation. Can only be used on types that extend Collection.");
    }

    @Override
    public String getMessage(CollectionMax annotation) {
        return String.format("Size of the collection must be less than or equal to @|bold %d|@", annotation.value());
    }
}
