package uk.gov.defra.datareturns.testutils.client;

import io.restassured.response.ValidatableResponse;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import uk.gov.defra.datareturns.testutils.IntegrationTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractTestEntity {
    private final Map<String, Object> state = new HashMap<>();
    private final Map<String, Supplier<Object>> modifications = new HashMap<>();

    @Getter
    private String url;


    public ValidatableResponse read() {
        return IntegrationTestUtils.getEntity(url);
    }

    public synchronized void create() {
        create((r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });
    }

    public synchronized void create(final Consumer<ValidatableResponse> responseAssertions) {
        if (url != null) {
            throw new UnsupportedOperationException("Attempted to create a pre-existing entity");
        }
        final Map<String, Object> values = modifications.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll);
        this.url = IntegrationTestUtils.createEntity(getResourcePath(), new JSONObject(values).toString(), responseAssertions);
        this.state.putAll(values);
        this.modifications.clear();
    }

    public synchronized void update() {
        if (url == null) {
            throw new UnsupportedOperationException("Attempted to update a non-existent entity");
        }
        if (modifications.isEmpty()) {
            throw new UnsupportedOperationException("No changes to persist");
        }
        final Map<String, Object> values = modifications.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll);
        final JSONObject json = new JSONObject(values);
        IntegrationTestUtils.patchEntity(url, json.toString(), (r) -> {
            r.statusCode(HttpStatus.OK.value());
            r.body("errors", Matchers.nullValue());
        });
        this.state.putAll(values);
        this.modifications.clear();
    }

    public synchronized void delete() {
        IntegrationTestUtils.deleteEntity(url);
    }

    Object modify(final String key, final Supplier<Object> value) {
        return this.modifications.put(key, value);
    }

    Object modify(final String key, final String value) {
        return this.modifications.put(key, () -> value);
    }

    Object modify(final String key, final Number value) {
        return this.modifications.put(key, () -> value);
    }

    Object modify(final String key, final Boolean value) {
        return this.modifications.put(key, () -> value);
    }

    Object modify(final String key, final LocalDate value) {
        return this.modifications.put(key, () -> value);
    }

    private Object getValue(final String key) {
        Object toReturn = this.state.get(key);
        final Supplier<?> modified = this.modifications.get(key);
        if (modified != null) {
            toReturn = modified.get();
        }
        return toReturn;
    }

    Integer getIntegerValue(final String key) {
        return (Integer) getValue(key);
    }


    /**
     * @return Path to the collection resource on the server (relative to the API root)
     */
    abstract String getResourcePath();
}
